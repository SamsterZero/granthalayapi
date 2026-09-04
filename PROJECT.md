# Granthalay API Project Guide

This repository contains the separately deployed backend for the
[Granthalay static PWA](https://github.com/SamsterZero/Granthalay). It begins as a modular monolith:
one application and one PostgreSQL database with explicit module ownership.

## Status

The project is at the foundation stage. The application skeleton, local PostgreSQL environment,
container image, documentation, and CI automation are present; public API endpoints and product
modules are not implemented yet.

## Technology

- Java 25 and Spring Boot 4
- Spring Modulith
- PostgreSQL, Flyway, and Spring Data JPA
- Spring Security and JDBC-backed sessions
- OpenAPI and Swagger UI
- JUnit and Testcontainers
- Docker-compatible OCI images published to GitHub Container Registry

## Product constraints

- Anonymous users can import and read personal EPUBs without this service.
- Personal books, reading history, bookmarks, and highlights remain device-local by default.
- The frontend must degrade gracefully when the API is unavailable.
- Purchased content is delivered only after an entitlement check and is never bundled into the
  static frontend.
- Credentials, payment data, tokens, book files, and personal data must never appear in logs.
- External email, payment, storage, and observability providers stay behind adapters.
- A module may become a separate service only when operational evidence justifies the cost.

## Intended modules

`accounts`, `catalog`, `content`, `commerce`, `entitlements`, `notifications`, `publishers`,
`administration`, and `audit` are the intended business boundaries. Cross-module access must use a
published internal API or domain event rather than another module's repository or tables.

These modules collectively provide email/password accounts and revocable sessions, catalog and
publisher metadata, orders and payment processing, entitlement decisions, protected book delivery,
transactional notifications, publisher and administrative operations, and security-relevant audit
events.

## Related resources

- [Frontend repository](https://github.com/SamsterZero/Granthalay)
- [Architecture](docs/architecture.md)
- [API conventions](docs/api-conventions.md)
- [Security policy](SECURITY.md)
- [Granthalay roadmap](https://github.com/users/SamsterZero/projects/5)

## Source of truth

Executable behavior and tests outrank plans. Durable decisions belong in `docs/`; actionable work
belongs in GitHub issues. Update affected documentation in the same pull request as a behavior or
contract change.

## Definition of done

A change is complete when success and failure paths are handled, automated checks pass, module
boundaries remain intact, privacy and security effects are reviewed, migrations are safe, and
affected API and architecture documentation is current.
