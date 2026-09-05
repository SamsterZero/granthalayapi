#!/usr/bin/env python3
"""Check a built image with an isolated Compose database, then remove test resources."""

import argparse
import json
from pathlib import Path
import subprocess
import time
import urllib.error
import urllib.request
import uuid


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--engine", choices=("docker", "podman"), default="docker")
    parser.add_argument("--image", default="granthalay-api:dev")
    args = parser.parse_args()
    project = "granthalay-check-" + uuid.uuid4().hex[:10]
    database, application = project + "-db", project + "-app"
    root = Path(__file__).resolve().parent.parent
    compose = [args.engine, "compose", "-f", str(root / "compose.yaml"), "-p", project]

    def run(command):
        return subprocess.check_output(command, text=True, cwd=root).strip()

    def health(port, path, timeout=5):
        try:
            with urllib.request.urlopen(f"http://127.0.0.1:{port}{path}", timeout=timeout) as response:
                return response.status, json.load(response)
        except urllib.error.HTTPError as error:
            return error.code, json.load(error)

    try:
        # Compose run does not publish the service ports, avoiding conflicts with local databases.
        run(compose + ["run", "--detach", "--name", database, "postgres"])
        deadline = time.monotonic() + 60
        while subprocess.run(
            [args.engine, "exec", database, "pg_isready", "-U", "granthalay", "-d", "granthalay"],
            stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL,
        ).returncode:
            if time.monotonic() >= deadline:
                raise RuntimeError("PostgreSQL did not become ready within 60 seconds")
            time.sleep(1)

        run([args.engine, "run", "--detach", "--name", application,
             "--network", project + "_default", "-p", "127.0.0.1::8080",
             "-e", f"SPRING_DATASOURCE_URL=jdbc:postgresql://{database}:5432/granthalay",
             "-e", "SPRING_DATASOURCE_USERNAME=granthalay",
             "-e", "SPRING_DATASOURCE_PASSWORD=granthalay", args.image])
        port = run([args.engine, "port", application, "8080/tcp"]).rsplit(":", 1)[1]
        deadline = time.monotonic() + 60
        while True:
            if run([args.engine, "inspect", "--format", "{{.State.Running}}", application]) != "true":
                raise RuntimeError("Application container exited before becoming ready")
            try:
                if health(port, "/actuator/health/readiness") == (200, {"status": "UP"}):
                    break
            except (OSError, ValueError):
                pass
            if time.monotonic() >= deadline:
                raise RuntimeError("Application did not become ready within 60 seconds")
            time.sleep(1)

        if health(port, "/actuator/health/liveness") != (200, {"status": "UP"}):
            raise RuntimeError("Liveness check failed")
        if health(port, "/actuator/env")[0] != 403:
            raise RuntimeError("Sensitive actuator route was not denied")
        print("Container startup, migrations, and protected probes passed.", flush=True)

        run([args.engine, "stop", "--time", "2", database])
        if health(port, "/actuator/health/readiness", timeout=40) != (503, {"status": "DOWN"}):
            raise RuntimeError("Readiness did not report the database outage")
        if health(port, "/actuator/health/liveness") != (200, {"status": "UP"}):
            raise RuntimeError("Database outage incorrectly affected liveness")
        print("Database outage: readiness 503, liveness 200.", flush=True)
    finally:
        subprocess.run([args.engine, "rm", "--force", application, database],
                       stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        subprocess.run(compose + ["down", "--volumes", "--remove-orphans"], check=True)


if __name__ == "__main__":
    main()
