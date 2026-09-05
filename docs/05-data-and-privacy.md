# Data Ownership and Privacy

## Zero-Logging Privacy Policy

Privacy and security boundaries are enforced strictly across logging and database storage.

```mermaid
flowchart TD
  subgraph Input["Incoming HTTP Request / Execution"]
    A[Credentials / Passwords]
    B[Session & Bearer Tokens]
    C[Payment & Card Data]
    D[Book EPUB File Content]
    E[Personal Identity Data]
  end

  subgraph Sanitize["Granthalay Log Redactor Filter"]
    F["Redact Sensitive Fields & Sanitize Outputs"]
  end

  subgraph SafeLogs["Structured Application Logs"]
    G[Operation Name & Correlation ID]
    H[HTTP Status & Latency Metrics]
    I[Non-Sensitive Module Audit Events]
  end

  Input --> Sanitize --> SafeLogs

  style Input fill:#7f1d1d,stroke:#ef4444,color:#fff
  style Sanitize fill:#1e3a8a,stroke:#3b82f6,color:#fff
  style SafeLogs fill:#064e3b,stroke:#10b981,color:#fff
```

### Strict Redaction Rules
1. **Never Log Secrets**: Passwords, auth tokens, session IDs, and authorization headers must never be written to logs.
2. **Never Log Payment Data**: Credit card details, bank accounts, and payment payload data stay within encrypted adapter boundaries.
3. **Never Log Book Contents**: EPUB binary data or book body text must never be logged.
4. **Never Log Raw PII**: Personal identification data (email addresses, phone numbers, full names) must not appear in diagnostic logs.

---

## Database Schema Ownership

Flyway is the **sole schema writer** for the PostgreSQL database.

```mermaid
flowchart LR
  subgraph Migrations["src/main/resources/db/migration"]
    V1["V1__identity_schema.sql"]
    V2["V2__operations_event_registry.sql"]
    V3["V3__future_module_schema.sql"]
  end

  subgraph Flyway["Flyway Migration Engine"]
    F[Forward-Only Execution]
  end

  subgraph Database["PostgreSQL Database"]
    T1[("identity: SPRING_SESSION")]
    T2[("operations: event_publication")]
    T3[("business schemas...")]
  end

  Migrations --> Flyway --> Database

  style Migrations fill:#1f2937,stroke:#3b82f6,color:#fff
  style Flyway fill:#111827,stroke:#f59e0b,color:#fff
  style Database fill:#1f2937,stroke:#10b981,color:#fff
```

### Database Migration Rules
- **Forward-Only Migrations**: Released schema changes use immutable, forward-only Flyway migrations. Never alter or delete released migration scripts.
- **Module Table Ownership**: Business modules own their database tables. Cross-module database queries are forbidden.
- **Disabled Auto-Generators**: Hibernate schema generation (`ddl-auto`), SQL script execution, and Flyway clean are disabled in all environments.
