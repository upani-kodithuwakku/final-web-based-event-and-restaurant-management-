-- Additive migration; run after 01_create_database.sql or Hibernate schema creation.
-- Safe to rerun.
USE restaurant_event_db;
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
  INDEX idx_food_request_status (status, created_at)
);

-- Index customer history first so MySQL does not add a duplicate FK index.
SET @idx := (SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'food_requests' AND INDEX_NAME = 'idx_food_request_customer');
SET @sql := IF(@idx = 0,
  'CREATE INDEX idx_food_request_customer ON food_requests (customer_id, created_at)',
  'DO 0');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Hibernate may create the table first without foreign keys, so add each
-- key only when it is missing.
SET @fk := (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'food_requests' AND CONSTRAINT_NAME = 'fk_freq_customer');
SET @sql := IF(@fk = 0,
  'ALTER TABLE food_requests ADD CONSTRAINT fk_freq_customer FOREIGN KEY (customer_id) REFERENCES users(id)',
  'DO 0');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @fk := (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'food_requests' AND CONSTRAINT_NAME = 'fk_freq_menu_item');
SET @sql := IF(@fk = 0,
  'ALTER TABLE food_requests ADD CONSTRAINT fk_freq_menu_item FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)',
  'DO 0');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @fk := (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'food_requests' AND CONSTRAINT_NAME = 'fk_freq_resolved_by');
SET @sql := IF(@fk = 0,
  'ALTER TABLE food_requests ADD CONSTRAINT fk_freq_resolved_by FOREIGN KEY (resolved_by) REFERENCES users(id)',
  'DO 0');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
