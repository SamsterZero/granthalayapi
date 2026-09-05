# Architecture

## System Context & C4 Container Diagram

```mermaid
flowchart LR
  PWA["Granthalay PWA<br/>(Static Frontend / GitHub Pages)"]
  API["Granthalay API<br/>(Spring Boot 4 Modular Monolith)"]
  DB[("PostgreSQL 18<br/>(Isolated Schema Ownership)")]
  OBJ[("Object Storage<br/>(EPUB Books)")]
  EXT["External Adapters<br/>(Payment & Email Providers)"]

  PWA -->|Versioned HTTPS API /api/v1| API
  API -->|Spring Data JPA / Flyway| DB
  API -->|Storage Adapter| OBJ
  API -->|Provider Adapters| EXT

  style PWA fill:#1f2937,stroke:#3b82f6,color:#fff
  style API fill:#111827,stroke:#10b981,color:#fff
  style DB fill:#1f2937,stroke:#f59e0b,color:#fff
  style OBJ fill:#1f2937,stroke:#8b5cf6,color:#fff
  style EXT fill:#1f2937,stroke:#ef4444,color:#fff
```

The backend is built as a **Spring Modulith modular monolith** within a single application artifact and PostgreSQL database.

---

## Modulith Architecture & Event Publication Flow

Modules must remain decoupled. Modules interact using **published internal application APIs** or **domain events**. Cross-module direct invocation of another module's repository or entity is forbidden and checked by ArchUnit (`ModuleArchitectureTests`).

```mermaid
sequenceDiagram
  autonumber
  participant Publisher as Module A (e.g. Commerce)
  participant Modulith as Spring Modulith Event Registry
  participant EventLog as PostgreSQL (JPA Event Publication Table)
  participant Consumer as Module B Listener (e.g. Entitlements)

  Publisher->>Modulith: Publish Domain Event (e.g. OrderCompletedEvent)
  Modulith->>EventLog: Persist Event State (Incomplete)
  Modulith->>Consumer: Invoke @ApplicationModuleListener
  Consumer-->>Modulith: Listener Execution Succeeded
  Modulith->>EventLog: Mark Event State (Completed)
```

---

## Foundation Modules

1. **`identity`**: User accounts, revocable browser sessions (`SESSION` cookie), credentials.
2. **`catalog`**: Book catalog metadata, authors, genres, public listings.
3. **`publishing`**: Publisher accounts, content submissions, publishing workflows.
4. **`storage`**: Content file storage integration behind replaceable adapters.
5. **`commerce`**: Orders, checkout flows, payment provider webhook callbacks.
6. **`entitlements`**: Authorization decisions determining book ownership and access.
7. **`delivery`**: Secure, authenticated EPUB book content streaming.
8. **`operations`**: Operational health probes, transactional event publications, security audit events.

---

## Architectural Rules

- **Module Encapsulation**: Each business module owns its domain model, repositories, and Flyway migrations under `dev.samster.granthalay.<module>`.
- **Transaction Boundaries**: Declared at application use-case services.
- **Adapter Boundary**: External cloud services (S3/GCS object storage, Stripe, SendGrid) are placed behind interfaces owned by the consuming module.
- **Verification**: Modulith verification tests (`ModuleArchitectureTests`) check package boundaries and dependency allowlists on every build.
