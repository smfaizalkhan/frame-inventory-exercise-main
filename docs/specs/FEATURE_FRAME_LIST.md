# Feature Spec: Frame List Page

## Business requirement
A user can see all frames in the inventory, navigate to add a new frame, and access individual frames to view or edit them.

---

## Route
`/frames`

This is the default landing page of the application. Redirect `/` → `/frames`.

---

## API contract

### `GET /api/v1/frames`

**Query parameters**

| Parameter | Type    | Default | Description                                      |
|-----------|---------|---------|--------------------------------------------------|
| `page`    | Integer | 0       | Zero-based page number                           |
| `size`    | Integer | 25      | Page size (max 100)                              |
| `sort`    | String  | `frameReference,asc` | Field and direction e.g. `status,desc` |
| `status`  | String  | (none)  | Filter by FrameStatus enum value                 |
| `mediaType` | String | (none) | Filter by MediaType enum value                  |
| `town`    | String  | (none)  | Case-insensitive partial match on location.town  |

**Success — 200 OK**
```json
{
  "content": [
    {
      "id": "6650a1f2e3b4c5d6e7f80001",
      "frameReference": "UK-0099001",
      "location": {
        "town": "London",
        "region": "Greater London",
        "country": "GB"
      },
      "site": { "siteType": "ROADSIDE" },
      "format": { "mediaType": "DIGITAL", "formatCode": "6S" },
      "lifecycle": { "status": "ACTIVE" },
      "commercials": { "premium": false }
    }
  ],
  "totalElements": 1312,
  "totalPages": 53,
  "page": 0,
  "size": 25
}
```

> Note: list response returns a **summary projection** — not every field. Full frame is fetched on the detail page.

---

## Backend — summary projection

Create a `FrameSummary` record (not the full `Frame`) returned by the list endpoint:

```java
record FrameSummary(
  String id,
  String frameReference,
  LocationSummary location,   // town, region, country only
  SiteSummary site,           // siteType only
  FormatSummary format,       // mediaType, formatCode only
  LifecycleSummary lifecycle, // status only
  CommercialsSummary commercials // premium only
)
```

Use a MongoDB projection query — do not fetch and map full documents for a list.

---

## Frontend behaviour

### Layout
- Full-width data table below the page header.
- Page header contains:
  - Title: "Frame Inventory"
  - Right-aligned CTA button: **"+ Add Frame"** → navigates to `/frames/new`
  - Right-aligned secondary button: **"Import CSV"** → navigates to `/frames/import`

### Table columns

| Column           | Field                        | Sortable |
|------------------|------------------------------|----------|
| Frame Ref        | `frameReference`             | Yes      |
| Town             | `location.town`              | Yes      |
| Region           | `location.region`            | No       |
| Type             | `site.siteType`              | Yes      |
| Format           | `format.formatCode`          | No       |
| Media            | `format.mediaType`           | Yes      |
| Status           | `lifecycle.status`           | Yes      |
| Premium          | `commercials.premium`        | No       |
| Actions          | —                            | No       |

### Actions column (per row)
- **"View"** link → `/frames/:id`
- **"Edit"** link → `/frames/:id/edit`

### Filters bar (above table)
Three inline filter controls:
- Status dropdown (All | ACTIVE | INACTIVE | UNDER_MAINTENANCE | DECOMMISSIONED)
- Media Type dropdown (All | CLASSIC | DIGITAL)
- Town search input (debounced 300 ms)

Changing any filter resets to page 0 and re-fetches.

### Pagination
- Show "Showing 1–25 of 1,312 frames" label.
- Previous / Next buttons.
- Page size selector: 25 / 50 / 100.

### Status badge colours
| Status              | Colour  |
|---------------------|---------|
| ACTIVE              | Green   |
| INACTIVE            | Grey    |
| UNDER_MAINTENANCE   | Amber   |
| DECOMMISSIONED      | Red     |

### Empty state
"No frames found. Try adjusting your filters or add the first frame."
With a "+ Add Frame" button centred below.

### Loading state
Show a skeleton table (5 placeholder rows) while fetching.

---

## Tests to add to TESTS.md

### `FrameListPage.test.tsx`
```
renders table with frame rows fetched from API
clicking column header sorts by that field
changing status filter re-fetches with status param
changing town filter debounces and re-fetches
clicking "View" navigates to /frames/:id
clicking "Edit" navigates to /frames/:id/edit
clicking "+ Add Frame" navigates to /frames/new
clicking "Import CSV" navigates to /frames/import
shows skeleton while loading
shows empty state when API returns zero results
pagination controls update page param on click
```

### Backend — `FrameListIntegrationTest`
```
GET /api/v1/frames

given_framesExist_when_get_then_200WithSummaryProjection
given_statusFilter_when_get_then_onlyMatchingFramesReturned
given_townFilter_when_get_then_caseInsensitivePartialMatch
given_sortByStatus_when_get_then_orderedCorrectly
given_noFrames_when_get_then_200WithEmptyContent
given_pageSizeOf2_when_get_then_returns2FramesAndCorrectTotals
```
