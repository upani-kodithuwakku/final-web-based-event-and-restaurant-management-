# Database table audit

Reviewed the local `restaurant_event_db` schema, column definitions, foreign keys and corresponding backend entities/services on 6 October 2026. There are 26 application tables. No redundant duplicate table was identified, so no tables were dropped.

## Why food_orders and food_order_items are separate

`food_orders` stores one record per order: its reference, customer, table, status and subtotal. `food_order_items` stores one record per dish in that order: dish ID, quantity, special note and the name/price at ordering time. `food_order_items.order_id` points to `food_orders.id`.

For example, one order containing two burgers and one juice has one order row and two item rows. Combining these tables would repeat the customer and order status for every dish or require a list inside one database field. The current one-to-many design is appropriate. Price and name snapshots intentionally preserve historical orders when menu details change.

## All tables and their purpose

| Table | Purpose |
| --- | --- |
| users | Login and customer identity |
| roles | Available permission roles |
| user_roles | Many-to-many mapping between users and roles |
| password_reset_tokens | Expiring password recovery tokens |
| staff_profiles | Employment details beyond login identity |
| shifts | Scheduled work periods |
| shift_assignments | Staff allocated to each shift |
| restaurant_tables | Seating capacity and location |
| table_reservations | Guest bookings against those tables |
| event_halls | Event spaces |
| event_packages | Offered celebration packages |
| event_bookings | Customer bookings selecting hall/package |
| menu_categories | Groups of dishes |
| menu_items | Current dish details and availability |
| food_orders | Order headers |
| food_order_items | Ordered dishes and historical price/name snapshots |
| food_requests | Customer questions or dietary requests, distinct from orders |
| invoices | Billing document, tax, service charge, discount and total |
| invoice_items | Line items of the issued billing document |
| payments | Payments against invoices through the billing service |
| customer_payments | Customer checkout records for food, event deposits and table deposits |
| inventory_items | Current stock and reorder levels |
| stock_movements | History of additions, consumption and adjustments |
| suppliers | Supplier companies, contacts and supplied products |
| notifications | In-app messages to individual users |
| audit_logs | History of actions, including reservation status changes |

`payments` and `customer_payments` have overlapping payment information, but different references and active services: billing payments require an invoice; customer checkout can pay a reservation/event deposit without one. They are not unused duplicates. A future unified ledger would require migrating both workflows and existing records; dropping either table would break current features. Reports should not blindly sum both ledgers for the same transaction.

Invoices and invoice items are a billing snapshot, whereas food orders and order items drive kitchen fulfilment. They have distinct responsibilities despite sharing some monetary fields.

Some relationships are represented as scalar IDs in entities rather than physical database foreign keys. This is a separate integrity consideration, not evidence of duplicate tables. This audit does not claim every legacy row is free of duplicate business data.

## Supplier additions

The original supplier columns were `id`, `name`, `contact_person`, `phone`, `email`, `address`, `is_active`, `created_at`, and `updated_at`. Added `supplied_products` and `joined_date` in `database/14_supplier_details.sql`.

The supplier form exposes all editable business columns and active status; ID and creation/update timestamps are shown as automatically managed metadata. Existing unknown products and joining dates remain null until entered; creation date is not treated as the historical joining date.

Admin and Manager can create, read, update and remove suppliers. All staff roles can read. Removal marks a supplier inactive, keeps their details and allows reactivation through Edit. This avoids losing supplier history.
