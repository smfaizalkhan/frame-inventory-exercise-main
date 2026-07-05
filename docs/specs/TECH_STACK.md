# Tech Stack Specification

## Overview
This document defines the authoritative technology choices for the Frame Inventory service.
Codex must follow these decisions exactly and must not introduce alternatives unless explicitly noted.

---

## Backend

| Concern             | Choice                                    |
|---------------------|-------------------------------------------|
| Language            | Java 21                                   |
| Framework           | Spring Boot 3.5                           |
| Build tool          | Gradle (Kotlin DSL)                       |
| Database            | MongoDB (via Spring Data MongoDB)         |
| Object mapping      | MapStruct                                 |
| Validation          | Jakarta Bean Validation (`@Valid`)        |
| Audit               | Spring Data `@EnableMongoAuditing`        |
| CSV parsing         | Apache Commons CSV                        |
| Logging             | SLF4J + Logback (Spring Boot default)     |
| API style           | REST/JSON, versioned under `/api/v1`      |

### Backend rules
- All domain logic lives in `*Service` classes — controllers are thin.
- No business logic in repositories or controllers.
- Use records for immutable DTOs (request/response).
- Validate all inbound DTOs at the controller boundary with `@Valid`.
- Map between domain objects and DTOs with MapStruct — no manual `new DTO(entity.getField())` chains.
- Every public service method must have a Javadoc comment.
- Return `ProblemDetail` (RFC 9457) for all error responses.
- Use `@Transactional` only where multiple writes must be atomic.

---

## Frontend

| Concern             | Choice                                    |
|---------------------|-------------------------------------------|
| Language            | TypeScript (strict mode)                  |
| Framework           | React 19                                  |
| Build tool          | Vite                                      |
| Component library   | Project design system (`design-system/`)  |
| HTTP client         | Fetch API wrapped in typed service layer  |
| Form management     | React Hook Form + Zod validation          |
| State               | React built-ins (useState, useReducer, Context) — no Redux |
| Routing             | React Router v7                           |
| Testing             | Vitest + React Testing Library            |

### Frontend rules
- Every component that accepts props must have a TypeScript interface defined above it.
- No `any` types anywhere — use `unknown` and narrow.
- API calls are isolated in `src/api/` — components never call `fetch` directly.
- All user-visible strings are in component files (no separate i18n layer needed).
- Use the design system tokens for all spacing, colour, and typography — no raw CSS values.
- Forms submit only after client-side Zod validation passes.

---

## Infrastructure

| Service               | Purpose                                   |
|-----------------------|-------------------------------------------|
| MongoDB               | Primary data store for frames + history   |
| OpenSearch            | Optional full-text search (stretch goal)  |

### Docker Compose rules
- MariaDB service and its volume must be removed — MongoDB is the only datastore.
- Each service must define a `healthcheck`.
- Backend must declare `depends_on: mongodb: condition: service_healthy`.

---

## Cross-cutting

- **Error handling:** backend returns `ProblemDetail`; frontend maps HTTP error bodies to user-visible toast messages.
- **Timestamps:** all timestamps stored and transmitted as ISO-8601 UTC strings.
- **IDs:** MongoDB `ObjectId`, exposed as hex strings in the API.
- **CORS:** backend permits `http://localhost:3000` in local profile only.
- **Observability:** Spring Boot Actuator enabled; `/api/actuator/health` and `/api/actuator/info` exposed.
