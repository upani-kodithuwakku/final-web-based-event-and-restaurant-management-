# Database Schema — Menu and Orders

## menu_categories
```sql
CREATE TABLE menu_categories (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(255) NOT NULL,
  description   TEXT,
  display_order INT DEFAULT 0,
  is_active     BOOLEAN DEFAULT TRUE
);
```

## menu_items
```sql
CREATE TABLE menu_items (
  id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
  category_id          BIGINT NOT NULL,
  name                 VARCHAR(255) NOT NULL,
  description          TEXT,
  price                DECIMAL(12,2) NOT NULL,
  image_url            VARCHAR(500),
  preparation_minutes  INT DEFAULT 15,
  is_available         BOOLEAN DEFAULT TRUE,
  is_active            BOOLEAN DEFAULT TRUE,
  FOREIGN KEY (category_id) REFERENCES menu_categories(id)
);
```

## food_orders
```sql
CREATE TABLE food_orders (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_reference VARCHAR(50) UNIQUE NOT NULL,
  customer_id     BIGINT NOT NULL,
  table_id        BIGINT,
  reservation_id  BIGINT,
  order_type      VARCHAR(50),
  status          VARCHAR(50) DEFAULT 'PENDING',
  special_note    TEXT,
  subtotal        DECIMAL(12,2),
  created_at      DATETIME,
  updated_at      DATETIME,
  FOREIGN KEY (customer_id) REFERENCES users(id),
  FOREIGN KEY (table_id) REFERENCES restaurant_tables(id),
  FOREIGN KEY (reservation_id) REFERENCES table_reservations(id)
);
```

## food_order_items
```sql
CREATE TABLE food_order_items (
  id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id            BIGINT NOT NULL,
  menu_item_id        BIGINT NOT NULL,
  item_name_snapshot  VARCHAR(255) NOT NULL,
  unit_price_snapshot DECIMAL(12,2) NOT NULL,
  quantity            INT NOT NULL,
  special_note        TEXT,
  line_total          DECIMAL(12,2) NOT NULL,
  FOREIGN KEY (order_id) REFERENCES food_orders(id),
  FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
);
```

## menu_item_ingredients
```sql
CREATE TABLE menu_item_ingredients (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY,
  menu_item_id      BIGINT NOT NULL,
  inventory_item_id BIGINT NOT NULL,
  quantity_required DECIMAL(12,3) NOT NULL,
  FOREIGN KEY (menu_item_id) REFERENCES menu_items(id),
  FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id)
);
```
