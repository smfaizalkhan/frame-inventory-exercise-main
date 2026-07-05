# Codex Agent Instructions

## Purpose
This file is automatically read by Codex at the start of every session.
It defines the working rules, verification steps, and spec references for the Frame Inventory project.

---

## Specs location
All specification files are in `specs/`. Always read the relevant spec(s) before writing any code.

| Spec file                        | Read when working on...                        |
|----------------------------------|------------------------------------------------|
| `specs/CODEX_INSTRUCTIONS.md`   | Every session — contains the build order       |
| `specs/TECH_STACK.md`           | Every session — coding rules and libraries     |
| `specs/DOMAIN_MODEL.md`         | Steps 2, 3, 4                                  |
| `specs/FEATURE_FRAME_LIST.md`   | Steps 10, 11, 21                               |
| `specs/FEATURE_ADD_FRAME.md`    | Steps 6, 7, 23                                 |
| `specs/FEATURE_EDIT_FRAME.md`   | Steps 8, 9, 24                                 |
| `specs/FEATURE_BULK_UPLOAD.md`  | Steps 12, 13, 14, 25                           |
| `specs/FEATURE_FRAME_HISTORY.md`| Steps 15, 16, 26                               |
| `specs/FEATURE_FRAME_DETAIL.md` | Step 26                                        |
| `specs/UI_SHELL.md`             | Steps 17, 18, 19, 20, 27                       |
| `specs/TESTS.md`                | Every step that involves writing tests         |

---

## Working directories

| Module   | Path            |
|----------|-----------------|
| Backend  | `src/backend/`  |
| Frontend | `src/frontend/` |
| Specs    | `specs/`        |

---

## After every backend change
1. Run `cd src/backend && ./gradlew clean build` from the repo root
2. Fix any compilation errors before finishing
3. Fix any test failures before finishing
4. Only mark a step complete when `BUILD SUCCESSFUL` is shown

## After every frontend change
1. Run `cd src/frontend && npm install && npm run build` from the repo root
2. Run `npm test -- --run` to execute Vitest tests
3. Fix any TypeScript errors or test failures before finishing
4. Only mark a step complete when the build and tests pass

---

## Code rules (summary — full detail in specs/TECH_STACK.md)

### Backend
- Controllers are thin — all logic goes in `*Service` classes
- Use MapStruct for all entity ↔ DTO mapping — no manual mapping
- Use records for all DTOs
- Validate all inbound requests with `@Valid`
- Return `ProblemDetail` for all error responses — never raw exception messages
- Use SLF4J for logging — never `System.out.println`
- No `@SuppressWarnings` without an explanatory comment

### Frontend
- No `any` TypeScript types — use `unknown` and narrow
- Components never call `fetch` directly — all API calls go through `src/api/framesApi.ts`
- All forms use React Hook Form + Zod validation
- Use design system tokens for all spacing, colour, and typography

---

## Test-first workflow (TDD)
For every step:
1. Write the failing test(s) first using the exact names from `specs/TESTS.md`
2. Write the implementation to make the tests pass
3. Run the build to verify
4. Do not skip writing tests — every service method must have a corresponding test

---

## What Codex must never do
- Add dependencies not in `specs/TECH_STACK.md` without a comment explaining why
- Write business logic in a controller or repository
- Use `any` in TypeScript
- Skip the test that corresponds to a service method
- Return a raw exception message to the client
- Use `System.out.println`
- Mark a step done without a passing build
