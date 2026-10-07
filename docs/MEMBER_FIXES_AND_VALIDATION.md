# Member fixes and validation

Updated: 7 October 2026. Changes are in the local `kodithuwakku-improvement` checkout. This does not confirm that GitHub main contains them.

The earlier SIX_MEMBER_CRUD_VALIDATION_REVIEW.md is the review before these fixes.

| Area | Changes |
| --- | --- |
| Accounts and notifications | Password byte limit, optional reset email delivery after commit, ownership checks when deleting notifications. SMTP credentials are still needed for real email delivery. |
| Events and billing | Edit eligible event enquiries with capacity and overlap checks; invoice ownership checks, amounts calculated by the server, void unpaid invoices, and guards against duplicate billing. |
| Menu and orders | Category editing/removal, validated image paths, edit/cancel eligible pending orders, and edit/withdraw open food requests. Payment and ownership restrictions apply. |
| Reservations | Cancellation text limits, check-in/no-show time checks, and guards against taking booked tables out of service. |
| Inventory | Stock movement history, duplicate checks, low-stock notifications, purchase order create/read/update/cancel, and atomic full delivery receipt. Partial deliveries are not supported. |
| Staff | Staff account deactivation, consistent profile IDs, role and shift capacity checks, attendance CRUD with date/time validation, and protection of recorded attendance history. |

## Folder cleanup

Twelve empty numbered duplicate directories were removed from reservation DTOs and common packages. No Java source file was removed. The source of those duplicate folders has not been established.

## Verification

- Backend: 77 tests passed with no failures or errors, using an isolated H2 test database.
- Frontend: TypeScript and production Vite build passed. Vite reports a non-blocking bundle size warning.
- Real SMTP delivery and every workflow against a fresh MySQL database have not been verified.
- Build success does not confirm that an already-running backend process has loaded these changes; restart it after saving.

## Database

The full MySQL schema is `database/00_full_schema.sql` at the repository root. Purchase orders and attendance now have backend workflows. The attendance default is ABSENT; supported recorded statuses are PRESENT, ABSENT, LATE, and HALF_DAY. Existing databases should be backed up before schema changes.
