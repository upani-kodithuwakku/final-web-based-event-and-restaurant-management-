-- Additional business details; existing supplier history is preserved.
SET @supplier_ddl = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='suppliers' AND COLUMN_NAME='supplied_products')=0,
 'ALTER TABLE suppliers ADD COLUMN supplied_products VARCHAR(500) NULL', 'SELECT 1');
PREPARE supplier_stmt FROM @supplier_ddl; EXECUTE supplier_stmt; DEALLOCATE PREPARE supplier_stmt;
SET @supplier_ddl = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='suppliers' AND COLUMN_NAME='joined_date')=0,
 'ALTER TABLE suppliers ADD COLUMN joined_date DATE NULL', 'SELECT 1');
PREPARE supplier_stmt FROM @supplier_ddl; EXECUTE supplier_stmt; DEALLOCATE PREPARE supplier_stmt;
-- Do not infer historical joining dates or products from company names.
