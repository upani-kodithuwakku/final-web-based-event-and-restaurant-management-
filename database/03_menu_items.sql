-- The six dishes already defined in frontend/src/data.ts.
-- Safe to rerun: add missing dishes without changing existing prices or availability.
USE restaurant_event_db;
START TRANSACTION;
INSERT INTO menu_categories (name, description, display_order, is_active, created_at, updated_at)
SELECT seed.name, seed.description, seed.display_order, 1, NOW(), NOW()
FROM (
  SELECT 'Starters' name, 'Appetizers and soups' description, 1 display_order
  UNION ALL SELECT 'Mains', 'Main dishes', 2
  UNION ALL SELECT 'Desserts', 'Sweet endings', 3
  UNION ALL SELECT 'Drinks', 'Hot and cold drinks', 4
) seed WHERE NOT EXISTS (SELECT 1 FROM menu_categories c WHERE c.name = seed.name);
INSERT INTO menu_items
  (category_id, name, description, price, image_url, preparation_minutes, is_available, is_active, created_at, updated_at)
SELECT c.id, seed.name, seed.description, seed.price,
  CONCAT('https://images.unsplash.com/', seed.photo, '?w=800&fit=crop'), 15, 1, 1, NOW(), NOW()
FROM (
  SELECT 'Starters' category, 'Burrata & heirloom tomatoes' name,
    'Creamy burrata, garden basil, aged balsamic' description, 2400.00 price, 'photo-1608897013039-887f21d8c804' photo
  UNION ALL SELECT 'Mains', 'Wood-fired margherita', 'San Marzano tomatoes, mozzarella, fresh basil', 3200.00, 'photo-1579751626657-72bc17010498'
  UNION ALL SELECT 'Mains', 'Grilled salmon bowl', 'Atlantic salmon, seasonal greens, lemon dressing', 4800.00, 'photo-1467003909585-2f8a72700288'
  UNION ALL SELECT 'Mains', 'The Gather burger', 'Grilled beef, aged cheddar, house sauce, fries', 3600.00, 'photo-1568901346375-23c9450c58cd'
  UNION ALL SELECT 'Desserts', 'Chocolate indulgence', 'Warm chocolate cake with vanilla ice cream', 1800.00, 'photo-1578985545062-69928b1d9587'
  UNION ALL SELECT 'Drinks', 'Garden citrus cooler', 'Fresh lime, mint, sparkling water', 950.00, 'photo-1513558161293-cdaf765edfd7'
) seed JOIN menu_categories c ON c.name = seed.category
WHERE NOT EXISTS (SELECT 1 FROM menu_items i WHERE i.name = seed.name AND i.category_id = c.id);
COMMIT;
