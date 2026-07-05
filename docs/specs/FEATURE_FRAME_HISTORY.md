# Feature Spec: View a Frame's History

## Business requirement
A user can see the audit trail of changes to any given frame.

---

## API contract

### `GET /api/v1/frames/:id/history`

Returns the audit trail for a frame in reverse-chronological order (most recent first).

**Query parameters**

| Parameter | Type    | Default | Description              |
|-----------|---------|---------|--------------------------|
| `page`    | Integer | 0       | Zero-based page number   |
| `size`    | Integer | 20      | Page size (max 100)      |

**Success — 200 OK**
```json
{
  "frameId": "6650a1f2e3b4c5d6e7f80001",
  "frameReference": "UK-0099001",
  "totalEvents": 5,
  "page": 0,
  "size": 20,
  "events": [
    {
      "id": "6650b2f3e3b4c5d6e7f80002",
      "changedAt": "2024-06-01T14:32:00Z",
      "changeType": "UPDATED",
      "changedBy": "system",
      "before": {
        "lifecycle.status": "INACTIVE"
      },
      "after": {
        "lifecycle.status": "ACTIVE"
      }
    },
    {
      "id": "6650a1f2e3b4c5d6e7f80001",
      "changedAt": "2024-05-30T09:00:00Z",
      "changeType": "CREATED",
      "changedBy": "system",
      "before": null,
      "after": {
        "frameReference": "UK-0099001",
        "lifecycle.status": "ACTIVE",
        "...": "all fields at creation"
      }
    }
  ]
}
```

**Not found — 404:** `ProblemDetail` (frame id does not exist).

---

## Service behaviour

1. Verify the frame exists by id — throw `FrameNotFoundException` if not.
2. Query `frame_history` collection where `frameId = :id`, sorted by `changedAt DESC`.
3. Apply pagination.
4. Map to `FrameHistoryResponse`.

---

## Repository

```java
Page<FrameHistory> findByFrameIdOrderByChangedAtDesc(String frameId, Pageable pageable);
```

---

## Frontend behaviour

- Displayed as a tab or section on the frame detail page (`/frames/:id`).
- Timeline layout: each event is a card showing:
  - Date/time (human-friendly: "1 Jun 2024, 14:32")
  - Change type badge: CREATED (blue) | UPDATED (amber) | CSV_IMPORTED (grey)
  - Changed by label ("system")
  - Expandable diff section:
    - For CREATED: show key creation fields (status, mediaType, location).
    - For UPDATED: show a two-column before/after diff for each changed field.
    - For CSV_IMPORTED: same as CREATED.
- Pagination controls at the bottom (Previous / Next).
- Empty state: "No history found for this frame." (should never occur in practice).
- Load history on tab activation, not on page load.
