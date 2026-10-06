-- Customer payments for confirmed food orders and event bookings.
-- Additive migration; run after 01_create_database.sql or Hibernate schema creation.
-- Safe to rerun. Separate from the cashier billing table `payments`.
USE restaurant_event_db;
CREATE TABLE IF NOT EXISTS customer_payments (
  id                BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  payment_reference VARCHAR(30)   NOT NULL,
  customer_id       BIGINT        NOT NULL,
  purpose           VARCHAR(20)   NOT NULL,            -- FOOD_ORDER or EVENT_BOOKING
  food_order_id     BIGINT,
  event_booking_id  BIGINT,
  amount            DECIMAL(12,2) NOT NULL,
  method            VARCHAR(20)   NOT NULL,            -- CARD or PAY_AT_OUTLET
  status            VARCHAR(20)   NOT NULL,            -- PENDING, PAID, FAILED, REFUNDED
  card_holder_name  VARCHAR(100),
  card_last4        CHAR(4),                           -- full card numbers and CVVs are never stored
  card_brand        VARCHAR(20),
  gateway_reference VARCHAR(100),
  paid_at           DATETIME(6),
  created_at        DATETIME(6)   NOT NULL,
  updated_at        DATETIME(6)   NOT NULL,
  UNIQUE KEY uk_cpay_reference (payment_reference),
  UNIQUE KEY uk_cpay_food_order (food_order_id),       -- one payment per order
  UNIQUE KEY uk_cpay_event_booking (event_booking_id), -- one payment per booking
  INDEX idx_cpay_customer (customer_id, created_at),
  INDEX idx_cpay_status (status)
);

-- Hibernate may create the table first without foreign keys, so add each
-- key only when it is missing.
SET @fk := (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'customer_payments' AND CONSTRAINT_NAME = 'fk_cpay_customer');
SET @sql := IF(@fk = 0,
  'ALTER TABLE customer_payments ADD CONSTRAINT fk_cpay_customer FOREIGN KEY (customer_id) REFERENCES users(id)',
  'DO 0');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @fk := (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'customer_payments' AND CONSTRAINT_NAME = 'fk_cpay_food_order');
SET @sql := IF(@fk = 0,
  'ALTER TABLE customer_payments ADD CONSTRAINT fk_cpay_food_order FOREIGN KEY (food_order_id) REFERENCES food_orders(id)',
  'DO 0');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @fk := (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'customer_payments' AND CONSTRAINT_NAME = 'fk_cpay_event_booking');
SET @sql := IF(@fk = 0,
  'ALTER TABLE customer_payments ADD CONSTRAINT fk_cpay_event_booking FOREIGN KEY (event_booking_id) REFERENCES event_bookings(id)',
  'DO 0');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
