# Claude Code Instructions — Restaurant & Event Management System

## Project Context

This is an academic Software Engineering project: **Web-Based Restaurant and Event Management System**.

The system is a single-branch restaurant management platform. It must manage:

1. Authentication and role-based access
2. Table reservation management
3. Menu and food ordering
4. Event booking and management
5. Billing and simulated payments
6. Inventory and suppliers
7. Staff scheduling and attendance
8. Notifications, feedback, audit logs, and reports

The required platform is a **Java-based web application**.

## Technology Decisions

### Backend

- Java 21
- Spring Boot 3
- Maven
- Spring Web
- Spring Data JPA / Hibernate
- Spring Security
- JWT authentication
- Jakarta Bean Validation
- JUnit 5 and Mockito
- Swagger / OpenAPI

### Frontend

- React
- Vite
- TypeScript
- Tailwind CSS
- Axios
- React Router

### Database

- MySQL 8
- MySQL Workbench is installed locally
- MySQL Server is used as the local database server
- Database name: `restaurant_event_db`
- Development machine: macOS / MacBook
- Database port: `3306`
- Backend port: `8080`
- Frontend port: `5173`

Do not use MongoDB, Express.js, Node.js backend, PHP, or microservices.

## Local Database Setup

Use MySQL Workbench to create the local database:

```sql
CREATE DATABASE IF NOT EXISTS restaurant_event_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

Never place real passwords, JWT secrets, API keys, or database credentials in Git commits.

Use environment variables or an ignored `.env` file for sensitive values.

Example local environment values:

```properties
DB_HOST=localhost
DB_PORT=3306
DB_NAME=restaurant_event_db
DB_USERNAME=root
DB_PASSWORD=CHANGE_THIS
JWT_SECRET=replace-with-a-long-random-local-secret
```

Example Spring Boot configuration:

```properties
spring.application.name=restaurant-event-system

spring.datasource.url=jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:restaurant_event_db}?useSSL=false&serverTimezone=Asia/Colombo&allowPublicKeyRetrieval=true
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

server.port=8080

app.jwt.secret=${JWT_SECRET}
app.jwt.expiration-ms=86400000
```

## System Users and Roles

Use the following roles:

```text
CUSTOMER
ADMIN
MANAGER
WAITER
KITCHEN_STAFF
EVENT_COORDINATOR
CASHIER
INVENTORY_MANAGER
```

### Access Expectations

- `CUSTOMER`: Own profile, reservations, orders, event bookings, invoices, payments, feedback, and notifications.
- `WAITER`: Reservation calendar, table status, check-in, table occupancy, assigned operational views.
- `KITCHEN_STAFF`: View food orders and update order preparation status.
- `EVENT_COORDINATOR`: View and decide event booking requests; manage packages, halls, and event calendar.
- `CASHIER`: Create invoices, record simulated payments, and view payment history.
- `INVENTORY_MANAGER`: Manage stock, suppliers, purchase orders, stock movements, and low-stock alerts.
- `ADMIN` and `MANAGER`: Full system management and reports.

## Backend Architecture

Use a modular monolith. Do not create microservices.

```text
React Frontend
    |
    | JSON REST API
    v
Spring Boot Backend
    |
    | JPA / Hibernate
    v
MySQL Server
```

Use this pattern in every module:

```text
Controller -> Service -> Repository -> Entity
```

Controllers must use request and response DTOs. Do not return JPA entities directly from controllers.

Use constructor injection only. Do not use field injection.

## Repository Structure

```text
restaurant-event-backend/
├── pom.xml
├── README.md
├── .gitignore
├── .env.example
├── src/
│   ├── main/
│   │   ├── java/com/group06/restaurantevent/
│   │   │   ├── RestaurantEventApplication.java
│   │   │   ├── config/
│   │   │   ├── common/
│   │   │   │   ├── audit/
│   │   │   │   ├── enums/
│   │   │   │   ├── exception/
│   │   │   │   └── response/
│   │   │   ├── security/
│   │   │   ├── auth/
│   │   │   ├── users/
│   │   │   ├── reservations/
│   │   │   ├── menu/
│   │   │   ├── orders/
│   │   │   ├── events/
│   │   │   ├── billing/
│   │   │   ├── inventory/
│   │   │   ├── staff/
│   │   │   ├── notifications/
│   │   │   └── reports/
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/
│   └── test/
```

Inside each feature module, use:

```text
module/
├── controller/
├── dto/
│   ├── request/
│   └── response/
├── entity/
├── mapper/
├── repository/
└── service/
```

## Database Conventions

- Use `BIGINT AUTO_INCREMENT` identifiers.
- Use `DECIMAL(12,2)` for all money values.
- Use `DECIMAL(12,3)` for inventory quantity.
- Use `LocalDate`, `LocalTime`, and `LocalDateTime` in Java.
- Store timestamps in MySQL as `DATETIME`.
- Use enums for roles and statuses.
- Use `created_at` and `updated_at` on important entities.
- Add foreign keys, unique constraints, and indexes.
- Use `is_active` for master records instead of deleting data that might be referenced.
- Use `@Transactional` for multi-table operations.
- Add database migrations using Flyway after the core schema is stable.

## Core Entity Design

### Security and Users

```text
Role
- id
- name
- description

User
- id
- fullName
- email
- phone
- passwordHash
- isActive
- createdAt
- updatedAt

UserRole
- userId
- roleId

PasswordResetToken
- id
- userId
- token
- expiresAt
- used
```

### Table Reservation Module

```text
RestaurantTable
- id
- tableNumber
- capacity
- location
- currentStatus
- isActive
- createdAt
- updatedAt

TableReservation
- id
- bookingReference
- customerId
- tableId
- reservationDate
- startTime
- endTime
- guestCount
- seatingPreference
- specialRequest
- status
- contactName
- contactPhone
- cancelReason
- createdAt
- updatedAt
```

Enums:

```text
TableStatus:
AVAILABLE
RESERVED
OCCUPIED
OUT_OF_SERVICE

ReservationStatus:
PENDING
CONFIRMED
CHECKED_IN
COMPLETED
CANCELLED
NO_SHOW
```

### Menu and Orders

```text
MenuCategory
- id
- name
- description
- displayOrder
- isActive

MenuItem
- id
- categoryId
- name
- description
- price
- imageUrl
- preparationMinutes
- isAvailable
- isActive

FoodOrder
- id
- orderReference
- customerId
- tableId
- reservationId
- orderType
- status
- specialNote
- subtotal
- createdAt
- updatedAt

FoodOrderItem
- id
- orderId
- menuItemId
- itemNameSnapshot
- unitPriceSnapshot
- quantity
- specialNote
- lineTotal
```

### Events

```text
EventHall
- id
- name
- capacity
- location
- description
- isActive

EventPackage
- id
- name
- eventType
- description
- basePrice
- minimumGuests
- maximumGuests
- isActive

EventBooking
- id
- bookingReference
- customerId
- hallId
- packageId
- eventDate
- startTime
- endTime
- guestCount
- specialRequirements
- status
- rejectionReason
- depositAmount
- createdAt
- updatedAt
```

### Billing and Payments

```text
Invoice
- id
- invoiceNumber
- customerId
- foodOrderId
- eventBookingId
- invoiceType
- subtotal
- serviceCharge
- taxAmount
- discountAmount
- totalAmount
- status
- issuedAt

InvoiceItem
- id
- invoiceId
- description
- quantity
- unitPrice
- lineTotal

Payment
- id
- paymentReference
- invoiceId
- amount
- method
- status
- paidAt
- gatewayReference
```

### Inventory

```text
InventoryItem
- id
- name
- unit
- currentQuantity
- reorderLevel
- isActive

MenuItemIngredient
- id
- menuItemId
- inventoryItemId
- quantityRequired

Supplier
- id
- name
- contactPerson
- phone
- email
- address
- isActive

PurchaseOrder
- id
- poNumber
- supplierId
- status
- orderedAt
- receivedAt
- notes

PurchaseOrderItem
- id
- purchaseOrderId
- inventoryItemId
- quantityOrdered
- quantityReceived
- unitCost

StockMovement
- id
- inventoryItemId
- movementType
- quantityChange
- referenceType
- referenceId
- note
- createdAt
```

### Staff

```text
StaffProfile
- id
- userId
- employeeCode
- jobTitle
- employmentStatus
- joinedDate

Shift
- id
- shiftDate
- startTime
- endTime
- roleRequired
- requiredStaffCount
- status

ShiftAssignment
- id
- shiftId
- staffId
- assignedRole
- status

AttendanceRecord
- id
- shiftAssignmentId
- checkInAt
- checkOutAt
- attendanceStatus
```

### Shared Entities

```text
Notification
- id
- userId
- title
- message
- type
- isRead
- createdAt

Feedback
- id
- customerId
- foodOrderId
- eventBookingId
- rating
- comment
- createdAt

AuditLog
- id
- userId
- action
- entityName
- entityId
- oldValue
- newValue
- createdAt
```

## Critical Reservation Requirements

Table Reservation Management is the primary assigned module.

The customer must be able to:

1. Search tables using date, time, party size, and optional seating preference.
2. View available suitable tables.
3. Receive alternative available times if no table is free.
4. Create a reservation with contact details and special requests.
5. View personal reservations.
6. Modify a future reservation.
7. Cancel a future reservation.
8. Receive an in-app confirmation notification.

Restaurant staff must be able to:

1. View the daily reservation calendar.
2. View table status and live floor state.
3. Check in an arriving customer.
4. Mark the table occupied.
5. Complete a reservation and release the table.
6. Mark absent customers as no-show and release the table.
7. Set a table out of service.

### Reservation Rules

1. A reservation cannot be made for a past date/time.
2. `guestCount` must be greater than zero.
3. Assigned table capacity must be at least the guest count.
4. A table marked `OUT_OF_SERVICE` cannot be reserved.
5. The backend must prevent overlap; React validation alone is insufficient.
6. Active statuses that block time slots are `PENDING`, `CONFIRMED`, and `CHECKED_IN`.
7. `CANCELLED`, `COMPLETED`, and `NO_SHOW` do not block availability.
8. A customer can only view, edit, or cancel their own reservations.
9. Reservation creation, table assignment, and notification creation must be transactional.
10. A reservation reference must be unique, for example `RES-20260913-A7F3`.
11. Normal duration should initially be configurable, with a default of 120 minutes.
12. The staff check-in process changes:
    - reservation to `CHECKED_IN`
    - table status to `OCCUPIED`
13. Completing a dining session changes:
    - reservation to `COMPLETED`
    - table status to `AVAILABLE`

## Reservation Endpoints

```text
GET    /api/reservations/availability?date=2026-09-20&time=19:00&guests=4&preference=WINDOW

POST   /api/reservations
GET    /api/reservations/my
GET    /api/reservations/{id}
PUT    /api/reservations/{id}
PATCH  /api/reservations/{id}/cancel

GET    /api/admin/reservations?date=2026-09-20&status=CONFIRMED
PATCH  /api/admin/reservations/{id}/check-in
PATCH  /api/admin/reservations/{id}/complete
PATCH  /api/admin/reservations/{id}/no-show

GET    /api/admin/tables
POST   /api/admin/tables
PUT    /api/admin/tables/{id}
PATCH  /api/admin/tables/{id}/status
DELETE /api/admin/tables/{id}
```

Example reservation request:

```json
{
  "tableId": 3,
  "reservationDate": "2026-09-20",
  "startTime": "19:00",
  "guestCount": 4,
  "seatingPreference": "WINDOW",
  "specialRequest": "Birthday dinner",
  "contactName": "Test Customer",
  "contactPhone": "0771234567"
}
```

## Cross-Module Rules

1. A food order deducts ingredient stock according to `MenuItemIngredient`.
2. If inventory falls below `reorderLevel`, create a low-stock notification for the inventory manager.
3. Menu items cannot be ordered when marked unavailable or ingredients are insufficient.
4. Event hall slots cannot overlap for confirmed/approved events.
5. Staff cannot receive overlapping shift assignments.
6. An invoice belongs to one food order or one event booking.
7. A payment always belongs to an invoice.
8. Payment integration is simulated only, with statuses such as `PENDING`, `PAID`, `FAILED`, and `REFUNDED`.
9. All significant admin actions must create an audit log.
10. Passwords must use BCrypt hashing.

## API Quality Rules

- Base path: `/api`
- Use RESTful endpoint names.
- Return correct HTTP status codes.
- `201 Created` for create requests.
- `204 No Content` for successful deletion if used.
- `400 Bad Request` for validation failures.
- `401 Unauthorized` for unauthenticated requests.
- `403 Forbidden` for unauthorized roles.
- `404 Not Found` for missing resources.
- `409 Conflict` for duplicate email, reservation overlap, event overlap, or shift conflict.
- Use a global exception handler.
- Return a consistent error response:

```json
{
  "timestamp": "2026-09-13T15:50:00",
  "status": 400,
  "error": "Validation failed",
  "message": "Guest count must be greater than zero",
  "path": "/api/reservations"
}
```

## Implementation Sequence

### Stage 1 — Foundation

1. Create the Spring Boot project using Maven.
2. Configure MySQL connection.
3. Add `application.properties` and `.env.example`.
4. Add global exception handling.
5. Add CORS configuration for `http://localhost:5173`.
6. Add Swagger/OpenAPI.
7. Create user, role, authentication, JWT, and password hashing.
8. Seed default roles and one development admin account.
9. Verify login with Postman.

### Stage 2 — Reservation Module First

1. Implement `RestaurantTable`.
2. Implement admin table CRUD.
3. Implement `TableReservation`.
4. Implement availability search.
5. Implement overlap prevention.
6. Implement reservation creation.
7. Implement customer reservation history.
8. Implement modify/cancel flow.
9. Implement staff calendar endpoints.
10. Implement check-in, complete, no-show, and table status changes.
11. Implement notification creation.
12. Add tests for normal and conflict cases.

### Stage 3 — Menu, Orders, and Inventory

1. Menu category CRUD.
2. Menu item CRUD.
3. Food order and food order item creation.
4. Kitchen order status workflow.
5. Inventory items and ingredient recipes.
6. Transactional stock deduction.
7. Low-stock notification and purchase order flow.

### Stage 4 — Events, Billing, and Staff

1. Event hall and package CRUD.
2. Event availability and event booking flow.
3. Approval and rejection processing.
4. Invoice generation from orders/events.
5. Simulated payment processing.
6. Staff profiles, shifts, assignments, conflicts, and attendance.

### Stage 5 — Reports and Demo Readiness

1. Admin dashboard.
2. Daily sales report.
3. Reservation report.
4. Inventory low-stock report.
5. Event booking report.
6. Seed realistic demonstration data.
7. Test every full workflow from React to MySQL.
8. Write screenshots and technical documentation.

## Testing Requirements

At minimum, add tests for:

- Registration rejects duplicate emails.
- Login rejects wrong credentials.
- A customer cannot access another customer's reservation.
- Table availability excludes overlapping active reservations.
- A table cannot be reserved while out of service.
- Reservation cancellation releases availability.
- Check-in changes both reservation and table status.
- Menu item cannot be ordered when unavailable.
- Insufficient inventory rejects an order.
- An event hall cannot have overlapping approved bookings.
- A staff member cannot have overlapping shifts.
- Payment status updates invoice status correctly.

## Design Patterns for Assessment

Implement and be able to explain at least two patterns:

1. **Strategy Pattern**
   - Use for payment methods such as `CashPaymentStrategy`, `MockOnlinePaymentStrategy`, and `CardPaymentStrategy`.
   - Benefit: new payment methods can be added without changing the payment service heavily.

2. **Factory Pattern**
   - Use `NotificationFactory` to create reservation, payment, order-status, event, and low-stock notifications.
   - Benefit: centralized and consistent notification creation.

Optional third pattern:

3. **Observer Pattern**
   - Use Spring application events for actions such as `ReservationCreatedEvent` or `OrderPlacedEvent`.
   - Listeners can create notifications and audit records without tightly coupling modules.

## Git Workflow

Use these branches:

```text
main
develop
feature/auth-core
feature/table-reservations
feature/menu-orders
feature/event-bookings
feature/billing-payments
feature/inventory
feature/staff-scheduling
```

Rules:

- Never code directly on `main`.
- Keep each commit small and meaningful.
- Run tests before merging to `develop`.
- Use pull requests for feature merges.
- Do not merge untested generated code.
- Do not commit `.env`, IDE files, build folders, or credentials.

Suggested commit messages:

```text
feat(auth): add JWT login and registration
feat(reservations): add table availability search
feat(reservations): prevent overlapping reservations
feat(menu): add menu item CRUD
feat(inventory): add low-stock detection
test(reservations): cover booking conflict scenarios
docs: update backend API documentation
```

## How Claude Code Must Work

Claude Code is the main engineering coordinator and reviewer.

For each task:

1. Read the relevant files first.
2. State the precise task scope.
3. Check existing code and avoid duplicate classes or conflicting designs.
4. Break large work into small tasks.
5. Delegate only a small, well-defined coding task to Codex if Codex is available.
6. Require Codex to return:
   - files created or changed
   - short implementation summary
   - commands to run
   - test results
   - unresolved issues
7. Review all Codex changes for:
   - compilation errors
   - package naming
   - security
   - validation
   - DTO usage
   - transaction correctness
   - authorization rules
   - database relationships
   - adherence to this document
8. Run or request unit tests.
9. Explain what changed in concise technical language.
10. Do not make unrelated refactors.

## Codex Delegation Rules

When delegating to Codex:

- Assign one bounded task only.
- Do not ask it to build the whole system.
- Provide relevant existing files and expected endpoints.
- Require it to avoid unrelated modules.
- Require tests.
- Require no placeholders or fake completed logic.
- Claude Code must review before accepting any changes.

Example prompt:

```text
Implement only the RestaurantTable module in the existing Spring Boot project.

Create:
- RestaurantTable entity
- TableStatus enum
- request/response DTOs
- repository
- service
- controller
- admin-only CRUD endpoints
- Bean Validation
- unit tests

Do not edit authentication, reservation logic, frontend, or unrelated modules.
Use constructor injection and DTOs only.
Return a concise list of modified files, test command, test output, and known limitations.
```

## Required First Task

Start only with backend foundation and reservation table management.

Create and verify:

1. Spring Boot Maven project.
2. MySQL connection.
3. Basic health endpoint:
   `GET /api/health`
4. Role and user foundations.
5. `RestaurantTable` entity and CRUD endpoints.
6. Seed 8–12 restaurant tables with different capacities and locations.
7. Swagger endpoint documentation.
8. Unit tests for table CRUD validation.

Do not begin React pages until the health endpoint, database connection, authentication foundation, and restaurant table endpoints work correctly.

## Definition of Done

A feature is complete only when:

- Code compiles.
- Database migration/schema works.
- API endpoint works in Postman/Swagger.
- Validation errors are handled.
- Authorization is enforced.
- Relevant unit tests pass.
- Data is stored and retrieved from MySQL.
- No secrets are committed.
- The developer can explain the implementation and business rules.