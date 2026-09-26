-- ============================================================
-- Restaurant & Event Management System
-- Database Creation Script
-- Run this in MySQL Workbench before starting the backend
-- ============================================================

CREATE DATABASE IF NOT EXISTS restaurant_event_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE restaurant_event_db;

-- ============================================================
-- SECURITY & USERS
-- ============================================================

CREATE TABLE IF NOT EXISTS roles (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(50)  NOT NULL UNIQUE,
  description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS users (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  full_name     VARCHAR(100)  NOT NULL,
  email         VARCHAR(150)  NOT NULL UNIQUE,
  phone         VARCHAR(20),
  password_hash VARCHAR(255)  NOT NULL,
  is_active     TINYINT(1)    NOT NULL DEFAULT 1,
  created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_users_email (email)
);

CREATE TABLE IF NOT EXISTS user_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id    BIGINT       NOT NULL,
  token      VARCHAR(255) NOT NULL UNIQUE,
  expires_at DATETIME     NOT NULL,
  used       TINYINT(1)   NOT NULL DEFAULT 0,
  CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================
-- TABLE RESERVATIONS
-- ============================================================

CREATE TABLE IF NOT EXISTS restaurant_tables (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  table_number   VARCHAR(10)  NOT NULL UNIQUE,
  capacity       INT          NOT NULL,
  location       VARCHAR(50),
  current_status VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE',
  is_active      TINYINT(1)   NOT NULL DEFAULT 1,
  created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS table_reservations (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY,
  booking_reference VARCHAR(30)  NOT NULL UNIQUE,
  customer_id       BIGINT       NOT NULL,
  table_id          BIGINT       NOT NULL,
  reservation_date  DATE         NOT NULL,
  start_time        TIME         NOT NULL,
  end_time          TIME         NOT NULL,
  guest_count       INT          NOT NULL,
  seating_preference VARCHAR(30),
  special_request   TEXT,
  status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
  contact_name      VARCHAR(100) NOT NULL,
  contact_phone     VARCHAR(20)  NOT NULL,
  cancel_reason     TEXT,
  created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_tr_customer FOREIGN KEY (customer_id) REFERENCES users(id),
  CONSTRAINT fk_tr_table    FOREIGN KEY (table_id)    REFERENCES restaurant_tables(id),
  INDEX idx_tr_date_status (reservation_date, status),
  INDEX idx_tr_customer    (customer_id)
);

-- ============================================================
-- MENU & ORDERS
-- ============================================================

CREATE TABLE IF NOT EXISTS menu_categories (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(100) NOT NULL UNIQUE,
  description   VARCHAR(255),
  display_order INT          NOT NULL DEFAULT 0,
  is_active     TINYINT(1)   NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS menu_items (
  id                   BIGINT          AUTO_INCREMENT PRIMARY KEY,
  category_id          BIGINT          NOT NULL,
  name                 VARCHAR(150)    NOT NULL,
  description          TEXT,
  price                DECIMAL(12,2)   NOT NULL,
  image_url            VARCHAR(500),
  preparation_minutes  INT             NOT NULL DEFAULT 15,
  is_available         TINYINT(1)      NOT NULL DEFAULT 1,
  is_active            TINYINT(1)      NOT NULL DEFAULT 1,
  CONSTRAINT fk_mi_category FOREIGN KEY (category_id) REFERENCES menu_categories(id)
);

CREATE TABLE IF NOT EXISTS food_orders (
  id               BIGINT        AUTO_INCREMENT PRIMARY KEY,
  order_reference  VARCHAR(30)   NOT NULL UNIQUE,
  customer_id      BIGINT        NOT NULL,
  table_id         BIGINT,
  reservation_id   BIGINT,
  order_type       VARCHAR(20)   NOT NULL,
  status           VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
  special_note     TEXT,
  subtotal         DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_fo_customer    FOREIGN KEY (customer_id)   REFERENCES users(id),
  CONSTRAINT fk_fo_table       FOREIGN KEY (table_id)      REFERENCES restaurant_tables(id),
  CONSTRAINT fk_fo_reservation FOREIGN KEY (reservation_id) REFERENCES table_reservations(id)
);

CREATE TABLE IF NOT EXISTS food_order_items (
  id                  BIGINT        AUTO_INCREMENT PRIMARY KEY,
  order_id            BIGINT        NOT NULL,
  menu_item_id        BIGINT        NOT NULL,
  item_name_snapshot  VARCHAR(150)  NOT NULL,
  unit_price_snapshot DECIMAL(12,2) NOT NULL,
  quantity            INT           NOT NULL,
  special_note        TEXT,
  line_total          DECIMAL(12,2) NOT NULL,
  CONSTRAINT fk_foi_order     FOREIGN KEY (order_id)     REFERENCES food_orders(id) ON DELETE CASCADE,
  CONSTRAINT fk_foi_menu_item FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
);

-- ============================================================
-- EVENTS
-- ============================================================

CREATE TABLE IF NOT EXISTS event_halls (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(100) NOT NULL UNIQUE,
  capacity    INT          NOT NULL,
  location    VARCHAR(100),
  description TEXT,
  is_active   TINYINT(1)   NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS event_packages (
  id             BIGINT        AUTO_INCREMENT PRIMARY KEY,
  name           VARCHAR(100)  NOT NULL,
  event_type     VARCHAR(50)   NOT NULL,
  description    TEXT,
  base_price     DECIMAL(12,2) NOT NULL,
  minimum_guests INT           NOT NULL DEFAULT 1,
  maximum_guests INT           NOT NULL DEFAULT 500,
  is_active      TINYINT(1)    NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS event_bookings (
  id                   BIGINT        AUTO_INCREMENT PRIMARY KEY,
  booking_reference    VARCHAR(30)   NOT NULL UNIQUE,
  customer_id          BIGINT        NOT NULL,
  hall_id              BIGINT        NOT NULL,
  package_id           BIGINT        NOT NULL,
  event_date           DATE          NOT NULL,
  start_time           TIME          NOT NULL,
  end_time             TIME          NOT NULL,
  guest_count          INT           NOT NULL,
  special_requirements TEXT,
  status               VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
  rejection_reason     TEXT,
  deposit_amount       DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  created_at           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_eb_customer FOREIGN KEY (customer_id) REFERENCES users(id),
  CONSTRAINT fk_eb_hall     FOREIGN KEY (hall_id)     REFERENCES event_halls(id),
  CONSTRAINT fk_eb_package  FOREIGN KEY (package_id)  REFERENCES event_packages(id),
  INDEX idx_eb_date_status (event_date, status)
);

-- ============================================================
-- BILLING & PAYMENTS
-- ============================================================

CREATE TABLE IF NOT EXISTS invoices (
  id               BIGINT        AUTO_INCREMENT PRIMARY KEY,
  invoice_number   VARCHAR(30)   NOT NULL UNIQUE,
  customer_id      BIGINT        NOT NULL,
  food_order_id    BIGINT,
  event_booking_id BIGINT,
  invoice_type     VARCHAR(20)   NOT NULL,
  subtotal         DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  service_charge   DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  tax_amount       DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  discount_amount  DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  total_amount     DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  status           VARCHAR(20)   NOT NULL DEFAULT 'UNPAID',
  issued_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_inv_customer      FOREIGN KEY (customer_id)      REFERENCES users(id),
  CONSTRAINT fk_inv_food_order    FOREIGN KEY (food_order_id)    REFERENCES food_orders(id),
  CONSTRAINT fk_inv_event_booking FOREIGN KEY (event_booking_id) REFERENCES event_bookings(id)
);

CREATE TABLE IF NOT EXISTS invoice_items (
  id          BIGINT        AUTO_INCREMENT PRIMARY KEY,
  invoice_id  BIGINT        NOT NULL,
  description VARCHAR(255)  NOT NULL,
  quantity    INT           NOT NULL DEFAULT 1,
  unit_price  DECIMAL(12,2) NOT NULL,
  line_total  DECIMAL(12,2) NOT NULL,
  CONSTRAINT fk_ii_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS payments (
  id                BIGINT        AUTO_INCREMENT PRIMARY KEY,
  payment_reference VARCHAR(30)   NOT NULL UNIQUE,
  invoice_id        BIGINT        NOT NULL,
  amount            DECIMAL(12,2) NOT NULL,
  method            VARCHAR(20)   NOT NULL,
  status            VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
  paid_at           DATETIME,
  gateway_reference VARCHAR(100),
  CONSTRAINT fk_pay_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id)
);

-- ============================================================
-- INVENTORY
-- ============================================================

CREATE TABLE IF NOT EXISTS inventory_items (
  id               BIGINT         AUTO_INCREMENT PRIMARY KEY,
  name             VARCHAR(150)   NOT NULL UNIQUE,
  unit             VARCHAR(30)    NOT NULL,
  current_quantity DECIMAL(12,3)  NOT NULL DEFAULT 0.000,
  reorder_level    DECIMAL(12,3)  NOT NULL DEFAULT 0.000,
  is_active        TINYINT(1)     NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS menu_item_ingredients (
  id                BIGINT        AUTO_INCREMENT PRIMARY KEY,
  menu_item_id      BIGINT        NOT NULL,
  inventory_item_id BIGINT        NOT NULL,
  quantity_required DECIMAL(12,3) NOT NULL,
  UNIQUE KEY uq_mi_inv (menu_item_id, inventory_item_id),
  CONSTRAINT fk_mii_menu_item      FOREIGN KEY (menu_item_id)      REFERENCES menu_items(id)      ON DELETE CASCADE,
  CONSTRAINT fk_mii_inventory_item FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id)
);

CREATE TABLE IF NOT EXISTS suppliers (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  name           VARCHAR(150) NOT NULL,
  contact_person VARCHAR(100),
  phone          VARCHAR(20),
  email          VARCHAR(150),
  address        TEXT,
  is_active      TINYINT(1)   NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS purchase_orders (
  id          BIGINT      AUTO_INCREMENT PRIMARY KEY,
  po_number   VARCHAR(30) NOT NULL UNIQUE,
  supplier_id BIGINT      NOT NULL,
  status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  ordered_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  received_at DATETIME,
  notes       TEXT,
  CONSTRAINT fk_po_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
);

CREATE TABLE IF NOT EXISTS purchase_order_items (
  id                 BIGINT        AUTO_INCREMENT PRIMARY KEY,
  purchase_order_id  BIGINT        NOT NULL,
  inventory_item_id  BIGINT        NOT NULL,
  quantity_ordered   DECIMAL(12,3) NOT NULL,
  quantity_received  DECIMAL(12,3) NOT NULL DEFAULT 0.000,
  unit_cost          DECIMAL(12,2) NOT NULL,
  CONSTRAINT fk_poi_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id) ON DELETE CASCADE,
  CONSTRAINT fk_poi_item  FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id)
);

CREATE TABLE IF NOT EXISTS stock_movements (
  id               BIGINT        AUTO_INCREMENT PRIMARY KEY,
  inventory_item_id BIGINT       NOT NULL,
  movement_type    VARCHAR(20)   NOT NULL,
  quantity_change  DECIMAL(12,3) NOT NULL,
  reference_type   VARCHAR(50),
  reference_id     BIGINT,
  note             TEXT,
  created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_sm_item FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id)
);

-- ============================================================
-- STAFF
-- ============================================================

CREATE TABLE IF NOT EXISTS staff_profiles (
  id                BIGINT      AUTO_INCREMENT PRIMARY KEY,
  user_id           BIGINT      NOT NULL UNIQUE,
  employee_code     VARCHAR(20) NOT NULL UNIQUE,
  job_title         VARCHAR(100),
  employment_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  joined_date       DATE,
  CONSTRAINT fk_sp_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS shifts (
  id                   BIGINT      AUTO_INCREMENT PRIMARY KEY,
  shift_date           DATE        NOT NULL,
  start_time           TIME        NOT NULL,
  end_time             TIME        NOT NULL,
  role_required        VARCHAR(50) NOT NULL,
  required_staff_count INT         NOT NULL DEFAULT 1,
  status               VARCHAR(20) NOT NULL DEFAULT 'OPEN',
  INDEX idx_shift_date (shift_date)
);

CREATE TABLE IF NOT EXISTS shift_assignments (
  id            BIGINT      AUTO_INCREMENT PRIMARY KEY,
  shift_id      BIGINT      NOT NULL,
  staff_id      BIGINT      NOT NULL,
  assigned_role VARCHAR(50) NOT NULL,
  status        VARCHAR(20) NOT NULL DEFAULT 'ASSIGNED',
  UNIQUE KEY uq_shift_staff (shift_id, staff_id),
  CONSTRAINT fk_sa_shift FOREIGN KEY (shift_id) REFERENCES shifts(id) ON DELETE CASCADE,
  CONSTRAINT fk_sa_staff FOREIGN KEY (staff_id) REFERENCES staff_profiles(id)
);

CREATE TABLE IF NOT EXISTS attendance_records (
  id                  BIGINT      AUTO_INCREMENT PRIMARY KEY,
  shift_assignment_id BIGINT      NOT NULL UNIQUE,
  check_in_at         DATETIME,
  check_out_at        DATETIME,
  attendance_status   VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  CONSTRAINT fk_ar_assignment FOREIGN KEY (shift_assignment_id) REFERENCES shift_assignments(id) ON DELETE CASCADE
);

-- ============================================================
-- SHARED / CROSS-MODULE
-- ============================================================

CREATE TABLE IF NOT EXISTS notifications (
  id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
  user_id    BIGINT       NOT NULL,
  title      VARCHAR(150) NOT NULL,
  message    TEXT         NOT NULL,
  type       VARCHAR(50)  NOT NULL,
  is_read    TINYINT(1)   NOT NULL DEFAULT 0,
  created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id),
  INDEX idx_notif_user_read (user_id, is_read)
);

CREATE TABLE IF NOT EXISTS feedback (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  customer_id      BIGINT NOT NULL,
  food_order_id    BIGINT,
  event_booking_id BIGINT,
  rating           INT    NOT NULL,
  comment          TEXT,
  created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_fb_customer      FOREIGN KEY (customer_id)      REFERENCES users(id),
  CONSTRAINT fk_fb_food_order    FOREIGN KEY (food_order_id)    REFERENCES food_orders(id),
  CONSTRAINT fk_fb_event_booking FOREIGN KEY (event_booking_id) REFERENCES event_bookings(id)
);

CREATE TABLE IF NOT EXISTS audit_logs (
  id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
  user_id     BIGINT,
  action      VARCHAR(100) NOT NULL,
  entity_name VARCHAR(100) NOT NULL,
  entity_id   BIGINT,
  old_value   TEXT,
  new_value   TEXT,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_audit_entity (entity_name, entity_id)
);

-- ============================================================
-- SEED DATA
-- ============================================================

-- Roles
INSERT IGNORE INTO roles (name, description) VALUES
  ('CUSTOMER',           'Restaurant customer'),
  ('ADMIN',              'System administrator'),
  ('MANAGER',            'Restaurant manager'),
  ('WAITER',             'Floor staff - waiter'),
  ('KITCHEN_STAFF',      'Kitchen preparation staff'),
  ('EVENT_COORDINATOR',  'Event bookings coordinator'),
  ('CASHIER',            'Billing and payments staff'),
  ('INVENTORY_MANAGER',  'Inventory and supplier management');

-- Admin account (password: Admin@123 — BCrypt)
INSERT IGNORE INTO users (full_name, email, phone, password_hash, is_active)
VALUES ('System Admin', 'admin@restaurant.com', '0711000000',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPZsjCYyXVe', 1);

-- Assign ADMIN role to admin user
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'admin@restaurant.com' AND r.name = 'ADMIN';

-- Restaurant Tables (10 tables with variety)
INSERT IGNORE INTO restaurant_tables (table_number, capacity, location, current_status) VALUES
  ('T01', 2,  'WINDOW',  'AVAILABLE'),
  ('T02', 2,  'WINDOW',  'AVAILABLE'),
  ('T03', 4,  'INDOOR',  'AVAILABLE'),
  ('T04', 4,  'INDOOR',  'AVAILABLE'),
  ('T05', 4,  'OUTDOOR', 'AVAILABLE'),
  ('T06', 4,  'OUTDOOR', 'AVAILABLE'),
  ('T07', 6,  'INDOOR',  'AVAILABLE'),
  ('T08', 6,  'INDOOR',  'AVAILABLE'),
  ('T09', 8,  'PRIVATE', 'AVAILABLE'),
  ('T10', 10, 'PRIVATE', 'AVAILABLE');

-- Menu Categories
INSERT IGNORE INTO menu_categories (name, description, display_order) VALUES
  ('Starters',   'Appetizers and soups',                1),
  ('Main Course','Mains — rice, curry, and grills',     2),
  ('Desserts',   'Sweet endings',                       3),
  ('Beverages',  'Hot and cold drinks',                 4);

-- Event Halls
INSERT IGNORE INTO event_halls (name, capacity, location, description) VALUES
  ('Crystal Hall',    200, 'Ground Floor', 'Elegant banquet hall for weddings and gala dinners'),
  ('Garden Terrace',  80,  'Rooftop',      'Open-air terrace ideal for cocktail receptions'),
  ('Boardroom Suite', 30,  'First Floor',  'Private meeting and corporate dinner room');

-- Event Packages
INSERT IGNORE INTO event_packages (name, event_type, description, base_price, minimum_guests, maximum_guests) VALUES
  ('Silver Wedding',      'WEDDING',    'Standard wedding reception with buffet and décor', 250000.00, 50,  150),
  ('Gold Wedding',        'WEDDING',    'Premium wedding package with live music',          450000.00, 80,  200),
  ('Corporate Dinner',    'CORPORATE',  'Formal three-course corporate dinner',              75000.00, 20,   80),
  ('Birthday Bash',       'BIRTHDAY',   'Fun birthday setup with cake and DJ',               35000.00, 20,   60),
  ('Cocktail Reception',  'RECEPTION',  'Canape-style cocktail reception',                   50000.00, 30,   80);
