-- Example SQL queries for Menu and Orders module

-- 1. List available menu items by category
SELECT mi.id, mc.name AS category, mi.name, mi.price, mi.preparation_minutes
FROM menu_items mi
JOIN menu_categories mc ON mc.id = mi.category_id
WHERE mi.is_available = TRUE AND mi.is_active = TRUE
ORDER BY mc.display_order, mi.name;

-- 2. Toggle menu item availability
UPDATE menu_items SET is_available = NOT is_available WHERE id = 5;

-- 3. Kitchen queue (active orders)
SELECT fo.id, fo.order_reference, fo.status, fo.created_at,
       GROUP_CONCAT(CONCAT(foi.quantity, 'x ', foi.item_name_snapshot) SEPARATOR ', ') AS items
FROM food_orders fo
JOIN food_order_items foi ON foi.order_id = fo.id
WHERE fo.status IN ('PENDING', 'PREPARING', 'READY')
GROUP BY fo.id
ORDER BY fo.created_at ASC;

-- 4. Check if all ingredients are available for an order
SELECT mi.name, ii.name AS ingredient, mii.quantity_required, ii.current_quantity,
       CASE WHEN ii.current_quantity >= mii.quantity_required THEN 'OK' ELSE 'INSUFFICIENT' END AS stock_check
FROM menu_item_ingredients mii
JOIN menu_items mi ON mi.id = mii.menu_item_id
JOIN inventory_items ii ON ii.id = mii.inventory_item_id
WHERE mii.menu_item_id = 3;

-- 5. Update order status
UPDATE food_orders SET status = 'PREPARING', updated_at = NOW() WHERE id = 7;

-- 6. Customer order history
SELECT fo.id, fo.order_reference, fo.status, fo.subtotal, fo.created_at
FROM food_orders fo
WHERE fo.customer_id = 4
ORDER BY fo.created_at DESC;

-- 7. Order detail with items
SELECT foi.item_name_snapshot, foi.quantity, foi.unit_price_snapshot, foi.line_total
FROM food_order_items foi
WHERE foi.order_id = 7;
