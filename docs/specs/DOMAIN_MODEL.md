# Domain Model Specification

## Frame

The central aggregate. Represents a single physical out-of-home advertising surface.

### MongoDB collection: `frames`

```
Frame
├── id                  : String          — MongoDB ObjectId as hex
├── frameReference      : String          — unique human-readable identifier (e.g. "UK-0012345")
│
├── location
│   ├── country         : String          — ISO 3166-1 alpha-2 (e.g. "GB")
│   ├── region          : String
│   ├── town            : String
│   ├── postcode        : String
│   ├── address         : String
│   ├── latitude        : Double
│   ├── longitude       : Double
│   └── distanceToSchoolMetres : Integer  — nullable
│
├── site
│   ├── siteType        : SiteType        — enum (see below)
│   └── position        : String          — free text (e.g. "Facing North")
│
├── format
│   ├── mediaType       : MediaType       — enum: CLASSIC | DIGITAL
│   ├── formatCode      : String          — e.g. "48S", "6S"
│   ├── widthMm         : Integer         — nullable
│   ├── heightMm        : Integer         — nullable
│   ├── pixelWidth      : Integer         — nullable (digital only)
│   ├── pixelHeight     : Integer         — nullable (digital only)
│   ├── aspectRatio     : String          — nullable (e.g. "16:9")
│   ├── illuminated     : Boolean
│   ├── numberOfSlots   : Integer         — default 1 for classic
│   └── sizeGroup       : String          — e.g. "Large", "Small"
│
├── commercials
│   ├── impactWeight    : Double          — nullable
│   ├── pricingGrade    : String          — nullable
│   ├── productionRateCard : String       — nullable
│   └── premium         : Boolean         — default false
│
├── lifecycle
│   ├── status          : FrameStatus     — enum (see below)
│   ├── statusReason    : String          — nullable
│   ├── createdAt       : Instant         — set once on creation
│   └── updatedAt       : Instant         — updated on every save
│
└── externalRefs
    ├── broadsignId     : String          — nullable
    ├── pricingRef      : String          — nullable
    └── linkedFrameIds  : List<String>    — nullable, other frame references
```

### Enums

```java
enum FrameStatus   { ACTIVE, INACTIVE, UNDER_MAINTENANCE, DECOMMISSIONED }
enum MediaType     { CLASSIC, DIGITAL }
enum SiteType      {
  ROADSIDE, STATION, UNDERGROUND, AIRPORT,
  MALL, BUS_SHELTER, RETAIL, OTHER
}
```

---

## FrameHistory

Immutable audit record. One document written per change event.

### MongoDB collection: `frame_history`

```
FrameHistory
├── id          : String    — MongoDB ObjectId
├── frameId     : String    — references Frame.id
├── changedAt   : Instant
├── changeType  : ChangeType — enum: CREATED | UPDATED | CSV_IMPORTED
├── changedBy   : String    — currently hardcoded "system" (no auth in scope)
├── before      : Map<String,Object>  — snapshot of changed fields before (null for CREATED)
└── after       : Map<String,Object>  — snapshot of changed fields after
```

Only the fields that actually changed are written into `before`/`after` maps, not the entire document.

---

## Design rules

- `Frame` is the only aggregate; `FrameHistory` is append-only and never mutated.
- `frameReference` must be unique — enforce with a MongoDB unique index.
- `createdAt` and `updatedAt` are managed by Spring Data auditing (`@CreatedDate`, `@LastModifiedDate`).
- Spatial fields (`latitude`, `longitude`) must both be present or both absent — validate at service layer.
- CSV import maps legacy column names to this model; see `FEATURE_BULK_UPLOAD.md` for the column mapping.
