# Feature Spec: Edit a Frame

## Business requirement
A user can amend an existing frame's attributes and have the changes saved.

---

## API contract

### `GET /api/v1/frames/:id`
Fetch a single frame before populating the edit form.

**Success — 200 OK:** returns `FrameResponse` (full frame object).
**Not found — 404:** `ProblemDetail` with type `not-found`.

---

### `PUT /api/v1/frames/:id`
Replace the editable fields of an existing frame.

**Request body** (`UpdateFrameRequest`)
Same shape as `CreateFrameRequest` **except** `frameReference` is omitted — it is immutable after creation.

**Success — 200 OK:** returns updated `FrameResponse`.

**Not found — 404:** `ProblemDetail`
```json
{
  "type": "https://frame-inventory.io/errors/not-found",
  "title": "Frame Not Found",
  "status": 404,
  "detail": "No frame found with id '6650a1f2e3b4c5d6e7f80001'."
}
```

**Validation errors — 400:** same `ProblemDetail` shape as Add.

---

## Validation rules
Same as Add a Frame, minus `frameReference` uniqueness check (field not editable).

---

## Service behaviour

1. Load the existing `Frame` by id — throw `FrameNotFoundException` if missing.
2. Validate `UpdateFrameRequest`.
3. Compute a diff: collect only the fields whose values changed.
4. Apply the changes to the domain object via MapStruct.
5. Spring auditing updates `updatedAt`.
6. Save to MongoDB.
7. Write a `FrameHistory` record:
   - `changeType = UPDATED`
   - `before = { only changed fields, old values }`
   - `after  = { only changed fields, new values }`
8. Return updated `FrameResponse`.

---

## Diff computation rule
Compare `UpdateFrameRequest` field-by-field against the existing `Frame`.
Collect `{ fieldPath → [oldValue, newValue] }` pairs where values differ.
Nested fields use dot notation: `location.town`, `format.mediaType`, etc.

---

## Frontend behaviour

- Route: `/frames/:id/edit`
- On mount: `GET /api/v1/frames/:id` to populate the form.
- Show a loading skeleton while fetching.
- Form is identical to the Add form, but `frameReference` field is read-only (disabled input).
- On submit: `PUT /api/v1/frames/:id`.
- On 200: redirect to `/frames/:id` and show a success toast "Frame updated."
- On 400: display field-level errors inline.
- On 404: show a full-page error "This frame no longer exists."
- Dirty-state guard: if the user navigates away with unsaved changes, prompt with a confirmation dialog.
