# Implementation Plan — Role & Dashboard Fix

Generated: 2026-09-15  
Status: IN PROGRESS

---

## Priority 1 — Fix login response parsing [BACKEND + FRONTEND] ✅ TODO
`authenticate()` in `api.ts` wraps the auth endpoint as `{data: User}` but the backend
returns a flat `AuthResponse`. `.data.data` resolves to `undefined`, so the user is never
stored and the JWT never saved — every protected call fails.

**Fix:** Update `authenticate()` and the `User` type to map `AuthResponse` fields directly.

---

## Priority 2 — Create Staff User flow [BACKEND + FRONTEND] ✅ TODO
`POST /api/admin/staff` only creates a `StaffProfile` record for an existing `userId`.
There is no way to create the `User` account (name / email / password / roles) from the UI.

**Backend:** New `POST /api/admin/users/staff` that in one transaction creates `User` +
assigns roles + creates `StaffProfile`.

**Frontend:** "Add Staff Member" modal in `Staff.tsx` with full name, email, temp password,
phone, job title, employment status, and role multi-select.

---

## Priority 3 — Customer ownership check [BACKEND] ✅ TODO
`ReservationController` guards routes with `hasRole('CUSTOMER')` but the service never
verifies `reservation.customerId == currentUserId`. Any authenticated customer can read or
modify any other customer's reservation by guessing an ID.

**Fix:** Inject `Authentication` in `ReservationService` methods `getById` and `update`,
compare `reservation.getCustomerId()` with the current user's ID, throw 403 if different.

---

## Priority 4 — Role-based sidebar filtering [FRONTEND] ✅ TODO
`AdminLayout` shows all 7 links to every staff role. A WAITER sees Inventory and Reports.

**Fix:** Map each role to allowed nav items; filter sidebar links from `user.roles`.

---

## Priority 5 — Role-based login redirect [FRONTEND] ✅ TODO
All staff land on `/admin` regardless of role. Spec requires:

```
ADMIN / MANAGER        -> /admin
EVENT_COORDINATOR      -> /admin/events
INVENTORY_MANAGER      -> /admin/inventory
CASHIER                -> /admin/cashier
KITCHEN_STAFF          -> /admin/kitchen
WAITER                 -> /admin/tables
CUSTOMER               -> /reservations
```

---

## Priority 6 — Enrich StaffProfileResponse [BACKEND] ✅ TODO
`StaffProfileResponse` has `employeeCode` and `jobTitle` but no `fullName`, `email`, or
`roles`. The frontend shows `EMP-XXXX` with no human identity.

**Fix:** Join `User` in `StaffService.listStaff()` and include `fullName`, `email`, `roles`
in the DTO.

---

## Priority 7 — Kitchen orders page [FRONTEND] ✅ TODO
`KitchenController` exists and is guarded for `KITCHEN_STAFF`. No frontend page exists.

**New page:** `src/pages/admin/Kitchen.tsx` — order queue with status transitions
RECEIVED → PREPARING → READY. Route: `/admin/kitchen`.

---

## Priority 8 — Cashier dashboard page [FRONTEND] ✅ TODO
`BillingController` exists and is guarded for `CASHIER`. No frontend page exists.

**New page:** `src/pages/admin/Cashier.tsx` — unpaid invoices list + payment recording.
Route: `/admin/cashier`.

---

## Priority 9 — Customer dashboard [FRONTEND] ✅ TODO
After login customers land on `/reservations` (list only). No dashboard with summary
widgets, quick actions, notifications.

**New page:** `src/pages/CustomerDashboard.tsx` — upcoming reservations, event bookings,
recent orders, notifications, quick action links. Route: `/dashboard`.

---

## Priority 10 — Missing staff endpoints [BACKEND] ✅ TODO
Spec requires but are absent:
- `GET  /api/admin/staff/{id}`
- `PATCH /api/admin/staff/{id}/roles`
- `POST  /api/admin/staff/{id}/reset-password`

---

## Completion Checklist

- [x] P1 — Login response parsing VERIFIED OK (backend wraps in ApiResponse, .data.data is correct)
- [x] P2 — Create staff user: backend `POST /api/admin/staff/users` (CreateStaffUserRequest → User + roles + StaffProfile in one transaction)
- [x] P2 — Create staff user: frontend "Add Staff Member" modal in Staff.tsx with full form + role picker
- [x] P3 — Customer ownership check VERIFIED already in ReservationService (lines 135-138, 145-148)
- [x] P4 — Role-based sidebar: AdminLayout filters nav links by user.roles
- [x] P5 — Role-based login redirect: Auth.tsx uses ROLE_REDIRECT priority table
- [x] P6 — StaffProfileResponse enriched: fullName, email, phone, roles added; StaffService joins User table
- [x] P7 — Kitchen orders page: src/pages/admin/Kitchen.tsx at route /admin/kitchen
- [x] P8 — Cashier dashboard: src/pages/admin/Cashier.tsx at route /admin/cashier
- [x] P9 — Customer dashboard: src/pages/CustomerDashboard.tsx at route /dashboard
- [x] P10 — GET /{id}, PATCH /{id}/roles, POST /{id}/reset-password added to StaffController + StaffService
