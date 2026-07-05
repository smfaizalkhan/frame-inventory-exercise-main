# UI Shell & Routing Specification

## Overview
Defines the application shell (navigation, layout wrapper) and the complete client-side routing table. Codex must wire `App.tsx` and the layout component exactly from this spec.

---

## Route table

| Path                  | Component            | Page title              | Notes                            |
|-----------------------|----------------------|-------------------------|----------------------------------|
| `/`                   | —                    | —                       | Redirect → `/frames`             |
| `/frames`             | `FrameListPage`      | Frame Inventory         | Default landing page             |
| `/frames/new`         | `AddFramePage`       | Add Frame               |                                  |
| `/frames/import`      | `CsvImportPage`      | Import Frames           |                                  |
| `/frames/:id`         | `FrameDetailPage`    | `<frameReference>`      | Title set dynamically            |
| `/frames/:id/edit`    | `EditFramePage`      | Edit `<frameReference>` | Title set dynamically            |
| `*`                   | `NotFoundPage`       | Page Not Found          | Catch-all 404                    |

---

## Application shell layout

```
┌─────────────────────────────────────────────────────┐
│  SIDEBAR (fixed left, 220px wide)                   │
│  ┌─────────────────────────────────────────────┐    │
│  │  [Logo / App name: "Frame Inventory"]        │    │
│  │                                             │    │
│  │  Nav links:                                 │    │
│  │    📋  Frames          → /frames            │    │
│  │    ⬆️  Import CSV      → /frames/import     │    │
│  └─────────────────────────────────────────────┘    │
│                                                     │
│  MAIN CONTENT AREA (fluid, scrollable)              │
│  ┌─────────────────────────────────────────────┐    │
│  │  <Outlet /> (React Router)                  │    │
│  └─────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────┘
```

- Sidebar is always visible on desktop (≥ 768px).
- On mobile (< 768px): sidebar collapses to a hamburger menu button in a top bar.
- Active nav link is highlighted using the design system's active state token.
- No top navigation bar on desktop — sidebar handles all navigation.

---

## Component file structure

```
src/
├── App.tsx                          ← router setup, wraps AppShell
├── AppShell.tsx                     ← sidebar + <Outlet />
├── api/
│   ├── framesApi.ts                 ← all frames API calls
│   └── types.ts                     ← shared API response types
├── components/
│   ├── StatusBadge.tsx              ← reusable status colour badge
│   ├── MediaTypeBadge.tsx           ← CLASSIC / DIGITAL badge
│   ├── ChangeTypeBadge.tsx          ← CREATED / UPDATED / CSV_IMPORTED badge
│   ├── ConfirmDialog.tsx            ← dirty-state navigation guard
│   ├── ErrorState.tsx               ← reusable error + retry UI
│   └── LoadingSkeleton.tsx          ← reusable skeleton rows
├── pages/
│   ├── FrameListPage.tsx
│   ├── FrameDetailPage.tsx
│   ├── AddFramePage.tsx
│   ├── EditFramePage.tsx
│   ├── CsvImportPage.tsx
│   └── NotFoundPage.tsx
└── forms/
    ├── FrameForm.tsx                ← shared form used by Add and Edit
    ├── frameFormSchema.ts           ← Zod schema for frame form
    └── FrameForm.test.tsx
```

---

## `api/framesApi.ts` — functions to implement

All functions are async, return typed responses, and throw a typed `ApiError` on non-2xx responses.

```typescript
// List
getFrames(params: FrameListParams): Promise<PagedResponse<FrameSummary>>

// Single
getFrame(id: string): Promise<FrameResponse>

// Create
createFrame(data: CreateFrameRequest): Promise<FrameResponse>

// Update
updateFrame(id: string, data: UpdateFrameRequest): Promise<FrameResponse>

// History
getFrameHistory(id: string, page: number, size: number): Promise<FrameHistoryResponse>

// Import
importFramesCsv(file: File): Promise<ImportResultResponse>
```

---

## `api/types.ts` — types to define

```typescript
// Mirror of backend FrameStatus enum
type FrameStatus = 'ACTIVE' | 'INACTIVE' | 'UNDER_MAINTENANCE' | 'DECOMMISSIONED'

// Mirror of backend MediaType enum
type MediaType = 'CLASSIC' | 'DIGITAL'

// Mirror of backend SiteType enum
type SiteType = 'ROADSIDE' | 'STATION' | 'UNDERGROUND' | 'AIRPORT' | 'MALL' | 'BUS_SHELTER' | 'RETAIL' | 'OTHER'

// Mirror of backend ChangeType enum
type ChangeType = 'CREATED' | 'UPDATED' | 'CSV_IMPORTED'

interface ApiError {
  status: number
  title: string
  detail: string
  errors?: Record<string, string>  // field-level validation errors
}

interface PagedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  page: number
  size: number
}

interface FrameSummary { ... }     // from FEATURE_FRAME_LIST.md
interface FrameResponse { ... }    // full frame, mirrors domain model
interface CreateFrameRequest { ... }
interface UpdateFrameRequest { ... }
interface FrameHistoryResponse { ... }
interface ImportResultResponse { ... }
```

---

## Shared components

### `StatusBadge`
```tsx
<StatusBadge status="ACTIVE" />
// Renders a pill badge with the colour from FEATURE_FRAME_LIST.md status table
```

### `ConfirmDialog`
Used in `EditFramePage` when the user navigates away with unsaved changes.
```tsx
<ConfirmDialog
  open={isDirty}
  title="Unsaved changes"
  message="You have unsaved changes. Are you sure you want to leave?"
  onConfirm={() => navigate(-1)}
  onCancel={() => {}}
/>
```

### `ErrorState`
```tsx
<ErrorState
  message="Frame not found."
  action={{ label: "← Back to list", onClick: () => navigate('/frames') }}
/>
```

---

## `App.tsx` wiring

```tsx
<BrowserRouter>
  <Routes>
    <Route path="/" element={<Navigate to="/frames" replace />} />
    <Route element={<AppShell />}>
      <Route path="/frames"            element={<FrameListPage />} />
      <Route path="/frames/new"        element={<AddFramePage />} />
      <Route path="/frames/import"     element={<CsvImportPage />} />
      <Route path="/frames/:id"        element={<FrameDetailPage />} />
      <Route path="/frames/:id/edit"   element={<EditFramePage />} />
    </Route>
    <Route path="*" element={<NotFoundPage />} />
  </Routes>
</BrowserRouter>
```

---

## Tests to add to TESTS.md

### `AppShell.test.tsx`
```
renders sidebar with Frames and Import CSV nav links
active route link is highlighted
clicking Frames link navigates to /frames
clicking Import CSV link navigates to /frames/import
on mobile viewport, sidebar is hidden and hamburger button is visible
clicking hamburger opens the sidebar
```

### `NotFoundPage.test.tsx`
```
renders "Page Not Found" message
renders link back to /frames
```
