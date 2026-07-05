# Feature Spec: Frame Detail Page

## Business requirement
A user can view the full details of a single frame, navigate to edit it, and see its audit history — all from one page.

---

## Route
`/frames/:id`

---

## API contract

Reuses the existing `GET /api/v1/frames/:id` endpoint (full `FrameResponse`).

---

## Frontend behaviour

### Page header
- Title: the frame's `frameReference` (e.g. "UK-0099001")
- Breadcrumb: "Frame Inventory → UK-0099001"
- Right-aligned **"Edit Frame"** button → navigates to `/frames/:id/edit`
- Right-aligned secondary **"← Back to list"** link → navigates to `/frames`

### Page body — two tabs

#### Tab 1: Details (default active tab)

Display all frame fields grouped into cards matching the domain model sections:

| Card title     | Fields shown                                                        |
|----------------|---------------------------------------------------------------------|
| Location       | Country, Region, Town, Postcode, Address, Lat/Lon, Distance to school |
| Site           | Site Type, Position                                                 |
| Format         | Media Type, Format Code, Dimensions, Pixels, Aspect Ratio, Illuminated, Slots, Size Group |
| Commercials    | Impact Weight, Pricing Grade, Production Rate Card, Premium         |
| Lifecycle      | Status (with badge), Status Reason, Created At, Updated At          |
| External Refs  | Broadsign ID, Pricing Ref, Linked Frame IDs                         |

- Null/empty fields show a "—" placeholder, not blank.
- `createdAt` and `updatedAt` formatted as "30 May 2024, 09:00".
- `linkedFrameIds` rendered as a comma-separated list of reference strings.

#### Tab 2: History

- Lazy-loaded on tab activation (do not fetch on page load).
- Renders the history timeline as specified in `FEATURE_FRAME_HISTORY.md`.

### Loading state
Show a full-page skeleton while the frame is being fetched.

### Error state
- **404:** show a centred message "Frame not found." with a "← Back to list" button.
- **Other errors:** show "Something went wrong. Please try again." with a retry button.

---

## Tests to add to TESTS.md

### `FrameDetailPage.test.tsx`
```
fetches frame on mount and displays frameReference as page title
renders all six detail cards with correct field values
null fields display "—" placeholder
"Edit Frame" button navigates to /frames/:id/edit
"Back to list" link navigates to /frames
history tab is not fetched on initial load
clicking History tab triggers history fetch and renders timeline
given 404 response, shows "Frame not found" error state
given network error, shows retry button
```
