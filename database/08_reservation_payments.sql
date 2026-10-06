-- Reservation deposits in the existing customer payment system. Safe to rerun.
-- Run after 04_customer_payments.sql.
USE restaurant_event_db;

SET @col := (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'customer_payments' AND COLUMN_NAME = 'table_reservation_id');
SET @sql := IF(@col = 0,
  'ALTER TABLE customer_payments ADD COLUMN table_reservation_id BIGINT NULL', 'DO 0');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx := (SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'customer_payments' AND INDEX_NAME = 'uk_cpay_reservation');
SET @sql := IF(@idx = 0,
  'ALTER TABLE customer_payments ADD UNIQUE KEY uk_cpay_reservation (table_reservation_id)', 'DO 0');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @fk := (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'customer_payments' AND CONSTRAINT_NAME = 'fk_cpay_reservation');
SET @sql := IF(@fk = 0,
  'ALTER TABLE customer_payments ADD CONSTRAINT fk_cpay_reservation FOREIGN KEY (table_reservation_id) REFERENCES table_reservations(id)', 'DO 0');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
