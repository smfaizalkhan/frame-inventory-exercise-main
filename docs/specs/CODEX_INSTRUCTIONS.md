# Codex Instructions — Frame Inventory

## How to use these specs

This repository is driven by specification files. Before writing any code, read the relevant spec.
The specs are the source of truth — do not invent behaviour not described in them.

## Spec files index

| File                       | Purpose                                                        |
|----------------------------|----------------------------------------------------------------|
| `TECH_STACK.md`            | Language, framework, library, and coding-style decisions       |
| `DOMAIN_MODEL.md`          | MongoDB document shapes, enums, and design rules               |
| `FEATURE_FRAME_LIST.md`    | Frame list page, filters, pagination, summary projection API   |
| `FEATURE_ADD_FRAME.md`     | API contract, validation, service steps, and UI for Add        |
| `FEATURE_EDIT_FRAME.md`    | API contract, validation, service steps, and UI for Edit       |
| `FEATURE_BULK_UPLOAD.md`   | API contract, CSV column mapping, service steps, and UI        |
| `FEATURE_FRAME_HISTORY.md` | API contract, service steps, and UI for audit trail            |
| `FEATURE_FRAME_DETAIL.md`  | Frame detail page: field cards, Edit button, History tab       |
| `UI_SHELL.md`              | Sidebar layout, full route table, API layer, shared components |
| `TESTS.md`                 | Full test inventory with method names for all layers           |

---

## Workflow for every task

1. **Read the spec first.** Identify which spec files are relevant to the task.
2. **Write the test(s) first.** Use the exact test names from `TESTS.md`. Tests should fail initially.
3. **Write the implementation** to make the tests pass.
4. **Do not add behaviour beyond the spec** — if something seems missing, add a `// TODO: spec needed` comment.
5. **Follow coding rules** in `TECH_STACK.md` without exception.

---

## Suggested build order

Follow this sequence to avoid circular dependencies:

```
── INFRASTRUCTURE ──────────────────────────────────────────
1.  Docker Compose cleanup      — remove MariaDB + OpenSearch services and volumes

── BACKEND ─────────────────────────────────────────────────
2.  Domain model                — Frame + FrameHistory documents + enums (DOMAIN_MODEL.md)
3.  Repositories                — FrameRepository + FrameHistoryRepository
4.  Repository tests            — @DataMongoTest (TESTS.md)
5.  FrameDiffService            — pure diff logic + unit tests
6.  FrameService (Add)          — FEATURE_ADD_FRAME.md + unit tests
7.  FrameController (Add)       — POST /api/v1/frames + integration test
8.  FrameService (Edit)         — FEATURE_EDIT_FRAME.md + unit tests
9.  FrameController (Edit)      — PUT /api/v1/frames/:id + integration test
10. FrameService (List)         — FEATURE_FRAME_LIST.md summary projection + unit tests
11. FrameController (List)      — GET /api/v1/frames + integration test
12. CsvRowMapper                — FEATURE_BULK_UPLOAD.md column mapping + unit tests
13. CsvImportService            — bulk upload logic + unit tests
14. FrameController (Import)    — POST /api/v1/frames/import + integration test
15. FrameHistoryService         — FEATURE_FRAME_HISTORY.md + unit tests
16. FrameController (History)   — GET /api/v1/frames/:id/history + integration test

── FRONTEND ────────────────────────────────────────────────
17. api/types.ts                — all TypeScript types (UI_SHELL.md)
18. api/framesApi.ts            — all typed fetch functions (UI_SHELL.md)
19. Shared components           — StatusBadge, ConfirmDialog, ErrorState, LoadingSkeleton
20. AppShell + routing          — sidebar, App.tsx route table (UI_SHELL.md) + tests
21. FrameListPage               — table, filters, pagination (FEATURE_FRAME_LIST.md) + tests
22. FrameForm (shared)          — React Hook Form + Zod schema (used by Add and Edit)
23. AddFramePage                — wraps FrameForm, POST on submit + tests
24. EditFramePage               — wraps FrameForm, PUT on submit, dirty guard + tests
25. CsvImportPage               — drop-zone, results summary (FEATURE_BULK_UPLOAD.md) + tests
26. FrameDetailPage             — field cards, Edit button, History tab + tests
27. NotFoundPage                — catch-all 404 + tests
```

---

## What Codex must never do

- Add dependencies not listed in `TECH_STACK.md` without a comment explaining why.
- Write business logic in a controller or repository.
- Use `any` in TypeScript.
- Suppress `@SuppressWarnings` without a comment.
- Skip writing the test that corresponds to each service method.
- Return a raw exception message to the client — always wrap in `ProblemDetail`.
- Use `System.out.println` — use SLF4J `log.info / log.error`.
