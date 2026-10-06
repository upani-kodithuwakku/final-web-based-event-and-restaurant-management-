-- Give private group spaces distinct dining-room photographs.
UPDATE restaurant_tables
SET image_url = 'https://images.unsplash.com/photo-1552566626-52f8b828add9?auto=format&fit=crop&w=1000&q=85',
    updated_at = CURRENT_TIMESTAMP
WHERE table_number = 'GROUP01';

UPDATE restaurant_tables
SET image_url = 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=1000&q=85',
    updated_at = CURRENT_TIMESTAMP
WHERE table_number = 'P02';
