# Gather — Running Guide
**Group 06 · Restaurant & Event Management System**

---

## What is Demo Mode?

**Demo mode** lets you run and use the entire frontend **without a running backend or database**.

| Feature | Demo Mode ON | Demo Mode OFF |
|---------|-------------|---------------|
| Backend required | ❌ No | ✅ Yes (Spring Boot on port 8080) |
| Database required | ❌ No | ✅ Yes (MySQL on port 3306) |
| Login/Register | Instant (no password needed) | Real JWT authentication |
| Reservations | Saved in browser localStorage | Saved to MySQL via API |
| Admin actions | Work locally (localStorage) | Hit real REST endpoints |
| Data survives refresh | Yes (localStorage) | Yes (MySQL) |
| Data shared between browsers | ❌ No | ✅ Yes |

**Use demo mode** to test the UI during development or for demonstrations.  
**Turn demo mode off** when the backend is ready and you want real data persistence and multi-user login.

---

## Quick Start — Demo Mode (Frontend only, no backend needed)

### Prerequisites
- Node.js 20+ installed
- `npm` available in terminal

### VS Code — Open terminal and run:

```bash
cd restaurant-event-frontend
```

```bash
npm install
```

```bash
npm run dev
```

Open your browser at: **http://localhost:5173**

That's it. No backend, no database needed. Everything works locally.

---

## Demo Mode — How to Log In

When demo mode is active, **no real password is checked**.

1. Go to **http://localhost:5173/login**
2. Type **any email address** (e.g. `demo@gather.com`)
3. Click **"Continue with demo profile"**
4. You are logged in as a CUSTOMER

To reach the **Staff / Admin workspace**:
- Click the profile icon (top right) → **Staff workspace**
- Or go directly to **http://localhost:5173/admin**
- In demo mode, the admin area is accessible without a staff account

> All data created in demo mode (reservations, orders, events, staff, inventory) is stored in your **browser's localStorage**. It persists across page refreshes but is **private to your browser** and can be cleared from browser DevTools → Application → Local Storage → `http://localhost:5173` → Clear All.

---

## How to Remove Demo Mode (Connect to Real Backend)

### Step 1 — Create a `.env` file in the frontend folder

```bash
cd restaurant-event-frontend
cp .env.example .env
```

### Step 2 — Edit the `.env` file

Open `restaurant-event-frontend/.env` and change:

```env
VITE_DEMO_MODE=false
VITE_API_BASE_URL=/api
```

Save the file. The Vite dev server will restart automatically.

### Step 3 — Make sure the backend is running (see below)

Once `VITE_DEMO_MODE=false`:
- Login now requires a **real email and password** from the database
- All data is saved to MySQL
- The frontend proxies `/api` requests to `http://localhost:8080`

---

## Full Stack — Running Frontend + Backend + Database

### Prerequisites
- Java 21 (Homebrew: `brew install openjdk@21`)
- Maven (`brew install maven`)
- MySQL 8 running on port 3306
- Node.js 20+

---

### Step 1 — Start MySQL and create the database

Open **MySQL Workbench** or your terminal:

```sql
CREATE DATABASE IF NOT EXISTS restaurant_event_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

---

### Step 2 — Configure the backend environment

Create a `.env` file inside `restaurant-event-backend/`:

```bash
cd restaurant-event-backend
```

Create the file `restaurant-event-backend/.env`:

```env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=restaurant_event_db
DB_USERNAME=root
DB_PASSWORD=your_mysql_root_password
JWT_SECRET=a-very-long-random-secret-string-at-least-64-characters-long-here
```

> **Never commit this `.env` file.** It is listed in `.gitignore`.

---

### Step 3 — Run the backend

**In VS Code — open a new terminal:**

```bash
cd restaurant-event-backend
```

```bash
mvn spring-boot:run
```

Wait until you see:
```
Started RestaurantEventApplication in X.XXX seconds
```

Backend is now running at: **http://localhost:8080**  
Swagger UI: **http://localhost:8080/swagger-ui.html**

---

### Step 4 — Configure the frontend to use the backend

```bash
cd restaurant-event-frontend
cp .env.example .env
```

Edit `restaurant-event-frontend/.env`:

```env
VITE_DEMO_MODE=false
VITE_API_BASE_URL=/api
```

---

### Step 5 — Run the frontend

**In VS Code — open a second terminal:**

```bash
cd restaurant-event-frontend
```

```bash
npm install
```

```bash
npm run dev
```

Frontend is now running at: **http://localhost:5173**

---

## Login Credentials (Real Backend Mode)

Demo accounts are disabled by default. Required role records are initialized automatically, so admins can add real staff without sample data. Existing accounts are preserved.

For a fresh local database only, you can opt into the accounts below by setting `SEED_DEMO_USERS=true` before starting the backend. Turn it off after setup and use your existing admin account to add staff.

| Role | Email | Password |
|------|-------|----------|
| Admin | `admin@gather.com` | `Admin@1234` |
| Manager | `manager@gather.com` | `Manager@1234` |
| Waiter | `waiter@gather.com` | `Waiter@1234` |
| Kitchen Staff | `kitchen@gather.com` | `Kitchen@1234` |
| Event Coordinator | `events@gather.com` | `Events@1234` |
| Cashier | `cashier@gather.com` | `Cashier@1234` |
| Inventory Manager | `inventory@gather.com` | `Inventory@1234` |
| Customer | `customer@gather.com` | `Customer@1234` |

> With demo seeding disabled, a fresh database needs an administrator provisioned before you can access the admin area. Disabling seeding does not delete previously seeded accounts.

To **register a new customer account**, go to: **http://localhost:5173/register**

---

## VS Code — All Commands at a Glance

Open VS Code in the project root, then use **Terminal → New Terminal**:

### Frontend only (demo mode)
```bash
cd restaurant-event-frontend && npm install && npm run dev
```

### Backend only
```bash
cd restaurant-event-backend && mvn spring-boot:run
```

### Run both (two terminals side by side)

**Terminal 1 — Backend:**
```bash
cd restaurant-event-backend && mvn spring-boot:run
```

**Terminal 2 — Frontend:**
```bash
cd restaurant-event-frontend && npm run dev
```

### Build the frontend for production
```bash
cd restaurant-event-frontend && npm run build
```

### Run frontend tests
```bash
cd restaurant-event-frontend && npm test
```

### Run backend tests
```bash
cd restaurant-event-backend && mvn test
```

---

## URLs Summary

| Service | URL |
|---------|-----|
| Frontend (React) | http://localhost:5173 |
| Backend (Spring Boot) | http://localhost:8080 |
| Swagger / API Docs | http://localhost:8080/swagger-ui.html |
| Admin workspace | http://localhost:5173/admin |
| Health check | http://localhost:8080/api/health |
| Customer login | http://localhost:5173/login |
| Customer register | http://localhost:5173/register |

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `npm: command not found` | Install Node.js from https://nodejs.org |
| `java: command not found` | `brew install openjdk@21` then follow brew's path instructions |
| `Access denied for user 'root'@'localhost'` | Wrong DB_PASSWORD in `.env` — check MySQL Workbench credentials |
| Frontend shows "Unable to reach the server" | Backend is not running — start it first with `mvn spring-boot:run` |
| Port 8080 already in use | `lsof -ti:8080 | xargs kill -9` |
| Port 5173 already in use | `lsof -ti:5173 | xargs kill -9` |
| Demo data not clearing | Open browser DevTools → Application → Local Storage → Clear All |

---

*Gather · Group 06 · SLIIT 2026*
