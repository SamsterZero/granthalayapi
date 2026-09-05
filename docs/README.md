# Granthalay API Documentation

Welcome to the **Granthalay API** technical documentation suite. This repository contains the Spring Boot modular monolith backend for the [Granthalay static PWA](https://github.com/SamsterZero/Granthalay).

---

## Documentation Index

| Guide | Description | Key Topics |
| :--- | :--- | :--- |
| **[01-vision.md](01-vision.md)** | Product Vision & Boundaries | Core philosophy, local-first reader boundary, optional connected backend services |
| **[02-development.md](02-development.md)** | Development & Setup Guide | Local setup (Java 25, Spring Boot 4, Maven), Testcontainers, Podman socket configuration, code formatting |
| **[03-architecture.md](03-architecture.md)** | Modular Monolith Architecture | Spring Modulith boundaries, module allowlists, application APIs, domain events, publication registry |
| **[04-api-conventions.md](04-api-conventions.md)** | API Conventions & OpenAPI | REST `/api/v1` standards, RFC 9457 Problem Details, DTO isolation, TypeScript client generation, contract tests |
| **[05-data-and-privacy.md](05-data-and-privacy.md)** | Data Ownership & Privacy | Zero-logging policy, module schema ownership, Flyway forward-only migrations, JPA entity boundaries |
| **[06-deployment.md](06-deployment.md)** | Production Deployment Guide | OCI Kubernetes (OKE) manifests, Docker Compose VM stack, Caddy reverse proxy, network security, secret rotation, rollback |
| **[07-troubleshooting.md](07-troubleshooting.md)** | Troubleshooting & Diagnostics | Podman container cleanup, Testcontainers socket troubleshooting, Flyway migration resolution |
| **[08-modules-and-roadmap.md](08-modules-and-roadmap.md)** | Module Inventory & Roadmap | The 8 foundation modules (`identity`, `catalog`, `publishing`, `storage`, `commerce`, `entitlements`, `delivery`, `operations`), feature rollout |

---

## Source of Truth

- **Executable Code & Tests Outrank Plans**: Behavior verified by `./mvnw verify` is authoritative.
- **Documentation Hygiene**: Durable architectural decisions belong in `docs/`. Update documentation in the same pull request as code/contract changes.
