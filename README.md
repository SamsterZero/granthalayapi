# Granthalay API

Granthalay API is the modular Spring Boot backend for
[Granthalay](https://github.com/SamsterZero/Granthalay), a privacy-first, local-first EPUB library
and offline PWA. The frontend remains independently useful on GitHub Pages; this service adds
optional connected bookstore capabilities without taking ownership of personal books or reading
activity.

> **Project status:** foundation stage. The application skeleton and infrastructure are present,
> but public API endpoints and product modules are not implemented yet.

## Planned responsibilities

- Email/password accounts and revocable sessions
- Public catalog and publisher metadata
- Orders, payments, and entitlement decisions
- Entitlement-checked book delivery from object storage
- Transactional notifications
- Publisher and administrative operations
- Security-relevant audit events

Personal EPUB imports, reading position, bookmarks, and highlights stay in the browser by default.
They must not require an account or be collected by this API unless a future feature provides clear,
explicit user control.

## Technology

- Java 25 and Spring Boot 4
- Spring Modulith
- PostgreSQL, Flyway, and Spring Data JPA
- Spring Security and JDBC-backed sessions
- OpenAPI/Swagger UI
- JUnit and Testcontainers
- Docker/OCI images published through GitHub Actions

## Run locally

Prerequisites: JDK 25 and Docker with Compose support.

```sh
git clone https://github.com/SamsterZero/granthalayapi.git
cd granthalayapi
./mvnw spring-boot:run
```

Spring Boot's Docker Compose integration starts the PostgreSQL service from `compose.yaml`. To run
the database yourself instead:

```sh
docker compose up -d postgres
./mvnw spring-boot:run
```

The API defaults to `http://localhost:8080`. API documentation will be available at
`/swagger-ui.html` as endpoints are introduced.

## Validate a change

```sh
./mvnw verify
```

The integration test starts a disposable PostgreSQL container, so Docker must be running.

## Container image

Build locally with:

```sh
docker build -t granthalay-api:dev .
docker run --rm -p 8080:8080 granthalay-api:dev
```

The application container needs a reachable PostgreSQL database and runtime configuration. Do not
bake secrets into an image. Published images use `ghcr.io/samsterzero/granthalayapi`.

## Documentation

- [Project principles and scope](PROJECT.md)
- [Architecture](docs/architecture.md)
- [API conventions](docs/api-conventions.md)
- [Development and pull requests](CONTRIBUTING.md)
- [Security policy](SECURITY.md)

## Contributing and license

Read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request. Report vulnerabilities
privately as described in [SECURITY.md](SECURITY.md). Granthalay API is licensed under the
[MIT License](LICENSE).
