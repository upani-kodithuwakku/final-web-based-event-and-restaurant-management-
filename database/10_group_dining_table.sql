-- Add a group dining option without changing existing table capacities or status.
INSERT INTO restaurant_tables (table_number, capacity, location, current_status, is_active, created_at, updated_at)
SELECT 'GROUP01', 8, 'PRIVATE', 'AVAILABLE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM restaurant_tables WHERE table_number = 'GROUP01');
