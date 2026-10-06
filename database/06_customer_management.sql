-- Customer management: password reset tokens ("Forgot password").
-- Additive migration; safe to rerun. Hibernate also creates/updates this table on startup.
USE restaurant_event_db;

CREATE TABLE IF NOT EXISTS password_reset_tokens (
  id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id    BIGINT       NOT NULL,
  token      VARCHAR(255) NOT NULL,
  expires_at DATETIME(6)  NOT NULL,
  used       TINYINT(1)   NOT NULL DEFAULT 0,
  created_at DATETIME(6)  NULL,
  UNIQUE KEY uk_prt_token (token),
  INDEX idx_prt_user (user_id),
  CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 01_create_database.sql created this table without created_at; add it when missing.
SET @col := (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'password_reset_tokens' AND COLUMN_NAME = 'created_at');
SET @sql := IF(@col = 0, 'ALTER TABLE password_reset_tokens ADD COLUMN created_at DATETIME(6) NULL', 'DO 0');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Example: see the latest reset requests (tokens are one-time and expire after 30 minutes).
-- SELECT u.email, t.expires_at, t.used, t.created_at FROM password_reset_tokens t JOIN users u ON u.id = t.user_id ORDER BY t.id DESC LIMIT 10;
