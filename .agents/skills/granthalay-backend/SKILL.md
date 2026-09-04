---
name: granthalay-backend
description: Implement or refactor Granthalay API modules, application use cases, HTTP endpoints, and provider adapters in the Spring Boot modular monolith. Use for backend feature work; use granthalay-database for migration-focused tasks and granthalay-review for review-only requests.
---

# Granthalay backend development

Read `PROJECT.md`, `docs/architecture.md`, and `docs/api-conventions.md` before changing behavior.
Inspect the companion frontend only when its current contract or behavior materially affects the task.

## Preserve product boundaries

- The static PWA remains useful without this API.
- Personal EPUBs, reading activity, bookmarks, and highlights stay device-local by default.
- Backend ownership is limited to accounts, catalog, content, commerce, entitlements, notifications,
  publishers, administration, and audit concerns.

## Implement by module

Choose the owning business module before adding code. Keep its domain, application logic, persistence,
and adapters together under `dev.samster.granthalay.<module>`. Expose cross-module behavior through a
published application API or domain event; never reach into another module's repositories or entities.

Keep HTTP controllers responsible for transport mapping and validation, not business decisions.
Place transaction boundaries around application use cases. Put email, payment, object-storage, and
observability SDKs behind interfaces owned by the consuming module.

For public endpoints, use `/api/v1`, Bean Validation, precise status codes, and RFC 9457 Problem
Details. Generate identifiers and timestamps server-side where trust matters. Enforce authorization
again at the use-case boundary rather than relying only on route configuration.

## Complete the change

Add focused unit tests and integration tests proportional to the risk. Verify new module structures
with Spring Modulith tests. Use Testcontainers for PostgreSQL behavior and avoid mocked persistence
when database semantics are the subject of the test. Update OpenAPI descriptions and durable docs
with contract or architecture changes.

Run `./mvnw verify`. For container or runtime configuration changes, also validate Compose and build
the OCI image. Report any check that cannot run and the concrete environment limitation.
