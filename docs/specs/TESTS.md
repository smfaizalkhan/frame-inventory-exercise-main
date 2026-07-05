# Test Specification

## Philosophy

- Tests are the living specification. If behaviour isn't tested, it isn't specified.
- Test names follow the pattern: `given_[context]_when_[action]_then_[outcome]`.
- No mocking of the database in integration tests — use an in-process MongoDB (Flapdoodle).
- Frontend tests do not mock the network at the component level — use MSW (Mock Service Worker).

---

## Backend — Unit Tests

Location: `src/backend/src/test/java/...`
Framework: JUnit 5 + Mockito + AssertJ

---

### `FrameServiceTest`

Test the service in isolation. Mock `FrameRepository`, `FrameHistoryRepository`, and `FrameDiffService`.

#### Add a frame

```
given_validCreateRequest_when_createFrame_then_frameIsSavedAndHistoryWritten
  - verify frameRepository.save() is called once
  - verify historyRepository.save() called with changeType=CREATED, before=null

given_duplicateFrameReference_when_createFrame_then_throwsDuplicateFrameReferenceException
  - stub frameRepository.existsByFrameReference() to return true
  - expect DuplicateFrameReferenceException

given_latitudeWithoutLongitude_when_createFrame_then_throwsValidationException
  - both coordinates must be present or both absent
```

#### Edit a frame

```
given_validUpdateRequest_when_updateFrame_then_frameIsSavedAndDiffHistoryWritten
  - verify only changed fields appear in FrameHistory.before and .after

given_noFieldsChanged_when_updateFrame_then_noHistoryRecordWritten
  - identical request to existing state → history NOT written, frame saved with updated timestamp

given_unknownId_when_updateFrame_then_throwsFrameNotFoundException
```

#### CSV import

```
given_validCsvWithAllRows_when_importCsv_then_allRowsImported
  - 5-row in-memory CSV, all valid → ImportResult.imported = 5

given_csvWithDuplicateReference_when_importCsv_then_rowIsSkipped
  - pre-existing frame with same reference, identical data → skipped, not failed

given_csvWithDuplicateReferenceAndDifferentData_when_importCsv_then_rowIsFailed

given_csvWithInvalidRow_when_importCsv_then_rowIsFailedAndOtherRowsStillImported
  - malformed latitude on row 3 → row 3 failed, rows 1-2 and 4-5 imported

given_emptyFile_when_importCsv_then_throwsInvalidFileException

given_fileLargerThan10Mb_when_importCsv_then_throwsInvalidFileException
```

#### Frame history

```
given_existingFrame_when_getHistory_then_returnsEventsInReverseChronologicalOrder

given_unknownFrameId_when_getHistory_then_throwsFrameNotFoundException
```

---

### `FrameDiffServiceTest`

Tests for the diff computation logic (isolated, no mocks needed).

```
given_identicalObjects_when_computeDiff_then_returnsEmptyMap

given_oneFieldChanged_when_computeDiff_then_returnsOnlyThatField
  - change location.town from "Leeds" to "London" → diff = { "location.town": ["Leeds", "London"] }

given_nestedFieldChanged_when_computeDiff_then_useDotNotation

given_nullBecomingNonNull_when_computeDiff_then_captured
  - statusReason changes from null to "Planned maintenance"

given_nonNullBecomingNull_when_computeDiff_then_captured
```

---

### `CsvRowMapperTest`

Tests for the legacy CSV column → domain mapping logic.

```
given_validRow_when_map_then_allFieldsMapped

given_blankLatitude_when_map_then_latitudeIsNull

given_illuminatedAsY_when_map_then_illuminatedIsTrue
given_illuminatedAs1_when_map_then_illuminatedIsTrue
given_illuminatedAsFalse_when_map_then_illuminatedIsFalse

given_pipeSeparatedLinkedFrameIds_when_map_then_listParsedCorrectly

given_unknownSiteType_when_map_then_rowFailsWithClearReason

given_blankNumberOfSlots_when_map_then_defaultsToOne
```

---

## Backend — Repository Tests

Location: `src/backend/src/test/java/.../repository/`
Framework: `@DataMongoTest` + Flapdoodle embedded MongoDB

---

### `FrameRepositoryTest`

```
given_savedFrame_when_findById_then_frameReturned

given_duplicateFrameReference_when_save_then_throwsDuplicateKeyException
  - relies on unique index being present in the test context

given_noFrames_when_findAll_then_returnsEmptyPage

given_multipleFrames_when_findAll_then_respectsPageSizeAndOrder
```

### `FrameHistoryRepositoryTest`

```
given_multipleHistoryRecords_when_findByFrameIdOrderByChangedAtDesc_then_returnsMostRecentFirst

given_unknownFrameId_when_findByFrameId_then_returnsEmptyPage
```

---

## Backend — Integration Tests

Location: `src/backend/src/test/java/.../integration/`
Framework: `@SpringBootTest(webEnvironment = RANDOM_PORT)` + Flapdoodle + RestAssured

One base class `BaseIntegrationTest` starts the context and wipes MongoDB collections `@BeforeEach`.

---

### `AddFrameIntegrationTest`

```
POST /api/v1/frames

given_validPayload_when_post_then_201WithFrameResponse
given_missingFrameReference_when_post_then_400WithValidationError
given_duplicateFrameReference_when_post_then_409WithProblemDetail
given_invalidSiteTypeEnum_when_post_then_400
given_latitudeWithoutLongitude_when_post_then_400
```

### `EditFrameIntegrationTest`

```
PUT /api/v1/frames/:id

given_existingFrame_when_put_then_200WithUpdatedFrame
given_unknownId_when_put_then_404WithProblemDetail
given_invalidPayload_when_put_then_400WithFieldErrors
```

### `BulkUploadIntegrationTest`

```
POST /api/v1/frames/import (multipart)

given_validCsvFile_when_post_then_200WithImportResult
given_nonCsvFile_when_post_then_400
given_csvWithMixedValidAndInvalidRows_when_post_then_200WithPartialResult
given_reuploadOfSameCsv_when_post_then_allSkipped
```

### `FrameHistoryIntegrationTest`

```
GET /api/v1/frames/:id/history

given_frameWithHistory_when_get_then_200WithEventsDescending
given_unknownFrameId_when_get_then_404
given_pageSize2_when_get_then_returns2Events
```

---

## Frontend — Component Tests

Location: `src/frontend/src/**/*.test.tsx`
Framework: Vitest + React Testing Library + MSW

---

### `AddFrameForm.test.tsx`

```
renders all form sections (Location, Site, Format, Commercials, Lifecycle)
submit button is disabled while request is in flight
given valid input, submits POST and redirects to frame detail on 201
given server 400, displays field-level error messages inline
given server 409, shows toast "A frame with this reference already exists."
frameReference field accepts alphanumeric and hyphens
```

### `EditFrameForm.test.tsx`

```
fetches frame on mount and populates form fields
frameReference input is disabled
given valid change, submits PUT and redirects on 200
shows dirty-state confirmation dialog when navigating away with unsaved changes
given server 404, shows full-page error
```

### `CsvImport.test.tsx`

```
renders file drop-zone
accepts .csv files via click-to-browse
rejects non-csv files with an error message
shows spinner while upload is in flight
given server 200, displays imported / skipped / failed summary card
given failed rows, shows expandable error list with row number and reason
"Import another file" button resets the view
```

### `FrameHistory.test.tsx`

```
does not fetch history on page load — only on tab activation
renders timeline with CREATED, UPDATED, CSV_IMPORTED badges
UPDATED event shows before/after diff with field names
clicking an event expands the diff detail
renders pagination controls when totalEvents > pageSize
shows empty state "No history found" when events list is empty
```

---

## Test data helpers

Create a shared `TestFixtures` class (backend) and `factories.ts` (frontend) providing:
- `aValidCreateFrameRequest()` — fully populated, overridable per test
- `aFrame()` — saved domain object
- `aFrameHistory(changeType)` — history record
- `aCsvRow(overrides)` — single CSV row map for unit tests
