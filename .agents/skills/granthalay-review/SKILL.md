---
name: granthalay-review
description: Review Granthalay backend changes for correctness, regressions, module boundaries, security, privacy, database safety, API compatibility, and missing tests. Use for review or audit requests, not ordinary feature implementation.
---

# Granthalay backend review

Read `PROJECT.md`, `docs/architecture.md`, `docs/api-conventions.md`, and `SECURITY.md`. Inspect the
actual diff plus surrounding callers, configuration, migrations, and tests. Do not modify code unless
the user asks for fixes.

## Review priorities

1. Authentication, authorization, entitlement checks, payment authenticity, file access, injection,
   secret handling, and unsafe logging.
2. Data loss, transaction correctness, concurrency, idempotency, retries, and partial failure.
3. Module ownership violations and coupling that bypasses application APIs or domain events.
4. API compatibility, validation, status codes, Problem Details, CORS, and frontend degradation.
5. Migration correctness, production rollout safety, constraints, and query/index behavior.
6. Missing tests, observability gaps, documentation drift, and maintainability issues.

Preserve Granthalay's central privacy invariant: personal EPUBs and reading activity remain local by
default. Treat accidental collection or logging of that data as a high-severity finding.

## Report findings

Lead with findings ordered by severity. For each finding, cite the exact file and line, explain the
concrete failure or exploit scenario, and recommend the smallest viable correction. Distinguish
verified defects from questions or assumptions. If no defects are found, say so and identify residual
risks or checks that could not be performed.

Run relevant read-only checks when available. Do not claim a check passed unless it actually ran.
