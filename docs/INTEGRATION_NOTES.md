# Integration notes — 6 October 2026

Source improvements were transferred from the older working repository into the
final team repository. Existing member history is retained. New commits use the
configured identity `Upani kodithuwakku <Upanikodithuwakku5@gmail.com>` without
additional author or co-author trailers.

## Branches

- `feature/reservation-management` contains 13 new commits for dining space
  details, booking validation, saved filtering, table protection, Observer
  notifications/audit, staff calendar changes, reservation deposits UI, and
  reservation SQL examples.
- `kodithuwakku-improvement` starts at the updated reservation branch and adds
  the other modules and shared integration. It is the complete integrated tree.
  The reservation changes rely on shared APIs, authentication/notifications and
  payment support completed in this branch. Use the complete branch for testing
  and bring both sets of commits into `main` together.

The reservation merge preserves staff request-body booking creation, individual
lookup, editing/reassignment, cancellation, WAITER permissions, and the NO_SHOW
cancellation restriction. Both customer and staff cancellation update deposits;
payment audits identify the acting customer or staff account.

## Integrated features

- Authentication, registration, password reset, profile and admin users with
  compilable class filenames and role repositories.
- Persisted notifications, event catalog CRUD and booking, invoice billing,
  customer payments, cashier totals and reporting.
- Inventory stock adjustments, low-stock data and suppliers.
- Menu validation/administration, account-specific bags, food requests and
  kitchen transitions.
- Staff/shift validation, assignment checks, customer/admin routes, navigation,
  referenced public assets and responsive styles.
- Correctly named Java sources replace extensionless member files. Identical
  billing classes in the events package and populated-folder placeholders were
  removed after retaining their canonical implementations.

## Verification

The complete tree passed:

- Backend clean Maven test run: **63 tests, zero failures or errors**. Tests use
  H2. Run on the available JDK 25 with Java 21 compilation and the Byte Buddy
  experimental flag documented in [RUNNING.md](../RUNNING.md).
- Frontend production TypeScript/Vite build: **passed**.
- Frontend Vitest suite: **20 tests across five files, all passed**.
- Added integration regressions verify staff reassignment capacity/overlap
  checks, deposit cancellation and preserved no-show cancellation restrictions.

Vite reports one bundle slightly above its 500 kB advisory size. No live MySQL
migration, browser walkthrough or concurrent-booking load test was performed.
The simulated payment flow is for the university demonstration.

## Manual SQL order

Start the backend to create its entity schema first. Do not rerun the initial
`01_create_database.sql` against existing data without reviewing it. SQL files
with `USE restaurant_event_db` select that schema; also select it explicitly for
scripts using `DATABASE()`.

Schema/setup scripts: `02_food_requests.sql`, `04_customer_payments.sql`,
`06_customer_management.sql`, `08_reservation_payments.sql` (after 04),
`11_dining_spaces.sql` (schema additions and dining examples), and
`14_supplier_details.sql`. `10_remove_payment_sms.sql` is a separate cleanup
script; review it before use.

Optional examples/data: `03_menu_items.sql`, `07_reservations_data.sql`,
`09_small_celebrations.sql`, `10_group_dining_table.sql`,
`12_private_dining_photos.sql`, and `13_private_dining_names.sql`. Run the private
dining scripts after 11. The reservation data script adds demonstration customer
accounts and bookings; it is not needed for normal operation.

There are two scripts starting with 10; execute them by their full filename and
purpose. No `.env`, database volumes, dependency directories, build outputs,
macOS metadata, member archives or duplicate ` 2` source files were transferred.

The original [comparison report](../FINAL_REPO_COMPARISON_REPORT.md) is preserved
as the pre-integration baseline; its missing-module findings describe the tree
before this integration, not the complete branch now.
