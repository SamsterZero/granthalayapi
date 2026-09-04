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

Install JDK 25 and Docker, fork and clone the repository, then run:

```sh
./mvnw spring-boot:run
```

The development database is defined in `compose.yaml` and may be started explicitly with
`docker compose up -d postgres`. Keep credentials local; `.env` files are ignored.

## Validate a change

```sh
./mvnw verify
docker build -t granthalay-api:dev .
```

Tests use Testcontainers and require a working Docker daemon. Add focused tests for new behavior and
integration tests for persistence, security boundaries, migrations, and module interactions.

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
