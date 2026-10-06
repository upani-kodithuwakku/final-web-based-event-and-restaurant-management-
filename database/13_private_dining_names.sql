-- Distinguish the small private table from the group dining space.
UPDATE restaurant_tables
SET display_name = 'The Private Nook',
    description = 'A cosy private table for up to four guests.',
    updated_at = CURRENT_TIMESTAMP
WHERE table_number = 'T01';

UPDATE restaurant_tables
SET display_name = 'The Group Dining Lounge',
    description = 'A spacious private table for group dinners of up to eight guests.',
    updated_at = CURRENT_TIMESTAMP
WHERE table_number = 'GROUP01';
