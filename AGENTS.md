# Granthalay API agent guide

This repository is the separately deployed Spring Boot backend for the
[Granthalay PWA](https://github.com/SamsterZero/Granthalay). Preserve the frontend's anonymous,
local-first reader: personal EPUB imports, reading history, bookmarks, and highlights stay in the
browser by default.

## Start here

Before substantial work, read `PROJECT.md` and the relevant file under `docs/`. Use repository-local
skills when their descriptions match the task:

- `granthalay-backend` for modules, use cases, endpoints, and integrations
- `granthalay-database` for PostgreSQL schema and Flyway migrations
- `granthalay-review` for correctness, security, privacy, and architecture reviews
- `gh-stack` only when the user requests stacked branches or pull requests

## Architecture

- Build a modular monolith with Spring Modulith; do not introduce distributed infrastructure unless
  the task explicitly requires it.
- Business modules own their domain types, repositories, and tables. Cross-module collaboration uses
  published application APIs or domain events.
- Keep controllers thin, transaction boundaries in application services, and provider-specific code
  behind adapters.
- Public HTTP APIs live under `/api/v1` and use RFC 9457 Problem Details for errors.
- Never weaken the local-first frontend boundary to simplify backend implementation.

## Engineering rules

- Use Java 25 language features only when they improve clarity and remain consistent with the codebase.
- Prefer constructor injection, immutable values, explicit validation, and package-private implementation
  types where practical.
- Treat authentication, authorization, entitlements, payment callbacks, and file delivery as security
  boundaries. Default to denial when identity or authorization is uncertain.
- Never log credentials, tokens, payment data, personal data, or book content.
- Add a new forward-only Flyway migration for released schema changes; never rewrite migration history.
- Update tests, OpenAPI descriptions, and durable documentation in the same change as behavior.

## Verification

Run the narrowest relevant tests while iterating, then `./mvnw verify` before handoff. Tests use
Testcontainers and require a Docker-compatible daemon. For container changes, also validate
`podman compose config` or `docker compose config` and build the image.

Do not commit, push, publish packages, alter remote settings, or mutate external services unless the
user explicitly requests that action.
