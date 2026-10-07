# Six-member CRUD and validation review

Checked: 7 October 2026. Reviewed the current **final project** source, not the older member-package source.

Member ownership comes from the old project's `restaurant-event-member-packages/PACKAGE_MANIFEST.md`. The names below describe assigned modules; this report does not establish who authored each change.

## How to read this report

- **Create:** add a record.
- **Read:** view a list or one record.
- **Update:** change details or status.
- **Delete:** remove a record from use. In this project this often means marking it inactive or cancelled and keeping its history.
- **Frontend validation:** checks in the page before sending a request.
- **Backend validation:** checks in the Java request classes and services. These also apply when a request bypasses the page.
- **Present** means implemented in source; it does not mean every input and every screen has been tested.

## Quick member summary

| Member | Assigned area | Main CRUD result | Important limitation |
|---|---|---|---|
| Samarasinghe M.H.D. | Customer management, auth, notifications, reports | Customer account CRUD present, with deactivation | Notifications are marked read rather than deleted; reports are read-only |
| Ahamed M.I.I. | Events and billing | Hall and package CRUD present | Bookings can change status/cancel but cannot edit their date/hall/package; billing has separate limitations |
| Batagodage B.I. | Menu and orders | Menu item and category backend CRUD present | Category edit/delete have no controls in the current page; customers cannot edit/cancel submitted orders |
| Kodithuwakku U.A.A. | Table reservations | Table CRUD and reservation create/read/edit/cancel present | Cancellation text and status/date rules need more checks |
| Labijan L. | Inventory and supply | Inventory item and supplier CRUD present | Purchase order/delivery workflow is not implemented in these modules |
| Gunasekara M.N. | Staff and scheduling | Staff profile and shift CRUD present; assignment add/read/remove | Staff deletion does not disable login; attendance workflow is absent |

There is a main CRUD example for every member. There are still incomplete sub-features and validation gaps. Do not present this as “every feature is complete.”

## 1. Samarasinghe — customer management

| Operation | What exists |
|---|---|
| Create | Customer registration (`POST /api/auth/register`) |
| Read | Own profile; admin user list and user details |
| Update | Own name/phone/password; admin profile edit, suspension/reactivation and password reset |
| Delete | Admin deactivates a user (`DELETE /api/admin/users/{id}`); historical bookings/orders remain |

**Backend checks present:** required name/email/password, name up to 100 characters, email format and length, registration password 8–72 characters, optional phone must be empty or exactly 10 digits; duplicate email rejected; changing password needs the correct current password and a different new password; admin cannot suspend their own account. Reset tokens must be unused and unexpired, and the account must be active.

**Frontend checks present:** registration requires name/email/password; email input and minimum password length; profile forms and password confirmation; API errors are displayed.

**Other assigned features:** notifications list/unread count/mark-one-read/mark-all-read; ownership check prevents marking another customer's notification. Reports have admin/manager access and date-range checks. Notification creation is automatic through system events, not a customer create form. Reports do not need ordinary write CRUD.

**Gaps:** password change/admin reset/reset-token DTOs do not consistently enforce the same 72-character maximum as registration. The notifications `DELETE` endpoint only marks notifications read, so it is not real deletion. Password reset email is simulated in logs, not sent through a real email service.

References: [AuthController.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/auth/controller/AuthController.java:29>); [RegisterRequest.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/auth/dto/request/RegisterRequest.java:8>); [AdminUserController.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/users/controller/AdminUserController.java:28>); [UserService.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/users/service/UserService.java:47>); [PasswordResetService.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/auth/service/PasswordResetService.java:85>); [NotificationController.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/notifications/controller/NotificationController.java:54>); [ReportService.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/reports/service/ReportService.java:165>); [Profile.tsx](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-frontend/src/pages/Profile.tsx:8>).

## 2. Ahamed — events and billing

| Feature | Create | Read | Update | Delete / cancel |
|---|---|---|---|---|
| Event halls | Yes | Yes | Yes | Mark inactive, blocked if active bookings exist |
| Event packages | Yes | Yes | Yes, protected when active bookings exist | Mark inactive, blocked if active bookings exist |
| Event bookings | Yes | Own/list/detail and coordinator queue | Approve/reject status only | Customer cancel; history remains |
| Billing invoices | Yes | List/detail/own | Internal invoice recalculation/payment status | No public invoice delete/void endpoint |
| Legacy billing payments | Yes | Yes | No ordinary public edit | No public delete |

**Backend checks present:** future event date; start/end required and end after start; positive hall/package IDs; guest count 1–1000; active hall/package; guest count fits hall and package minimum/maximum; overlapping hall bookings rejected; only owner reads/cancels their booking; approval/rejection allowed only for pending bookings; rejection reason required and at most 500 characters. Hall/package names are checked for duplicates; hall capacity 1–1000; package price positive with two decimal places; maximum guests cannot be below minimum. Active bookings protect catalog deletion and incompatible edits.

**Frontend checks present:** booking form date/guest limits/hall selection/start/end; catalog create/edit/remove controls with required fields and numerical limits; API errors shown.

**Gaps:** no endpoint/form to edit a submitted booking's date, time, hall, package or guests. The legacy billing controller lets any authenticated user request invoice details/payment lists by ID without an owner or staff check. `createInvoice` does not validate that the selected customer/order/event IDs exist and match the invoice type, and can create a zero-total invoice. These billing gaps are separate from the newer customer-payment module.

References: [EventCatalogController.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/events/controller/EventCatalogController.java:15>); [EventBookingService.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/events/service/EventBookingService.java:47>); [EventCatalogService.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/events/service/EventCatalogService.java:20>); [CreateEventBookingRequest.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/events/dto/request/CreateEventBookingRequest.java:11>); [BillingController.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/billing/controller/BillingController.java:38>); [BillingService.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/billing/service/BillingService.java:40>); [EventCatalogManager.tsx](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-frontend/src/components/EventCatalogManager.tsx:7>); [Events.tsx](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-frontend/src/pages/Events.tsx:125>).

## 3. Batagodage — menu and orders

| Feature | Create | Read | Update | Delete / cancel |
|---|---|---|---|---|
| Menu items | Yes | Public catalog/detail and staff list | Details, price and availability | Mark inactive |
| Categories | Yes | Public category list | Backend endpoint present | Backend endpoint marks inactive |
| Orders | Customer places order | Own order list/detail and kitchen queue | Staff changes status | Staff status transition can cancel pending/preparing orders; no customer cancel endpoint |
| Food requests | Customer creates | Own requests and staff queue | Staff resolves | No delete endpoint |

**Backend checks present:** category/item name required and length-limited; duplicate category name rejected; category must be active; item price at least 0.01 with two decimal places; preparation time 1–240 minutes; order has at least one item; quantities 1–99; unavailable/inactive menu items cannot be ordered; price is calculated from the database instead of trusting a submitted price; order ownership; allowed status transitions only. Food-request message required and at most 500 characters, with customer/staff permissions.

**Frontend checks present:** menu item save validates name/category/positive price/two decimals/preparation time; image path check; required form fields; item edit/remove and availability controls; food ordering and kitchen status controls.

**Gaps:** category editing/deletion exists in Java but the current menu page only has Add category. The backend only checks image URL length, while the frontend checks URL/path format. Orders store submitted table/reservation IDs without verifying their existence or whether the reservation belongs to that customer. Submitted order items cannot be edited by the customer.

References: [AdminMenuController.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/menu/controller/AdminMenuController.java:32>); [CreateMenuItemRequest.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/menu/dto/request/CreateMenuItemRequest.java:10>); [MenuService.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/menu/service/MenuService.java:61>); [OrderService.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/orders/service/OrderService.java:38>); [OrderService.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/orders/service/OrderService.java:113>); [CreateOrderRequest.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/orders/dto/request/CreateOrderRequest.java:18>); [FoodRequestService.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/orders/service/FoodRequestService.java:29>); [Menu.tsx](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-frontend/src/pages/admin/Menu.tsx:40>).

## 4. Kodithuwakku — table reservations

| Feature | Create | Read | Update | Delete / cancel |
|---|---|---|---|---|
| Tables / dining spaces | Admin/manager adds | Catalog/admin list/detail | Details, capacity and status | Mark inactive; protected by current/upcoming reservations |
| Reservations | Customer or staff on behalf of customer | Own bookings/admin queue/detail/history | Customer edit; staff edit/reassign; confirm/check-in/complete/no-show | Cancel; preserve booking history and handle deposit |

**Backend checks present:** required date/time/table/contact; guest count 1–200; future reservation time in Asia/Colombo; start between 11:00 and 21:00; contact phone exactly 10 digits; name and notes length limits; active table/capacity; out-of-service tables rejected; overlapping slots rejected on creation and editing; customer ownership; only pending/confirmed reservations editable; lifecycle transitions checked; completed/checked-in/no-show bookings cannot be cancelled. Duplicate table number rejected; capacity cannot be reduced below an upcoming booking's guest count; reserved/occupied/upcoming-booked tables cannot be removed. Table photo must be HTTPS or a local `/images/` path.

**Frontend checks present:** shared reservation validator checks a real date, valid time, future slot, name, phone and integer guest count within table capacity. Customer and admin forms use it. Booking create/edit/cancel and table create/edit/remove controls exist.

**Gaps:** cancellation reason has no length annotation, and cancellation controllers do not use `@Valid`; oversized input is not cleanly rejected before saving. Edited seating preference lacks the create request's 50-character limit. Staff can directly change table status without checking a conflicting booking. Check-in/no-show transitions check status but not whether the reservation's date/start time has arrived. Table capacity limits differ: page max 30, backend max 200.

References: [AdminReservationController.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/reservations/controller/AdminReservationController.java:56>); [ReservationController.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/reservations/controller/ReservationController.java:84>); [ReservationService.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/reservations/service/ReservationService.java:347>); [ReservationService.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/reservations/service/ReservationService.java:234>); [TableService.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/reservations/service/TableService.java:80>); [CancelReservationRequest.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/reservations/dto/request/CancelReservationRequest.java:7>); [UpdateReservationRequest.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/reservations/dto/request/UpdateReservationRequest.java:22>); [validation.ts](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-frontend/src/services/validation.ts:2>); [Tables.tsx](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-frontend/src/pages/admin/Tables.tsx:131>).

## 5. Labijan — inventory and supply

| Feature | Create | Read | Update | Delete |
|---|---|---|---|---|
| Inventory items | Yes | List/detail/low-stock list | Item details, quantity and reorder level | Mark inactive |
| Suppliers | Yes, admin/manager | List/detail for allowed staff roles | Supplier details and active flag | Mark inactive |
| Stock movements | Automatically created during adjustments | Stored in database | No public edit | No public delete |

**Backend checks present:** required item name/unit with length limits; quantities/reorder levels non-negative and limited to three decimal places; adjustment must be non-zero; stock cannot fall below zero. Changing quantity through the editor records a movement. Supplier name/contact/address/products required; phone exactly 10 digits; valid email; joined date cannot be future; active flag required; role checks protect writes.

**Frontend checks present:** item name/unit/non-negative quantities; non-zero adjustment; supplier required fields/phone/email/date; create/edit/remove controls and confirmation.

**Gaps:** item and supplier duplicate names/emails are not rejected; decide with the team whether duplicates should be allowed. No implemented purchase-order, goods-receipt or supplier-to-stock link workflow in the inventory source. Low-stock appears on the page, but InventoryService does not call the low-stock notification factory. Creating initial stock does not write an initial movement. No stock-movement history endpoint/page was found.

References: [InventoryController.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/inventory/controller/InventoryController.java:40>); [InventoryService.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/inventory/service/InventoryService.java:63>); [CreateInventoryItemRequest.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/inventory/dto/request/CreateInventoryItemRequest.java:10>); [SupplierController.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/inventory/controller/SupplierController.java:23>); [SupplierRequest.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/inventory/dto/request/SupplierRequest.java:4>); [SupplierService.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/inventory/service/SupplierService.java:17>); [Inventory.tsx](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-frontend/src/pages/admin/Inventory.tsx:28>); [Suppliers.tsx](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-frontend/src/pages/admin/Suppliers.tsx:25>).

## 6. Gunasekara — staff and scheduling

| Feature | Create | Read | Update | Delete / cancel |
|---|---|---|---|---|
| Staff accounts/profiles | Create account with roles and profile | Staff list/detail | Job title/employment, roles, password, active status | Profile marked terminated/inactive |
| Shifts | Yes | List, optionally by date | Date/time/role/required staff count | Scheduled shift becomes cancelled |
| Shift assignments | Assign staff | Assignment list | No direct edit; unassign then assign | Unassign removes assignment |
| Attendance | No implemented workflow found | No implemented workflow found | No implemented workflow found | No implemented workflow found |

**Backend checks present:** full name/email/password required; registration password 8–72 characters; optional phone exactly 10 digits; unique normalized email; at least one role; role must exist; joined date cannot be future; valid employment status. Shifts require future start, end after start, valid non-customer role and required count 1–100. Assignment requires active non-terminated staff with the required role and an active user account; scheduled shift; no duplicate/overlapping assignment. Shift updates protect against overlapping assignments/role changes while assigned. Staff removal is blocked until upcoming scheduled assignments are unassigned.

**Frontend checks present:** basic staff name/email/password/phone checks; shift date/time/count checks; staff create/profile edit/remove and shift create/edit/cancel/assignment controls.

**Gaps:** `deleteStaff` only disables the profile; it leaves the associated user active, so login can remain enabled. Staff list reports `user.isActive`, which can also make a removed profile appear active. Staff roles reject uppercase `CUSTOMER` before converting roles to uppercase; lowercase `customer` can bypass that exclusion. Assignment count is not checked against `requiredStaffCount`, so a shift can be overfilled. Staff list mixes user IDs and profile IDs for accounts without a profile, while write actions expect profile IDs. Staff status input uses an unvalidated map and defaults to active if the field is omitted. Attendance is not implemented in this module. Shift staff-count limits differ: page max 20, backend max 100.

References: [StaffController.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/staff/controller/StaffController.java:34>); [StaffService.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/staff/service/StaffService.java:52>); [StaffService.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/staff/service/StaffService.java:247>); [StaffService.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/staff/service/StaffService.java:300>); [StaffService.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/staff/service/StaffService.java:202>); [StaffService.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/staff/service/StaffService.java:252>); [CreateShiftRequest.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/staff/dto/request/CreateShiftRequest.java:11>); [Staff.tsx](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-frontend/src/pages/admin/Staff.tsx:66>).

## Shared customer payments

The original member manifest assigns `billing` to Ahamed but does not assign the newer `payment` module to a member. Treat ownership as a team decision rather than guessing.

The newer module supports create/read/payment-method update/staff status update/admin delete. It validates exactly one booking/order/reservation target, customer ownership, duplicate payments, payable booking state, allowed status changes and card details. Reservation cancellation also handles pending outlet deposits and refunds paid deposits. These stronger checks do **not** fix the separate legacy `/api/billing` endpoints described above.

Reference: [CustomerPaymentService.java](</Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-/restaurant-event-backend/src/main/java/com/group06/restaurantevent/payment/service/CustomerPaymentService.java:95>).

## Fixes to prioritise before the demonstration

1. Restrict legacy billing detail/payment endpoints to the owner or appropriate staff, and validate invoice references/type/amount.
2. Make removing staff also disable the user account; use consistent profile IDs for staff actions.
3. Normalize staff roles before rejecting CUSTOMER and reject null/blank role entries.
4. Verify order table/reservation references and reservation ownership.
5. Add cancellation-reason and seating-preference limits, plus reservation date/time checks for check-in/no-show.
6. Add backend image URL validation matching the menu page; agree consistent UI/backend limits for table capacity and shift staffing.
7. Decide which missing sub-features the marking guideline actually requires: event booking edit, customer order cancel/edit, category edit/delete controls, attendance, and purchase/delivery workflow. A table or enum alone is not an implemented feature.

These are findings, not changes made by this review.

## Verification and limits

- The final backend test run earlier in this session passed **63 tests**, with zero failures/errors (7 October 2026, 00:22 Colombo time).
- During this audit the frontend tests were run again: **20 tests in 5 files passed**.
- Backend tests use an H2 database configured in MySQL mode. Frontend tests mostly cover utility functions; they are not full browser CRUD tests.
- Existing tests cover customer management/permissions, orders/menu/suppliers, inventory CRUD, event catalog rules, reservations/payments and shift create/read/update/cancel. They do not cover every gap above.
- No records were created, edited or deleted in your working MySQL database for this review. No full six-member browser walkthrough was performed.
- Review is of the local final project. This does not confirm that every reviewed change is already merged/pushed to GitHub main.

## Simple checks for each member to demonstrate

| Member | Good-data demo | Bad-data demo |
|---|---|---|
| Samarasinghe | Register, view profile, edit phone, admin deactivate a separate test account | Duplicate email; 9-digit phone; wrong current password; expired reset token |
| Ahamed | Create hall/package, edit, create enquiry, approve/cancel; delete an unused catalog entry | Guests exceed hall; package max below min; end before start; overlapping booking |
| Batagodage | Create dish, read, edit price, remove; place order and move kitchen status | Zero price; quantity 0/100; unavailable dish; invalid status jump |
| Kodithuwakku | Create table, book future slot, edit, cancel; remove unused table | Bad phone; past slot; too many guests; overlap; delete booked table |
| Labijan | Create/edit/remove stock and supplier; add/remove stock quantity | Negative stock; zero adjustment; invalid supplier phone/email/future join date |
| Gunasekara | Create staff and future shift, edit, assign/unassign, cancel shift | End before start; overlapping assignment; wrong staff role; remove staff with upcoming shift |

Use separate test records for these demonstrations. Do not remove real team records.
