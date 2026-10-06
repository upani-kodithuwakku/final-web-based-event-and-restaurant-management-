# Six-member module validation and CRUD audit

Reviewed against the six README files in `restaurant-event-member-packages/MEMBER_FOLDERS`.
Changes apply to the active frontend/backend; the member archives remain reference copies.

| Member/module | Create | Read | Update | Delete/remove | Validation added or retained |
| --- | --- | --- | --- | --- | --- |
| 01 Customer management | Customer registration; staff account creation | Own profile; admin directory | Profile, admin user details, password and status | Admin deactivation preserves records | Required/limited names, valid and unique email, normalized registration email, 8–72 character registration passwords, optional ten-digit phone; own-account suspension blocked |
| 02 Events and billing | Hall/package editor; customer enquiry | Public catalogue, own history, coordinator bookings | Hall/package editor, approval/rejection | Hall/package removal; enquiry cancellation | Positive prices, required names/location/type, min/max guest limits, active catalogue, hall capacity and overlap, ordered time range, required rejection reason; active bookings protect catalogue edits/removal |
| 03 Menu and food orders | Categories, items, orders | Menu/category lists, own order history, kitchen queue | Category/item edits, availability, order workflow | Category/item deactivation; kitchen cancellation | Required category/name, active category, positive price with two decimal places, preparation 1–240 minutes, nonnegative category ordering, limited descriptions/notes, order quantities 1–99, valid order type, forward status transitions |
| 04 Tables and reservations | Table editor and reservation form | Availability, own history, daily staff calendar | Table details/status, reservation modifications, confirm/check-in/complete/no-show | Table deactivation; reservation cancellation | Exactly ten digits for reservation phone; valid future date/time in Colombo; start 11:00–21:00; positive whole-number party size; capacity/overlap/out-of-service checks; active/upcoming reservations protect table removal/capacity changes |
| 05 Inventory | Item editor | Items/low-stock | Item details, audited stock adjustment | Item deactivation | Name/unit required; nonnegative stock/reorder threshold; max three decimal places; typed stock-adjustment request; nonzero delta; negative resulting stock blocked |
| 06 Staff scheduling | Staff account and shift creation | Staff directory; shifts/assignments | Staff profile/roles/status, shift editing | Staff profile removal; shift cancellation; unassignment | Optional ten-digit phone, email/password validation; valid employment status; future start, ordered times, positive staff count, existing staff role; inactive/terminated/wrong-role staff blocked; overlap check excludes cancelled shifts; edits detect assignment conflicts; removal requires unassigning upcoming shifts |

Deletion uses deactivation/cancellation for records referenced by bookings, orders, or audit history.
Supporting history records are preserved rather than exposed as arbitrary CRUD forms.

## Report correction

Event reports count **bookings received** during the selected range using `createdAt`, including celebrations scheduled in the future. Confirmed guests and value are calculated from the same cohort. Reservation reports continue to use the visit date.

## Reservation presentation

The staff view includes daily reservation/guest/arrival/dining counts, a compact labelled filter toolbar, venue photos, contact details and workflow actions. Customer reservations include a welcome banner and clearer booking details. The booking form shows validation inline and as a popup before submitting.

## Verification

Frontend validation tests cover exact phone length, impossible dates, past times, service hours and capacity. Integration tests cover reservation rejection, newly created future-event reporting, role permissions, hall CRUD, guest limits, malformed stock adjustments, invalid menu prices, and shift CRUD/time order. Existing customer, payment, menu and inventory suites are retained.

Final checks: production frontend build passed; 20 frontend tests and 50 backend tests passed. After restart, the live report shows 8 bookings, 2 confirmed, 60 confirmed guests and LKR 155,000 confirmed value in the selected last-30-days range. The staff calendar displays the three existing reservations for 6 October 2026. Existing phone values are preserved; the new ten-digit rule applies to new or edited contact input.
