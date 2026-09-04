# API conventions

These rules apply as HTTP endpoints are introduced.

## URLs and media types

- Public endpoints live below `/api/v1`.
- Use plural resource nouns and standard HTTP semantics.
- JSON is the default representation; file delivery uses an explicit safe content type.
- Pagination uses bounded `page` and `size` parameters until a cursor contract is required.
- OpenAPI is generated from the running application and updated with contract changes.

## Errors

Use RFC 9457 Problem Details (`application/problem+json`). A response should include a stable
machine-readable problem type, status, human-readable title, and safe detail. Validation failures
identify invalid fields without leaking rejected secrets or internal implementation details.

## Compatibility

Additive response fields are backward compatible; clients must ignore fields they do not recognize.
Removing or renaming fields, changing meaning, or tightening accepted input requires a new API
version or a documented migration window.

## Authentication and browser access

Public catalog endpoints may be anonymous. Account, order, entitlement, delivery, publisher, and
administrative endpoints require explicit authorization. CORS permits only configured Granthalay
origins and the minimum required methods and headers. Never place credentials or access tokens in
URLs.

## Observability

Propagate or create a request correlation identifier and return it in error responses when safe.
Metrics and logs identify operations, latency, outcome, and non-sensitive identifiers; they never
contain passwords, tokens, payment details, book content, or raw personal data.
