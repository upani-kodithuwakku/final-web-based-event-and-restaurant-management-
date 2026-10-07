-- Complete fresh-install schema for MySQL 8.0: 31 tables.
-- Includes current backend tables and two explicitly marked planned tables.
-- Run this entire file using the MySQL extension in VS Code, Workbench, or:
-- mysql -u root -p < database/00_full_schema.sql
-- Creates restaurant_event_db; does not drop tables or overwrite records.
-- Rerunning creates missing tables only; it does NOT upgrade existing tables.
-- Existing installations should use Hibernate update and the individual scripts.
-- Only required roles are seeded. Demo accounts/data remain opt-in.

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  full_name     VARCHAR(100)  NOT NULL,
  email         VARCHAR(150)  NOT NULL UNIQUE,
  phone         VARCHAR(20),
  password_hash VARCHAR(255)  NOT NULL,
  is_active     TINYINT(1)    NOT NULL DEFAULT 1,
  created_at    DATETIME(6)      NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at    DATETIME(6)      NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  INDEX idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS password_reset_tokens (
  created_at DATETIME(6),
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id    BIGINT       NOT NULL,
  token      VARCHAR(255) NOT NULL UNIQUE,
  expires_at DATETIME(6)     NOT NULL,
  used       TINYINT(1)   NOT NULL DEFAULT 0,
  CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE RESERVATIONS
-- ============================================================

CREATE TABLE IF NOT EXISTS restaurant_tables (
  display_name VARCHAR(255),
  description VARCHAR(255),
  image_url VARCHAR(255),
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  table_number   VARCHAR(10)  NOT NULL UNIQUE,
  capacity       INT          NOT NULL,
  location       VARCHAR(50),
  current_status VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE',
  is_active      TINYINT(1)   NOT NULL DEFAULT 1,
  created_at     DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at     DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
  created_at        DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at        DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_tr_customer FOREIGN KEY (customer_id) REFERENCES users(id),
  CONSTRAINT fk_tr_table    FOREIGN KEY (table_id)    REFERENCES restaurant_tables(id),
  INDEX idx_tr_date_status (reservation_date, status),
  INDEX idx_tr_customer    (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- MENU & ORDERS
-- ============================================================

CREATE TABLE IF NOT EXISTS menu_categories (
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(100) NOT NULL UNIQUE,
  description   VARCHAR(500),
  display_order INT          NOT NULL DEFAULT 0,
  is_active     TINYINT(1)   NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS menu_items (
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  id                   BIGINT          AUTO_INCREMENT PRIMARY KEY,
  category_id          BIGINT          NOT NULL,
  name                 VARCHAR(150)    NOT NULL,
  description          VARCHAR(1000),
  price                DECIMAL(12,2)   NOT NULL,
  image_url            VARCHAR(500),
  preparation_minutes  INT             NOT NULL DEFAULT 15,
  is_available         TINYINT(1)      NOT NULL DEFAULT 1,
  is_active            TINYINT(1)      NOT NULL DEFAULT 1,
  CONSTRAINT fk_mi_category FOREIGN KEY (category_id) REFERENCES menu_categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS food_orders (
  id               BIGINT        AUTO_INCREMENT PRIMARY KEY,
  order_reference  VARCHAR(30)   NOT NULL UNIQUE,
  customer_id      BIGINT        NOT NULL,
  table_id         BIGINT,
  reservation_id   BIGINT,
  order_type       VARCHAR(20)   NOT NULL,
  status           VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
  special_note     VARCHAR(500),
  subtotal         DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  created_at       DATETIME(6)      NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at       DATETIME(6)      NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_fo_customer    FOREIGN KEY (customer_id)   REFERENCES users(id),
  CONSTRAINT fk_fo_table       FOREIGN KEY (table_id)      REFERENCES restaurant_tables(id),
  CONSTRAINT fk_fo_reservation FOREIGN KEY (reservation_id) REFERENCES table_reservations(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS food_order_items (
  id                  BIGINT        AUTO_INCREMENT PRIMARY KEY,
  order_id            BIGINT        NOT NULL,
  menu_item_id        BIGINT        NOT NULL,
  item_name_snapshot  VARCHAR(150)  NOT NULL,
  unit_price_snapshot DECIMAL(12,2) NOT NULL,
  quantity            INT           NOT NULL,
  special_note        VARCHAR(300),
  line_total          DECIMAL(12,2) NOT NULL,
  CONSTRAINT fk_foi_order     FOREIGN KEY (order_id)     REFERENCES food_orders(id) ON DELETE CASCADE,
  CONSTRAINT fk_foi_menu_item FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- EVENTS
-- ============================================================

CREATE TABLE IF NOT EXISTS event_halls (
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(100) NOT NULL UNIQUE,
  capacity    INT          NOT NULL,
  location    VARCHAR(100),
  description VARCHAR(1000),
  is_active   TINYINT(1)   NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS event_packages (
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  id             BIGINT        AUTO_INCREMENT PRIMARY KEY,
  name           VARCHAR(100)  NOT NULL,
  event_type     VARCHAR(100),
  description    VARCHAR(1000),
  base_price     DECIMAL(12,2) NOT NULL,
  minimum_guests INT           NOT NULL DEFAULT 1,
  maximum_guests INT           NOT NULL DEFAULT 500,
  is_active      TINYINT(1)    NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
  special_requirements VARCHAR(1000),
  status               VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
  rejection_reason     VARCHAR(500),
  deposit_amount       DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  created_at           DATETIME(6)      NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at           DATETIME(6)      NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_eb_customer FOREIGN KEY (customer_id) REFERENCES users(id),
  CONSTRAINT fk_eb_hall     FOREIGN KEY (hall_id)     REFERENCES event_halls(id),
  CONSTRAINT fk_eb_package  FOREIGN KEY (package_id)  REFERENCES event_packages(id),
  INDEX idx_eb_date_status (event_date, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- BILLING & PAYMENTS
-- ============================================================

CREATE TABLE IF NOT EXISTS invoices (
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
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
  status           VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
  issued_at        DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_inv_customer      FOREIGN KEY (customer_id)      REFERENCES users(id),
  CONSTRAINT fk_inv_food_order    FOREIGN KEY (food_order_id)    REFERENCES food_orders(id),
  CONSTRAINT fk_inv_event_booking FOREIGN KEY (event_booking_id) REFERENCES event_bookings(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS invoice_items (
  id          BIGINT        AUTO_INCREMENT PRIMARY KEY,
  invoice_id  BIGINT        NOT NULL,
  description VARCHAR(300)  NOT NULL,
  quantity    INT           NOT NULL DEFAULT 1,
  unit_price  DECIMAL(12,2) NOT NULL,
  line_total  DECIMAL(12,2) NOT NULL,
  CONSTRAINT fk_ii_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payments (
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  id                BIGINT        AUTO_INCREMENT PRIMARY KEY,
  payment_reference VARCHAR(30)   NOT NULL UNIQUE,
  invoice_id        BIGINT        NOT NULL,
  amount            DECIMAL(12,2) NOT NULL,
  method            VARCHAR(20)   NOT NULL,
  status            VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
  paid_at           DATETIME(6),
  gateway_reference VARCHAR(100),
  CONSTRAINT fk_pay_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- INVENTORY
-- ============================================================

CREATE TABLE IF NOT EXISTS inventory_items (
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  id               BIGINT         AUTO_INCREMENT PRIMARY KEY,
  name             VARCHAR(150)   NOT NULL,
  unit             VARCHAR(30),
  current_quantity DECIMAL(12,3)  NOT NULL DEFAULT 0.000,
  reorder_level    DECIMAL(12,3)  NOT NULL DEFAULT 0.000,
  is_active        TINYINT(1)     NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Planned schema table; backend workflow is not implemented yet.
CREATE TABLE IF NOT EXISTS menu_item_ingredients (
  id                BIGINT        AUTO_INCREMENT PRIMARY KEY,
  menu_item_id      BIGINT        NOT NULL,
  inventory_item_id BIGINT        NOT NULL,
  quantity_required DECIMAL(12,3) NOT NULL,
  UNIQUE KEY uq_mi_inv (menu_item_id, inventory_item_id),
  CONSTRAINT fk_mii_menu_item      FOREIGN KEY (menu_item_id)      REFERENCES menu_items(id)      ON DELETE CASCADE,
  CONSTRAINT fk_mii_inventory_item FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS suppliers (
  supplied_products VARCHAR(500),
  joined_date DATE,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  name           VARCHAR(150) NOT NULL,
  contact_person VARCHAR(100),
  phone          VARCHAR(20),
  email          VARCHAR(150),
  address        VARCHAR(500),
  is_active      TINYINT(1)   NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Implemented backend workflow.
CREATE TABLE IF NOT EXISTS purchase_orders (
  id          BIGINT      AUTO_INCREMENT PRIMARY KEY,
  po_number   VARCHAR(30) NOT NULL UNIQUE,
  supplier_id BIGINT      NOT NULL,
  status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  ordered_at  DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  received_at DATETIME(6),
  notes       TEXT,
  CONSTRAINT fk_po_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Implemented backend workflow.
CREATE TABLE IF NOT EXISTS purchase_order_items (
  id                 BIGINT        AUTO_INCREMENT PRIMARY KEY,
  purchase_order_id  BIGINT        NOT NULL,
  inventory_item_id  BIGINT        NOT NULL,
  quantity_ordered   DECIMAL(12,3) NOT NULL,
  quantity_received  DECIMAL(12,3) NOT NULL DEFAULT 0.000,
  unit_cost          DECIMAL(12,2) NOT NULL,
  CONSTRAINT fk_poi_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id) ON DELETE CASCADE,
  CONSTRAINT fk_poi_item  FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS stock_movements (
  id               BIGINT        AUTO_INCREMENT PRIMARY KEY,
  inventory_item_id BIGINT       NOT NULL,
  movement_type    VARCHAR(20)   NOT NULL,
  quantity_change  DECIMAL(12,3) NOT NULL,
  reference_type   VARCHAR(50),
  reference_id     BIGINT,
  note             VARCHAR(500),
  created_at       DATETIME(6)      NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_sm_item FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- STAFF
-- ============================================================

CREATE TABLE IF NOT EXISTS staff_profiles (
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  id                BIGINT      AUTO_INCREMENT PRIMARY KEY,
  user_id           BIGINT      NOT NULL UNIQUE,
  employee_code     VARCHAR(20) NOT NULL UNIQUE,
  job_title         VARCHAR(100),
  employment_status VARCHAR(20) NOT NULL DEFAULT 'FULL_TIME',
  joined_date       DATE,
  CONSTRAINT fk_sp_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS shifts (
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  id                   BIGINT      AUTO_INCREMENT PRIMARY KEY,
  shift_date           DATE        NOT NULL,
  start_time           TIME        NOT NULL,
  end_time             TIME        NOT NULL,
  role_required        VARCHAR(50),
  required_staff_count INT         NOT NULL DEFAULT 1,
  status               VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
  INDEX idx_shift_date (shift_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS shift_assignments (
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  id            BIGINT      AUTO_INCREMENT PRIMARY KEY,
  shift_id      BIGINT      NOT NULL,
  staff_id      BIGINT      NOT NULL,
  assigned_role VARCHAR(50),
  status        VARCHAR(20) NOT NULL DEFAULT 'ASSIGNED',
  UNIQUE KEY uq_shift_staff (shift_id, staff_id),
  CONSTRAINT fk_sa_shift FOREIGN KEY (shift_id) REFERENCES shifts(id) ON DELETE CASCADE,
  CONSTRAINT fk_sa_staff FOREIGN KEY (staff_id) REFERENCES staff_profiles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Implemented backend workflow.
CREATE TABLE IF NOT EXISTS attendance_records (
  id                  BIGINT      AUTO_INCREMENT PRIMARY KEY,
  shift_assignment_id BIGINT      NOT NULL UNIQUE,
  check_in_at         DATETIME(6),
  check_out_at        DATETIME(6),
  attendance_status   VARCHAR(20) NOT NULL DEFAULT 'ABSENT',
  CONSTRAINT fk_ar_assignment FOREIGN KEY (shift_assignment_id) REFERENCES shift_assignments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
  created_at DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id),
  INDEX idx_notif_user_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Planned schema table; backend workflow is not implemented yet.
CREATE TABLE IF NOT EXISTS feedback (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  customer_id      BIGINT NOT NULL,
  food_order_id    BIGINT,
  event_booking_id BIGINT,
  rating           INT    NOT NULL,
  comment          TEXT,
  created_at       DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_fb_customer      FOREIGN KEY (customer_id)      REFERENCES users(id),
  CONSTRAINT fk_fb_food_order    FOREIGN KEY (food_order_id)    REFERENCES food_orders(id),
  CONSTRAINT fk_fb_event_booking FOREIGN KEY (event_booking_id) REFERENCES event_bookings(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS audit_logs (
  id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
  user_id     BIGINT,
  action      VARCHAR(100) NOT NULL,
  entity_name VARCHAR(100) NOT NULL,
  entity_id   BIGINT,
  old_value   TEXT,
  new_value   TEXT,
  created_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  INDEX idx_audit_entity (entity_name, entity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- CUSTOMER FOOD REQUESTS
CREATE TABLE IF NOT EXISTS food_requests (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  customer_id BIGINT NOT NULL,
  customer_name VARCHAR(100) NOT NULL,
  menu_item_id BIGINT,
  item_name VARCHAR(150),
  message VARCHAR(500) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
  resolved_by BIGINT,
  created_at DATETIME(6) NOT NULL,
  resolved_at DATETIME(6),
  INDEX idx_food_request_status (status, created_at),
  INDEX idx_food_request_customer (customer_id, created_at),
  CONSTRAINT fk_freq_customer FOREIGN KEY (customer_id) REFERENCES users(id),
  CONSTRAINT fk_freq_menu_item FOREIGN KEY (menu_item_id) REFERENCES menu_items(id),
  CONSTRAINT fk_freq_resolved_by FOREIGN KEY (resolved_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- CUSTOMER PAYMENTS (separate from cashier invoice payments)
CREATE TABLE IF NOT EXISTS customer_payments (
  id                BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  payment_reference VARCHAR(30)   NOT NULL,
  customer_id       BIGINT        NOT NULL,
  purpose           VARCHAR(20)   NOT NULL,            -- FOOD_ORDER or EVENT_BOOKING
  food_order_id     BIGINT,
  event_booking_id  BIGINT,
  table_reservation_id BIGINT,
  amount            DECIMAL(12,2) NOT NULL,
  method            VARCHAR(20)   NOT NULL,            -- CARD or PAY_AT_OUTLET
  status            VARCHAR(20)   NOT NULL,            -- PENDING, PAID, FAILED, REFUNDED
  card_holder_name  VARCHAR(100),
  card_last4        VARCHAR(4),                           -- full card numbers and CVVs are never stored
  card_brand        VARCHAR(20),
  gateway_reference VARCHAR(100),
  paid_at           DATETIME(6),
  created_at        DATETIME(6)   NOT NULL,
  updated_at        DATETIME(6)   NOT NULL,
  UNIQUE KEY uk_cpay_reference (payment_reference),
  UNIQUE KEY uk_cpay_food_order (food_order_id),       -- one payment per order
  UNIQUE KEY uk_cpay_event_booking (event_booking_id), -- one payment per booking
  INDEX idx_cpay_customer (customer_id, created_at),
  INDEX idx_cpay_status (status),
  UNIQUE KEY uk_cpay_reservation (table_reservation_id),
  CONSTRAINT fk_cpay_customer FOREIGN KEY (customer_id) REFERENCES users(id),
  CONSTRAINT fk_cpay_food_order FOREIGN KEY (food_order_id) REFERENCES food_orders(id),
  CONSTRAINT fk_cpay_event_booking FOREIGN KEY (event_booking_id) REFERENCES event_bookings(id),
  CONSTRAINT fk_cpay_reservation FOREIGN KEY (table_reservation_id) REFERENCES table_reservations(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- REQUIRED ACCESS ROLES
INSERT IGNORE INTO roles (name, description) VALUES
  ('CUSTOMER',           'Restaurant customer'),
  ('ADMIN',              'System administrator'),
  ('MANAGER',            'Restaurant manager'),
  ('WAITER',             'Floor staff - waiter'),
  ('KITCHEN_STAFF',      'Kitchen preparation staff'),
  ('EVENT_COORDINATOR',  'Event bookings coordinator'),
  ('CASHIER',            'Billing and payments staff'),
  ('INVENTORY_MANAGER',  'Inventory and supplier management');

