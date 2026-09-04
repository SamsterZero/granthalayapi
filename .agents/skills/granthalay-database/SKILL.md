---
name: granthalay-database
description: Design, implement, or review Granthalay PostgreSQL schemas, persistence mappings, and Flyway migrations. Use when work changes stored data, constraints, indexes, migration safety, or module data ownership.
---

# Granthalay database changes

Read `PROJECT.md` and `docs/architecture.md`, then inspect all existing migrations and the owning
module's persistence model before proposing a change.

## Protect module ownership

Each module owns its tables and persistence types. Do not let one module query or mutate another
module's tables directly. Represent cross-module references with stable identifiers and validate the
business relationship through the owning module's application API when needed.

## Design the schema

- Encode invariants with appropriate types, `NOT NULL`, uniqueness, checks, and foreign keys inside
  the owning boundary.
- Add indexes for demonstrated query and constraint needs; account for column order and write cost.
- Store timestamps with time zone semantics and generate security-relevant times server-side.
- Avoid storing secrets in plaintext. Minimize personal data and define retention or deletion effects.
- Make JPA mappings reflect database nullability and constraints instead of relying on ORM defaults.

## Write safe migrations

Use a new versioned Flyway migration. Never edit a migration that may have run outside a disposable
development database. Prefer backward-compatible expand-and-contract steps for deployed schemas.
Avoid long table rewrites and blocking constraint validation when data volume could make them unsafe.
For destructive or irreversible changes, document backup, rollout, verification, and recovery needs.

## Verify

Add PostgreSQL-backed integration tests for constraints, mappings, queries, and migration outcomes.
Run `./mvnw verify` with a Docker-compatible daemon. For a risky migration, also test upgrading from a
representative prior schema and inspect the generated query plan for performance claims.
