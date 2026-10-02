# Relationships — Menu and Orders

- menu_items many-to-one menu_categories
- food_orders many-to-one users (customer)
- food_orders many-to-one restaurant_tables (nullable)
- food_orders many-to-one table_reservations (nullable)
- food_order_items many-to-one food_orders (cascade delete)
- food_order_items many-to-one menu_items
- menu_item_ingredients many-to-one menu_items
- menu_item_ingredients many-to-one inventory_items (cross-module read)

## Key Design Note

food_order_items stores snapshot values (itemNameSnapshot, unitPriceSnapshot) so historical orders are accurate even if the menu item price changes later. The menuItem FK is kept for reference but the financial record uses snapshots.
