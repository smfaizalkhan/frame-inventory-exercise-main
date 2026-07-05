# Feature Spec: Add a Frame

## Business requirement
A user can create a new frame through the application and see it persisted in the inventory.

---

## API contract

### `POST /api/v1/frames`

**Request body** (`CreateFrameRequest`)
```json
{
  "frameReference": "UK-0099001",
  "location": {
    "country": "GB",
    "region": "Greater London",
    "town": "London",
    "postcode": "EC1A 1BB",
    "address": "123 High Street",
    "latitude": 51.5074,
    "longitude": -0.1278,
    "distanceToSchoolMetres": 350
  },
  "site": {
    "siteType": "ROADSIDE",
    "position": "Facing North"
  },
  "format": {
    "mediaType": "DIGITAL",
    "formatCode": "6S",
    "widthMm": 1800,
    "heightMm": 1200,
    "pixelWidth": 1920,
    "pixelHeight": 1080,
    "aspectRatio": "16:9",
    "illuminated": true,
    "numberOfSlots": 6,
    "sizeGroup": "Large"
  },
  "commercials": {
    "impactWeight": 1.2,
    "pricingGrade": "A",
    "productionRateCard": "Standard",
    "premium": false
  },
  "lifecycle": {
    "status": "ACTIVE",
    "statusReason": null
  },
  "externalRefs": {
    "broadsignId": "BS-4421",
    "pricingRef": null,
    "linkedFrameIds": []
  }
}
```

**Success — 201 Created**
```json
{
  "id": "6650a1f2e3b4c5d6e7f80001",
  "frameReference": "UK-0099001",
  "...": "full frame fields"
}
```

**Validation errors — 400 Bad Request** (`ProblemDetail`)
```json
{
  "type": "https://frame-inventory.io/errors/validation",
  "title": "Validation Failed",
  "status": 400,
  "detail": "One or more fields failed validation.",
  "errors": {
    "frameReference": "must not be blank",
    "location.country": "must not be blank"
  }
}
```

**Duplicate frameReference — 409 Conflict** (`ProblemDetail`)
```json
{
  "type": "https://frame-inventory.io/errors/duplicate",
  "title": "Duplicate Frame Reference",
  "status": 409,
  "detail": "A frame with reference 'UK-0099001' already exists."
}
```

---

## Validation rules

| Field                        | Rule                                             |
|------------------------------|--------------------------------------------------|
| `frameReference`             | Not blank, max 50 chars, unique                  |
| `location.country`           | Not blank, 2-char ISO code                       |
| `location.latitude`          | Between -90 and 90 (if provided)                 |
| `location.longitude`         | Between -180 and 180 (if provided)               |
| `location.latitude/longitude`| Both present or both absent                      |
| `site.siteType`              | Must be a valid `SiteType` enum value            |
| `format.mediaType`           | Must be a valid `MediaType` enum value           |
| `format.numberOfSlots`       | Min 1                                            |
| `lifecycle.status`           | Must be a valid `FrameStatus` enum value         |

---

## Service behaviour

1. Validate the request (Jakarta validation + cross-field checks).
2. Check `frameReference` uniqueness — throw `DuplicateFrameReferenceException` if taken.
3. Map `CreateFrameRequest` → `Frame` domain object via MapStruct.
4. Set `lifecycle.createdAt` and `lifecycle.updatedAt` via Spring auditing.
5. Save to MongoDB.
6. Write a `FrameHistory` record with `changeType = CREATED`, `before = null`, `after = full frame snapshot`.
7. Return the saved frame mapped to `FrameResponse`.

---

## Frontend behaviour

- Route: `/frames/new`
- Form sections match domain groups: Location, Site, Format, Commercials, Lifecycle, External Refs.
- Client-side Zod schema mirrors the validation rules above.
- On submit: POST to `/api/v1/frames`.
- On 201: redirect to `/frames/:id` (frame detail page) and show a success toast.
- On 400: display field-level errors inline next to each input.
- On 409: show an error toast "A frame with this reference already exists."
- Submit button disabled while request is in flight.
