-- Remove retired phone SMS storage. Reservation contact numbers are preserved.
DROP TABLE IF EXISTS payment_sms;
SET @column_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='customer_payments' AND COLUMN_NAME='sms_status');
SET @migration = IF(@column_exists > 0, 'ALTER TABLE customer_payments DROP COLUMN sms_status', 'DO 0');
PREPARE statement FROM @migration;
EXECUTE statement;
DEALLOCATE PREPARE statement;
SET @column_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='customer_payments' AND COLUMN_NAME='contact_phone');
SET @migration = IF(@column_exists > 0, 'ALTER TABLE customer_payments DROP COLUMN contact_phone', 'DO 0');
PREPARE statement FROM @migration;
EXECUTE statement;
DEALLOCATE PREPARE statement;
