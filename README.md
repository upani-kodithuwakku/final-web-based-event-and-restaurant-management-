# Restaurant and event management

## Local setup

This copy uses Docker MySQL on 127.0.0.1:3306, Spring Boot on 8081,
and the Vite frontend on 5174 to avoid other local installations.
Requirements: Docker Desktop, Java 21 or newer, Maven, and Node/npm.
Spring Boot remains 3.3.4 with its managed MySQL JDBC driver. The database
uses MySQL 8.0 for Workbench compatibility.

1. Keep your existing `restaurant-event-backend/.env`. For a new checkout,
   copy `.env.example` to `.env` and replace both password placeholders
   and JWT_SECRET. Use DB_USERNAME=restaurant_app.
2. From the project root start the database:

   ```sh
   docker compose --env-file restaurant-event-backend/.env up -d --wait
   ```
3. In one terminal:

   ```sh
   cd restaurant-event-backend
   mvn spring-boot:run
   ```
4. In another terminal:

   ```sh
   cd restaurant-event-frontend
   npm install
   npm run dev
   ```

Open http://localhost:5174. API documentation: http://localhost:8081/swagger-ui.html.
The frontend proxies `/api` to port 8081.

## MySQL Workbench

Create a Standard TCP/IP connection named Restaurant Docker:
- Host: 127.0.0.1
- Port: 3306
- Username: restaurant_app
- Password: DB_PASSWORD from restaurant-event-backend/.env
- Default schema: restaurant_event_db

This is a new database. Existing Homebrew/MySQL and Microsoft SQL Server data
have not been migrated or deleted. Demo accounts are disabled by default.
Docker keeps this database in a named volume. Stop it without deleting data:

```sh
docker compose --env-file restaurant-event-backend/.env stop
```

Do not use `down -v` unless you intend to erase this database. Initialization
password variables apply only when the database volume is first created;
changing `.env` later does not change an existing database user's password.

## Menu, bag, and food requests

Guests can browse /menu. Customers sign in to add, remove, clear, or undo bag
changes, and submit dine-in or takeaway orders with notes for the kitchen.
The bag survives page reloads in the same browser tab and is separated by
customer account. Orders and requests are saved in MySQL. The kitchen receives
orders through its existing queue; Food requests in the staff sidebar lets
waiters, kitchen staff, managers, and admins resolve customer requests.

After the backend has created its tables, apply these additive scripts once
from the project root. They can be rerun without replacing existing dishes:

```sh
docker compose --env-file restaurant-event-backend/.env exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -u root "$MYSQL_DATABASE"' < database/02_food_requests.sql
docker compose --env-file restaurant-event-backend/.env exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -u root "$MYSQL_DATABASE"' < database/03_menu_items.sql
```

The menu seed contains the six existing dishes and prices from frontend
src/data.ts. Review those prices before using the menu for real orders.
Hibernate also creates the food_requests table on backend startup.
Restart the backend after Java changes; Vite reloads frontend changes.
