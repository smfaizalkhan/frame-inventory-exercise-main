# Feature Spec: Bulk Upload Frames from CSV

## Business requirement
A user can upload a CSV through the application and have the rows ingested into the inventory.
The supplied `data/inventory_frame.csv` (~1,300 rows) is the validation file for this feature.
This is a user-driven UI feature, not a one-off bootstrap script.

---

## API contract

### `POST /api/v1/frames/import`

- Content-Type: `multipart/form-data`
- Field name: `file`
- Accepted MIME types: `text/csv`, `application/vnd.ms-excel`
- Max file size: 10 MB

**Success — 200 OK**
```json
{
  "totalRows": 1312,
  "imported": 1298,
  "skipped": 10,
  "failed": 4,
  "errors": [
    { "row": 42,  "frameReference": "UK-0001234", "reason": "Duplicate frame reference" },
    { "row": 107, "frameReference": null,          "reason": "frameReference is blank" }
  ]
}
```

**Invalid file — 400 Bad Request** (`ProblemDetail`)
```json
{
  "type": "https://frame-inventory.io/errors/invalid-file",
  "title": "Invalid File",
  "status": 400,
  "detail": "Uploaded file is not a valid CSV or exceeds the 10 MB limit."
}
```

---

## CSV column mapping

Map legacy column names from `inventory_frame.csv` to the domain model:

| CSV column                  | Domain field                          | Notes                                |
|-----------------------------|---------------------------------------|--------------------------------------|
| `frame_reference`           | `frameReference`                      |                                      |
| `country`                   | `location.country`                    |                                      |
| `region`                    | `location.region`                     |                                      |
| `town`                      | `location.town`                       |                                      |
| `postcode`                  | `location.postcode`                   |                                      |
| `address`                   | `location.address`                    |                                      |
| `latitude`                  | `location.latitude`                   | Parse as Double; null if blank       |
| `longitude`                 | `location.longitude`                  | Parse as Double; null if blank       |
| `distance_to_school`        | `location.distanceToSchoolMetres`     | Parse as Integer; null if blank      |
| `site_type`                 | `site.siteType`                       | Uppercase, map to SiteType enum      |
| `position`                  | `site.position`                       |                                      |
| `media_type`                | `format.mediaType`                    | CLASSIC or DIGITAL                   |
| `format_code`               | `format.formatCode`                   |                                      |
| `width_mm`                  | `format.widthMm`                      | Parse as Integer; null if blank      |
| `height_mm`                 | `format.heightMm`                     | Parse as Integer; null if blank      |
| `pixel_width`               | `format.pixelWidth`                   | Parse as Integer; null if blank      |
| `pixel_height`              | `format.pixelHeight`                  | Parse as Integer; null if blank      |
| `aspect_ratio`              | `format.aspectRatio`                  |                                      |
| `illuminated`               | `format.illuminated`                  | "true"/"false" / "1"/"0" / "Y"/"N"  |
| `number_of_slots`           | `format.numberOfSlots`                | Default 1 if blank                   |
| `size_group`                | `format.sizeGroup`                    |                                      |
| `impact_weight`             | `commercials.impactWeight`            | Parse as Double; null if blank       |
| `pricing_grade`             | `commercials.pricingGrade`            |                                      |
| `production_rate_card`      | `commercials.productionRateCard`      |                                      |
| `premium`                   | `commercials.premium`                 | Same bool parse as `illuminated`     |
| `status`                    | `lifecycle.status`                    | Uppercase, map to FrameStatus enum   |
| `status_reason`             | `lifecycle.statusReason`              |                                      |
| `broadsign_id`              | `externalRefs.broadsignId`            |                                      |
| `pricing_ref`               | `externalRefs.pricingRef`             |                                      |
| `linked_frame_ids`          | `externalRefs.linkedFrameIds`         | Pipe-separated: "UK-001\|UK-002"     |

---

## Service behaviour

1. Validate the uploaded file (non-empty, valid CSV header, ≤ 10 MB).
2. Parse rows lazily using Apache Commons CSV (do not load all rows into memory at once).
3. For each row:
   a. Map CSV columns to `CreateFrameRequest` using the column mapping above.
   b. Run the same Jakarta validation as the Add a Frame feature.
   c. Check `frameReference` uniqueness against the DB.
   d. **If valid:** save the frame; write a `FrameHistory` record with `changeType = CSV_IMPORTED`.
   e. **If invalid or duplicate:** add to the errors list; increment `failed` counter; continue.
4. A row with a `frameReference` that already exists is counted as `skipped` (not an error) if the data is identical, and as `failed` if the data differs.
5. Return the `ImportResultResponse` summarising totals.

### Idempotency
Re-uploading the same CSV should produce 0 imported, N skipped (identical rows already exist), 0 failed.

---

## Frontend behaviour

- Route: `/frames/import`
- UI: a file drop-zone (drag-and-drop + click-to-browse) accepting `.csv` files.
- Before upload: display the selected file name and row count estimate (from file size).
- Progress indicator while the import is in flight (spinner or progress bar).
- On 200: show a results summary card:
  - Green: "1,298 frames imported"
  - Amber: "10 rows skipped (already exist)"
  - Red: "4 rows failed" — expandable list showing row number, frame reference, and reason.
- On 400: show error toast "The file is not a valid CSV or exceeds 10 MB."
- "Import another file" button resets the view.
