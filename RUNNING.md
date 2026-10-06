# Run the final team project

The integrated project uses Java 21 source compatibility, Spring Boot 3.3.4,
MySQL 8.0, and the existing React/Vite frontend. Use JDK 21 for the team baseline.
Frontend: http://localhost:5174. Backend: http://localhost:8081.
Swagger: http://localhost:8081/swagger-ui.html.

## Start the application

1. Copy `restaurant-event-backend/.env.example` to `.env` and fill in your own
   database passwords and JWT secret. Keep `.env` outside Git.
2. From the repository root, start MySQL:

   ```sh
   docker compose --env-file restaurant-event-backend/.env up -d --wait
   ```
3. Start the backend in one terminal:

   ```sh
   cd restaurant-event-backend
   mvn spring-boot:run
   ```
4. Start the frontend in another terminal:

   ```sh
   cd restaurant-event-frontend
   npm ci
   npm run dev
   ```

The frontend proxies `/api` to backend port 8081. `FRONTEND_URL` defaults to 5174
for password reset links. Registration and authentication use the backend; there
is no application-wide demo-mode switch in this version. Browser storage keeps
UI preferences, saved spaces and the current account's bag. Operational records
use the API and database.

## Accounts and screens

Register a customer at `/register` and sign in at `/login`. Demo users are
opt-in through `SEED_DEMO_USERS=true`; keep it false for normal team data.
Staff roles control access to `/admin`, and the backend enforces permissions.
Staff can manage tables and reservations, menus, food requests, kitchen orders,
events, customer payments, cashier invoices, inventory, suppliers, staff shifts,
users, and reports according to their assigned role.

Password recovery uses logged reset links for this university implementation.
`EXPOSE_RESET_LINK=true` additionally returns links for local demonstrations.
Card payments use a simulated gateway, not a real card processor.

## Database scripts

Hibernate creates or updates the schema on startup. The numbered SQL scripts
are manually run setup/data scripts, not an automatic migration runner. See
[INTEGRATION_NOTES.md](docs/INTEGRATION_NOTES.md) for prerequisites and data scripts.
Select `restaurant_event_db` before executing scripts that rely on `DATABASE()`.
No existing local database was migrated during source integration.

## Verify source changes

From `restaurant-event-frontend`, run `npm run build` and `npm test`.
From `restaurant-event-backend`, run `mvn clean test` with JDK 21. Tests use H2
and do not need the MySQL container. On the JDK 25 available during integration,
the verified command was:

```sh
mvn -o clean test -DargLine=-Dnet.bytebuddy.experimental=true
```

Offline mode requires already cached Maven dependencies; omit `-o` on a fresh
machine. The extra Byte Buddy property is for the newer test JVM.
A clean Maven build also replaces stale IDE-generated classes.

On this Mac, frontend dependencies are stored in ignored `node_modules.nosync`
and linked from `node_modules`. The old cloud-backed dependency directory is
preserved in ignored `node_modules.cloud-backup`. These directories are not
part of the repository; a fresh checkout uses `npm ci` normally.
