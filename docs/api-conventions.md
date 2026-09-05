# API conventions

This document defines the HTTP compatibility policy. The machine-readable source of truth is
[`src/main/resources/static/openapi/granthalay-api-v1.yaml`](../src/main/resources/static/openapi/granthalay-api-v1.yaml).
The running application publishes that exact document at `/openapi/granthalay-api-v1.yaml` and links
to it from `GET /api/v1`. Dynamic Springdoc and Swagger UI routes remain private so there is only one
public contract document.

## URLs and media types

- Public endpoints live below `/api/v1`.
- Use plural resource nouns and standard HTTP semantics.
- JSON is the default representation; file delivery uses an explicit safe content type.
- Pagination uses bounded `page` and `size` parameters until a cursor contract is required.
- Request and response bodies use transport DTOs. Persistence entities must never cross the HTTP
  boundary; `HttpBoundaryTests` enforces this for controller signatures.
- Optional request fields are distinguishable from explicit `null` where those meanings differ.
- Success responses document every status, media type, and body. `204 No Content` has no body.
- The `Location` header identifies a newly created resource after a successful `201 Created`.

## Pagination

Collection endpoints use the reusable OpenAPI `Page` and `Size` parameters and return their items
with `PageMetadata`. `page` is zero-based, `size` is between 1 and 100, and stable sorting must be
documented by each operation. Pagination may move to an opaque cursor in a future API version when
offset pagination is no longer safe or efficient.

## Errors

Failures use RFC 9457 Problem Details (`application/problem+json`). `type` and validation `code`
values are stable machine-readable identifiers; `title` and `detail` are safe human-readable text.
Validation failures use `ValidationProblem.errors` with `field`, `code`, and `message`, never the
rejected value. Authentication and authorization failures use the same media type and do not reveal
whether an account or protected resource exists. A safe `requestId` may be returned for support.

## Compatibility

Additive response fields are backward compatible; clients must ignore fields they do not recognize.
Removing or renaming fields, changing meaning, or tightening accepted input requires a new API
`ApiContractCompatibilityTests` compares the published v1 contract with its committed compatibility
baseline. When intentionally expanding the contract, update the source document first and then the
baseline. A breaking v1 change must not update the baseline; introduce a new API version instead.

Maven generates a Fetch-based TypeScript client into `target/generated-clients/typescript` during
`generate-test-resources`, so `verify` proves that the published contract remains generator-compatible.

## Authentication and browser access

Public catalog endpoints may be anonymous. Account, order, entitlement, delivery, publisher, and
administrative endpoints require explicit authorization. Browser sessions use the `SESSION` cookie;
non-browser clients may use bearer credentials when an operation declares that scheme. Never place
credentials or access tokens in URLs.

CORS applies only below `/api/**`, permits credentialed requests only from the comma-separated
`GRANTHALAY_WEB_ALLOWED_ORIGINS` configuration, and never uses a wildcard origin. Allowed request
headers are `Accept`, `Content-Type`, `Authorization`, and `X-Request-ID`; exposed response headers
are `Location` and `X-Request-ID`.

## Observability

Propagate or create a request correlation identifier and return it in error responses when safe.
Metrics and logs identify operations, latency, outcome, and non-sensitive identifiers; they never
contain passwords, tokens, payment details, book content, or raw personal data.
