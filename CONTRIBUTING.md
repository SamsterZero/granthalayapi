# Contributing to Granthalay API

Thank you for helping build Granthalay's backend. Keep changes focused and preserve the local-first,
privacy-first behavior of the companion reader.

## Before starting

- Read the [Project Guide](PROJECT.md) and [architecture](docs/architecture.md).
- Search [existing issues](https://github.com/SamsterZero/granthalayapi/issues).
- Discuss new modules, breaking API changes, authentication, payment flows, or persistence redesigns
  before implementing them.
- Report suspected vulnerabilities privately according to [SECURITY.md](SECURITY.md).

## Local setup

Install JDK 25 and Docker with Compose support, then fork and clone the repository:

```sh
git clone https://github.com/<your-account>/granthalayapi.git
cd granthalayapi
```

Start the application:

```sh
./mvnw spring-boot:run
```

The development database is defined in `compose.yaml` and may be started explicitly with
`docker compose up -d postgres`. Keep credentials local; `.env` files are ignored.

The API defaults to `http://localhost:8080`. Health probes are available under `/actuator/health`.
Swagger UI and other routes remain denied until their access policies are introduced.

## Validate a change

```sh
./mvnw verify
```

Tests use Testcontainers and require a working Docker daemon. Add focused tests for new behavior and
integration tests for persistence, security boundaries, migrations, and module interactions.

## Containers

Build the application image locally with:

```sh
docker build -t granthalay-api:dev .
```

With Python 3 available, check the image against an isolated Compose database:

```sh
python3 scripts/check-container.py --engine podman --image granthalay-api:dev
```

Use `--engine docker` for Docker. The check verifies startup and probe behavior during a database
outage, then removes its test containers, network, and volume. It does not use your development database.

The container requires a reachable PostgreSQL database. Set `SPRING_DATASOURCE_URL` to its JDBC URL,
`SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` at runtime. Never bake secrets
into an image. For example, after exporting these variables locally:

```sh
podman run --rm -p 8080:8080 \
  -e SPRING_DATASOURCE_URL -e SPRING_DATASOURCE_USERNAME -e SPRING_DATASOURCE_PASSWORD \
  granthalay-api:dev
```

Release automation publishes multi-architecture images to
`ghcr.io/samsterzero/granthalayapi`.

## Database changes

- Add versioned Flyway migrations; never edit a migration that may have been released.
- Prefer backward-compatible expand-and-contract changes for deployed data.
- Include rollback or recovery notes for risky migrations.
- Do not make one module depend directly on another module's tables.

## Pull requests

- Link the issue and describe the user-visible or operational outcome.
- List the exact automated and manual checks performed.
- Call out API, schema, authentication, authorization, privacy, and deployment effects.
- Update OpenAPI and relevant docs with contract or architectural changes.
- Link related frontend work when a change affects `SamsterZero/Granthalay`.
- Avoid unrelated formatting or generated-file changes.

Use clear imperative commit subjects. Contributions are submitted under the [MIT License](LICENSE)
and must follow the [Code of Conduct](CODE_OF_CONDUCT.md).
