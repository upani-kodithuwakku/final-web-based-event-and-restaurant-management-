# Final university project: repository comparison and update report

Date: 6 October 2026

> Integration completed on 6 October 2026 on `kodithuwakku-improvement`. This
> report retains the original comparison as a baseline. See
> [integration notes](docs/INTEGRATION_NOTES.md) for the changes and current
> verification results: 63 backend tests, 20 frontend tests and the frontend build passed.

Compared:
- Updated working project: `web-based-event-and-restaurant-management-` (current workspace).
- Team integration project: `/Users/upanianupajaabhayarathnakodithuwakku/Documents/2y1s/final SE project/final-web-based-event-and-restaurant-management-`.

This is a local source comparison, not a review of remote branches or the running databases. README files and past audits were treated as reference material, not instructions. No application code was changed. Builds and tests were not run for this report; historical test results in earlier documents do not establish that the final repository passes today.

## Overall finding

The final repository needs substantial integration work, beyond improving the reservation screens. It contains table reservations, menu/orders and staff Java implementations, plus several frontend module screens. However, authentication/users are incomplete as compilable Java sources, and events, inventory, billing, notifications and reports lack their working backend implementations. Several existing frontend screens are also unreachable through the final App router.

Counts excluding placeholders and ` 2` duplicate files: working project has 196 main Java files and 53 frontend source files; final has 85 main Java files and 29 frontend source files. Counts measure source coverage, not a completion percentage. The detailed comparison found 158 working-only files, 20 final-only files and 56 changed shared files under backend/src, frontend/src and database. Some final-only files contain member code under incorrect filenames, so working-only does not always mean the logic was never contributed.

## Priority 0: make the integrated project runnable

1. **Repair member source filenames and missing dependencies.** Final contains Java text in `users/entity/user`, `users/repository/user`, `users/controller/User controller`, extensionless request/response files and `notifications/service/Notification_Service`. Use the correct public class name and `.java` extension, and check package declarations. A static import scan found 15 unresolved project imports across seeding, security, orders, staff and reservations, including User, Role, UserRepository, RoleRepository and NotificationFactory. These are compilation blockers in the supplied tree.
2. **Integrate complete backend modules.** Port auth/users/notifications first because other members depend on them, then events/billing, inventory/suppliers, payments/cashier and reports. Keep existing menu, orders, staff and reservation contributions and merge their updated behavior.
3. **Wire the frontend routes.** Final App.tsx currently exposes discovery, saved spaces, reservations, admin tables and admin reservations. Add login/register, password recovery, dashboard/profile, menu, events, payments, and the remaining admin routes already present or newly ported. Having a page file alone does not make the feature accessible.
4. **Agree on one runtime configuration.** Final pom specifies Java 25/Spring Boot 3.5.16; working specifies Java 21/Spring Boot 3.3.4. Select and verify the team baseline rather than blindly replacing pom.xml. Align backend port, frontend proxy, CORS, database connection and setup documentation. Working uses frontend 5174/backend 8081 and has compose.yaml. Its password-reset frontend URL default still says 5173; reconcile that when integrating.

## Table reservations and dining areas: required improvements

| Change to integrate | Evidence in updated working project | Benefit / integration note |
| --- | --- | --- |
| Named spaces with descriptions and photos | RestaurantTable, CreateTableRequest and TableResponse add displayName, description and imageUrl | Transfer entity, DTO, mapping and frontend types together; apply schema additions before seed data |
| More distinct dining choices | database/11_dining_spaces.sql, 12_private_dining_photos.sql, 13_private_dining_names.sql | Adds garden/outdoor/window/indoor/private examples and distinguishes small private dining from group dining |
| Group dining and small celebrations | 09_small_celebrations.sql and 10_group_dining_table.sql | Review real capacity and prices before using demo records; keep private table bookings distinct from event hall bookings |
| Improved discovery and saved spaces | Discover.tsx and services/savedSpaces.ts | Saved-space filtering excludes inactive/out-of-service spaces and respects party size and selected area |
| Consistent booking validation | ReservationService.validateSlot plus frontend validation.ts and reservation request DTOs | Future Colombo date/time, 11:00–21:00 start, guest count 1–200, capacity checks, ten-digit reservation phone and inline errors |
| Better alternative suggestions | ReservationService.checkAvailability | Suggestions respect seating preference, future time and permitted start hours |
| Protect tables already booked | TableService.updateTable/deleteTable | Prevent lowering capacity below upcoming guests and deactivating tables with active/upcoming reservations |
| Reservation deposit workflow | BookingPayment.tsx, payment module and deposit configuration | LKR 500 per guest configured; customer payment choices/history and cancellation linkage; simulated gateway for university demonstration |
| Confirmation and history | AdminReservationController /confirm and /history; reservations/observer | Add pending confirmation and an auditable status timeline |
| Reservation Observer pattern | ReservationSubject, ReservationEventPublisher, notification/audit observers | Notifications and audit reactions are separated from reservation rules; synchronous transaction behavior is documented in service |
| Updated customer/staff presentation | Reservations.tsx, AdminReservations.tsx, BookingModal.tsx, CSS | Clearer booking details, contact information, daily counts, photos and workflow actions |

**Area-model limitation:** the updated project still represents the seating area as RestaurantTable.location text. It does not introduce a separate DiningArea entity with independent CRUD, operating hours or area-level booking capacity. If the university specification requires area management as a separate module, that is additional development, not a feature that can simply be copied.

## Preserve final-repository behavior when merging reservations

The final branch contains useful functionality that the working version no longer exposes:
- Staff reservation detail, edit and cancel endpoints; editing includes changing the assigned table.
- AdminCreateReservationRequest and AdminUpdateReservationRequest.
- Individual table lookup endpoint.
- Staff creation permission includes WAITER; working restricts creation to ADMIN/MANAGER and uses customerEmail as a query parameter rather than the final request DTO.
- Final cancellation blocks NO_SHOW; working customer cancellation does not include that status in its rejection list.

Merge these deliberately with the new validation, payment cancellation and Observer events. Keep staff permissions agreed by the team, audit the actual staff actor, and test reassignment against capacity and overlap. Replacing the entire ReservationService/AdminReservationController with the working files would remove existing staff features and change API contracts.

## Work allocation across the six members

| Member | Existing final coverage | Updates to take responsibility for |
| --- | --- | --- |
| 01 Samarasinghe — customers | Security infrastructure and some misnamed user files; customer frontend pages exist but routing is incomplete | Restore compilable auth/users classes and repositories; login/register/profile/password recovery; admin user management; persisted notification API/bell; reports integration |
| 02 Ahamed — events/billing | Events and cashier frontend screens; backend implementation absent in the inspected tree | Hall/package CRUD and catalog, booking/availability, coordinator approval/rejection/cancellation, invoices/billing, event deposits, catalog manager UI and seeding |
| 03 Batagodage — menu/orders | Menu and order Java implementations and customer/kitchen pages | Merge updated category/item validation and CRUD, admin menu page, persisted account-specific bag, food requests and staff resolution UI, order workflow validation and menu imagery |
| 04 Kodithuwakku — reservations | Most complete integrated module; extra staff CRUD worth retaining | Named/photo dining spaces, saved filtering, stronger booking rules, protected tables, deposits, Observer audit/history and updated calendar/customer UI; preserve staff edit/reassign/cancel |
| 05 Labijan — inventory/supply | Inventory frontend screen; backend inventory/supplier implementation missing | Inventory entities/services/controllers, validated audited stock adjustment, low-stock data, supplier CRUD and Suppliers page, suppliedProducts/joinedDate schema additions |
| 06 Gunasekara — staff | Staff backend and frontend are already present | Merge updated staff/shift validation, employment/role restrictions, assignment conflict handling and cancellation/unassignment behavior; coordinate staff-user creation with Member 01 |

Shared ownership: App.tsx, AppContext.tsx, services/api.ts, types.ts, security/CORS, pom.xml, migrations and navigation must be integrated consistently. Each member should not independently overwrite these shared files.

## Database and asset updates

Final currently provides 01_create_database.sql and reservation reference documents/queries. Working additionally provides 02_food_requests, 03_menu_items, 04_customer_payments, 06_customer_management, 07_reservations_data, 08_reservation_payments, 09_small_celebrations, 10_group_dining_table, 10_remove_payment_sms, 11_dining_spaces, 12_private_dining_photos, 13_private_dining_names and 14_supplier_details scripts.

Read each script and distinguish schema changes from optional demo data. These filenames are not an automatic migration system: two scripts start with 10, and there is no 05 in this set. Establish explicit execution order, prerequisites and rerun behavior. Back up existing team data before migration. Do not copy a local database volume or credentials.

Transfer referenced public assets with their frontend changes, particularly menu photos/atlas and occasion images. Compare package.json/lockfile and Vite/test settings when porting tests. Exclude node_modules, generated targets, .env, .DS_Store and member archive snapshots from the integration.

The working repo contains ` 2` copies of SQL, components, services and test files. They were excluded from the primary comparison. Reconcile them before transfer rather than adding duplicate source or executing both copies of migrations.

## What still needs verification even after copying updates

- The updated repo is a source of improvements, not proof that every original member requirement is implemented. Member reference documents mention purchase orders/goods receiving, ingredient recipes/automatic order stock deduction and attendance recording. The inspected working inventory/staff class sets do not establish complete implementations of those flows; confirm the university scope and demonstrate them before claiming completion.
- Customer checkout and invoice billing use separate payment records. Reconcile reporting so the same payment is not counted twice.
- The card gateway is simulated; do not describe it as a real payment provider in the submission.
- Test concurrent same-table booking requests; a normal overlap lookup alone is not evidence of concurrency safety.
- Confirm role authorization on each API and page, and test customer ownership restrictions.

## Recommended integration order and acceptance checks

1. Correct source names and auth/user dependencies; select runtime/configuration baseline. Acceptance: backend compiles and starts against a fresh configured database, registration/login work.
2. Merge reservations and dining-space changes while retaining final staff CRUD. Acceptance: search/filter, capacity, future-time/service-hour checks, conflicting bookings, staff reassignment, cancellation and table protection all work.
3. Integrate events, inventory/suppliers, payment/billing/cashier, food requests and reports; merge menu and staff improvements. Acceptance: each member demonstrates their own create/read/update/remove flow and role restrictions with persisted data.
4. Complete routes/navigation, public assets, schemas and reproducible setup. Acceptance: a teammate can clone, configure and start the project using the README without your local files.
5. Run frontend build/Vitest and backend Maven tests, then a combined manual demo: register → reserve space → deposit choice → staff arrival/completion; event enquiry → coordinator decision → invoice/payment; food order → kitchen status; inventory adjustment/supplier management; shift assignment.

Keep contributions reviewable as module commits from each member's own account, followed by explicit shared-integration commits. The report does not determine a reliable percentage complete or effort estimate because compilation, database migration and end-to-end behavior have not been executed.

## File-level comparison appendix

Paths are repository-relative. This appendix covers backend/src, frontend/src and database; root configuration and public assets were discussed separately. Placeholder .gitkeep, .DS_Store and ` 2` copies are excluded.

### Files in working but absent at the same path in final

- `database/02_food_requests.sql`
- `database/03_menu_items.sql`
- `database/04_customer_payments.sql`
- `database/06_customer_management.sql`
- `database/07_reservations_data.sql`
- `database/08_reservation_payments.sql`
- `database/09_small_celebrations.sql`
- `database/10_group_dining_table.sql`
- `database/10_remove_payment_sms.sql`
- `database/11_dining_spaces.sql`
- `database/12_private_dining_photos.sql`
- `database/13_private_dining_names.sql`
- `database/14_supplier_details.sql`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/auth/controller/AuthController.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/auth/dto/request/ForgotPasswordRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/auth/dto/request/LoginRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/auth/dto/request/RegisterRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/auth/dto/request/ResetPasswordRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/auth/dto/response/AuthResponse.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/auth/dto/response/ForgotPasswordResponse.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/auth/entity/PasswordResetToken.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/auth/repository/PasswordResetTokenRepository.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/auth/service/AuthService.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/auth/service/PasswordResetService.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/billing/controller/BillingController.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/billing/dto/request/CreateInvoiceRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/billing/dto/request/CreatePaymentRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/billing/dto/response/InvoiceResponse.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/billing/dto/response/PaymentResponse.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/billing/entity/Invoice.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/billing/entity/InvoiceItem.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/billing/entity/Payment.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/billing/repository/InvoiceRepository.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/billing/repository/PaymentRepository.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/billing/service/BillingService.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/cashier/controller/CashierController.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/cashier/dto/response/CashierSummaryResponse.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/cashier/service/CashierService.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/common/enums/FoodRequestStatus.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/common/enums/PaymentOption.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/common/enums/PaymentPurpose.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/config/EventCatalogSeeder.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/config/StaffProfileSeeder.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/events/controller/EventCatalogController.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/events/controller/EventController.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/events/controller/EventCoordinatorController.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/events/dto/request/CreateEventBookingRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/events/dto/request/EventHallRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/events/dto/request/EventPackageRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/events/dto/response/EventBookingResponse.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/events/dto/response/EventHallResponse.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/events/dto/response/EventPackageResponse.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/events/entity/EventBooking.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/events/entity/EventHall.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/events/entity/EventPackage.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/events/repository/EventBookingRepository.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/events/repository/EventHallRepository.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/events/repository/EventPackageRepository.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/events/service/EventBookingService.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/events/service/EventCatalogService.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/inventory/controller/InventoryController.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/inventory/controller/SupplierController.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/inventory/dto/request/AdjustStockRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/inventory/dto/request/CreateInventoryItemRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/inventory/dto/request/SupplierRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/inventory/dto/response/InventoryItemResponse.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/inventory/entity/InventoryItem.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/inventory/entity/StockMovement.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/inventory/entity/Supplier.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/inventory/repository/InventoryItemRepository.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/inventory/repository/StockMovementRepository.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/inventory/repository/SupplierRepository.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/inventory/service/InventoryService.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/inventory/service/SupplierService.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/notifications/controller/NotificationController.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/notifications/dto/response/NotificationResponse.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/notifications/entity/Notification.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/notifications/mapper/NotificationMapper.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/notifications/repository/NotificationRepository.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/notifications/service/NotificationFactory.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/notifications/service/NotificationService.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/orders/controller/FoodRequestController.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/orders/dto/request/CreateFoodRequestRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/orders/dto/response/FoodRequestResponse.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/orders/entity/FoodRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/orders/repository/FoodRequestRepository.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/orders/service/FoodRequestService.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/payment/controller/CustomerPaymentController.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/payment/dto/request/CardDetailsRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/payment/dto/request/CreateCustomerPaymentRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/payment/dto/request/UpdateCustomerPaymentRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/payment/dto/request/UpdatePaymentStatusRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/payment/dto/response/CustomerPaymentResponse.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/payment/dto/response/PaymentSummaryResponse.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/payment/entity/CustomerPayment.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/payment/repository/CustomerPaymentRepository.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/payment/service/CustomerPaymentService.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/payment/strategy/CardPaymentStrategy.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/payment/strategy/PayAtOutletPaymentStrategy.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/payment/strategy/PaymentMethodStrategy.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/reports/controller/ReportController.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/reports/dto/response/DashboardReportResponse.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/reports/dto/response/EventReportResponse.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/reports/dto/response/InventoryReportResponse.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/reports/dto/response/ReservationReportResponse.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/reports/dto/response/SalesReportResponse.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/reports/service/ReportService.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/reservations/observer/CustomerNotificationObserver.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/reservations/observer/ReservationAuditObserver.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/reservations/observer/ReservationEvent.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/reservations/observer/ReservationEventPublisher.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/reservations/observer/ReservationObserver.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/reservations/observer/ReservationSubject.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/users/controller/AdminUserController.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/users/controller/UserController.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/users/dto/request/AdminResetPasswordRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/users/dto/request/ChangePasswordRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/users/dto/request/UpdateProfileRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/users/dto/request/UpdateUserStatusRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/users/dto/response/AdminUserResponse.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/users/dto/response/UserProfileResponse.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/users/entity/Role.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/users/entity/User.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/users/repository/RoleRepository.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/users/repository/UserRepository.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/users/service/UserService.java`
- `restaurant-event-backend/src/test/java/com/group06/restaurantevent/CustomerManagementTests.java`
- `restaurant-event-backend/src/test/java/com/group06/restaurantevent/CustomerPaymentTests.java`
- `restaurant-event-backend/src/test/java/com/group06/restaurantevent/InventoryCrudTests.java`
- `restaurant-event-backend/src/test/java/com/group06/restaurantevent/MenuOrderingTests.java`
- `restaurant-event-backend/src/test/java/com/group06/restaurantevent/RestaurantEventApplicationTests.java`
- `restaurant-event-backend/src/test/java/com/group06/restaurantevent/cashier/service/CashierServiceTests.java`
- `restaurant-event-backend/src/test/java/com/group06/restaurantevent/payment/strategy/PaymentStrategyTests.java`
- `restaurant-event-backend/src/test/java/com/group06/restaurantevent/reservations/observer/ReservationEventPublisherTests.java`
- `restaurant-event-frontend/src/components/BookingPayment.tsx`
- `restaurant-event-frontend/src/components/EventCatalogManager.tsx`
- `restaurant-event-frontend/src/components/HeroCarousel.tsx`
- `restaurant-event-frontend/src/components/MenuPhoto.tsx`
- `restaurant-event-frontend/src/components/NotificationBell.tsx`
- `restaurant-event-frontend/src/components/PaymentGateway.tsx`
- `restaurant-event-frontend/src/pages/ForgotPassword.tsx`
- `restaurant-event-frontend/src/pages/Payments.tsx`
- `restaurant-event-frontend/src/pages/ResetPassword.tsx`
- `restaurant-event-frontend/src/pages/admin/AdminEvents.tsx`
- `restaurant-event-frontend/src/pages/admin/CustomerPayments.tsx`
- `restaurant-event-frontend/src/pages/admin/FoodRequests.tsx`
- `restaurant-event-frontend/src/pages/admin/Menu.tsx`
- `restaurant-event-frontend/src/pages/admin/Suppliers.tsx`
- `restaurant-event-frontend/src/services/bag.test.ts`
- `restaurant-event-frontend/src/services/bag.ts`
- `restaurant-event-frontend/src/services/card.test.ts`
- `restaurant-event-frontend/src/services/card.ts`
- `restaurant-event-frontend/src/services/eventCatalog.test.ts`
- `restaurant-event-frontend/src/services/eventCatalog.ts`
- `restaurant-event-frontend/src/services/savedSpaces.test.ts`
- `restaurant-event-frontend/src/services/savedSpaces.ts`
- `restaurant-event-frontend/src/services/validation.test.ts`
- `restaurant-event-frontend/src/services/validation.ts`

### Files unique to final: preserve or repair

- `database/example-queries.sql`
- `database/module-entities.md`
- `database/module-relationships.md`
- `database/module-schema.md`
- `database/module-status-enums.md`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/auth/dto/request/ResetPasswordRequest`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/notifications/service/Notification_Service`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/reservations/dto/request/AdminCreateReservationRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/reservations/dto/request/AdminUpdateReservationRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/users/controller/User controller`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/users/dto/request/.AdminResetPasswordRequest`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/users/dto/request/ChangePasswordRequest`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/users/dto/request/UpdateProfileRequest`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/users/dto/request/UpdateUserStatusRequest`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/users/dto/response/.AdminUserResponse`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/users/dto/response/UserProfileResponse`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/users/entity/user`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/users/repository/user`
- `restaurant-event-frontend/src/pages/shared/CustomerDashboard`
- `restaurant-event-frontend/src/pages/shared/auth`

### Shared paths with different content

- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/RestaurantEventApplication.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/common/audit/AuditLogRepository.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/common/exception/GlobalExceptionHandler.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/config/CorsConfig.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/config/SecurityConfig.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/menu/controller/AdminMenuController.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/menu/dto/request/CreateCategoryRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/menu/dto/request/CreateMenuItemRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/menu/dto/response/CategoryResponse.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/menu/dto/response/MenuItemResponse.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/menu/service/MenuService.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/orders/controller/OrderController.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/orders/dto/request/CreateOrderRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/orders/service/OrderService.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/reservations/controller/AdminReservationController.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/reservations/controller/ReservationController.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/reservations/dto/request/CreateReservationRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/reservations/dto/request/CreateTableRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/reservations/dto/request/UpdateReservationRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/reservations/dto/response/TableResponse.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/reservations/entity/RestaurantTable.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/reservations/repository/TableReservationRepository.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/reservations/service/ReservationService.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/reservations/service/TableService.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/staff/controller/StaffController.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/staff/dto/request/CreateShiftRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/staff/dto/request/CreateStaffProfileRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/staff/dto/request/CreateStaffUserRequest.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/staff/repository/ShiftAssignmentRepository.java`
- `restaurant-event-backend/src/main/java/com/group06/restaurantevent/staff/service/StaffService.java`
- `restaurant-event-backend/src/main/resources/application.properties`
- `restaurant-event-frontend/src/App.tsx`
- `restaurant-event-frontend/src/components/AdminLayout.tsx`
- `restaurant-event-frontend/src/components/BookingModal.tsx`
- `restaurant-event-frontend/src/components/Layout.tsx`
- `restaurant-event-frontend/src/components/UI.tsx`
- `restaurant-event-frontend/src/context/AppContext.tsx`
- `restaurant-event-frontend/src/data.ts`
- `restaurant-event-frontend/src/index.css`
- `restaurant-event-frontend/src/pages/Auth.tsx`
- `restaurant-event-frontend/src/pages/Discover.tsx`
- `restaurant-event-frontend/src/pages/Events.tsx`
- `restaurant-event-frontend/src/pages/Menu.tsx`
- `restaurant-event-frontend/src/pages/Profile.tsx`
- `restaurant-event-frontend/src/pages/Reservations.tsx`
- `restaurant-event-frontend/src/pages/admin/AdminReservations.tsx`
- `restaurant-event-frontend/src/pages/admin/Cashier.tsx`
- `restaurant-event-frontend/src/pages/admin/Dashboard.tsx`
- `restaurant-event-frontend/src/pages/admin/Inventory.tsx`
- `restaurant-event-frontend/src/pages/admin/Kitchen.tsx`
- `restaurant-event-frontend/src/pages/admin/Reports.tsx`
- `restaurant-event-frontend/src/pages/admin/Staff.tsx`
- `restaurant-event-frontend/src/pages/admin/Tables.tsx`
- `restaurant-event-frontend/src/pages/admin/Users.tsx`
- `restaurant-event-frontend/src/services/api.ts`
- `restaurant-event-frontend/src/types.ts`
