-- ============================================================
-- Restaurant & Event Management System
-- Sample Data Script
-- Run this after database/01_create_database.sql
-- ============================================================

USE restaurant_event_db;

START TRANSACTION;

-- Remove this script's previous sample rows so it can be rerun safely.
DELETE FROM attendance_records
WHERE shift_assignment_id IN (
  SELECT sa.id
  FROM shift_assignments sa
  JOIN shifts s ON s.id = sa.shift_id
  WHERE s.shift_date IN ('2026-09-20', '2026-09-21')
);

DELETE sa FROM shift_assignments sa
JOIN shifts s ON s.id = sa.shift_id
WHERE s.shift_date IN ('2026-09-20', '2026-09-21');

DELETE FROM shifts
WHERE shift_date IN ('2026-09-20', '2026-09-21');

DELETE FROM notifications
WHERE title IN ('Reservation Confirmed', 'Event Deposit Received', 'Low Stock Alert');

DELETE FROM feedback
WHERE comment IN (
  'Excellent service and the dessert was perfect.',
  'Booking process was smooth and staff were helpful.'
);

DELETE FROM audit_logs
WHERE action IN ('CREATE_SAMPLE_DATA', 'CONFIRM_RESERVATION');

DELETE FROM payments
WHERE payment_reference IN ('PAY-2026-0001', 'PAY-2026-0002');

DELETE FROM invoice_items
WHERE invoice_id IN (
  SELECT id FROM invoices WHERE invoice_number IN ('INV-2026-0001', 'INV-2026-0002')
);

DELETE FROM invoices
WHERE invoice_number IN ('INV-2026-0001', 'INV-2026-0002');

DELETE FROM stock_movements
WHERE reference_type IN ('PURCHASE_ORDER', 'FOOD_ORDER')
  AND (
    reference_id IN (SELECT id FROM purchase_orders WHERE po_number IN ('PO-2026-0001', 'PO-2026-0002'))
    OR reference_id IN (SELECT id FROM food_orders WHERE order_reference IN ('FO-2026-0001', 'FO-2026-0002'))
  );

DELETE FROM purchase_order_items
WHERE purchase_order_id IN (
  SELECT id FROM purchase_orders WHERE po_number IN ('PO-2026-0001', 'PO-2026-0002')
);

DELETE FROM purchase_orders
WHERE po_number IN ('PO-2026-0001', 'PO-2026-0002');

DELETE FROM food_order_items
WHERE order_id IN (
  SELECT id FROM food_orders WHERE order_reference IN ('FO-2026-0001', 'FO-2026-0002')
);

DELETE FROM food_orders
WHERE order_reference IN ('FO-2026-0001', 'FO-2026-0002');

DELETE FROM event_bookings
WHERE booking_reference IN ('EV-2026-0001', 'EV-2026-0002');

DELETE FROM table_reservations
WHERE booking_reference IN ('TR-2026-0001', 'TR-2026-0002');

DELETE FROM staff_profiles
WHERE employee_code IN ('EMP-001', 'EMP-002');

DELETE FROM menu_item_ingredients
WHERE menu_item_id IN (
  SELECT id FROM menu_items
  WHERE name IN ('Crispy Calamari', 'Herb Grilled Chicken', 'Seafood Fried Rice', 'Chocolate Lava Cake', 'Fresh Lime Soda')
);

DELETE FROM menu_items
WHERE name IN ('Crispy Calamari', 'Herb Grilled Chicken', 'Seafood Fried Rice', 'Chocolate Lava Cake', 'Fresh Lime Soda');

DELETE FROM event_packages
WHERE name IN ('Silver Wedding Package', 'Corporate Meeting Package', 'Birthday Celebration Package');

DELETE FROM suppliers
WHERE name IN ('Fresh Farm Supplies', 'Ocean Catch Seafood', 'Ceylon Pantry Wholesale');

-- SECURITY & USERS
INSERT INTO roles (name, description) VALUES
  ('ADMIN', 'System administrator'),
  ('MANAGER', 'Restaurant and event manager'),
  ('STAFF', 'Operational staff member'),
  ('CUSTOMER', 'Restaurant customer')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- Password for all sample users: password123
INSERT INTO users (full_name, email, phone, password_hash, is_active) VALUES
  ('Admin User', 'admin@gather.test', '+94770000001', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1),
  ('Maya Perera', 'maya.perera@example.com', '+94771234567', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1),
  ('Nimal Fernando', 'nimal.fernando@example.com', '+94772345678', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1),
  ('Sarah Silva', 'sarah.silva@example.com', '+94773456789', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1),
  ('Kasun Jayawardena', 'kasun.staff@gather.test', '+94774567890', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1),
  ('Anjali Wijesinghe', 'anjali.staff@gather.test', '+94775678901', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1)
ON DUPLICATE KEY UPDATE
  full_name = VALUES(full_name),
  phone = VALUES(phone),
  is_active = VALUES(is_active);

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.name = 'ADMIN' WHERE u.email = 'admin@gather.test';

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.name = 'CUSTOMER'
WHERE u.email IN ('maya.perera@example.com', 'nimal.fernando@example.com', 'sarah.silva@example.com');

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.name = 'STAFF'
WHERE u.email IN ('kasun.staff@gather.test', 'anjali.staff@gather.test');

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.name = 'MANAGER'
WHERE u.email = 'kasun.staff@gather.test';

INSERT INTO password_reset_tokens (user_id, token, expires_at, used)
SELECT id, 'sample-reset-token-maya', DATE_ADD(NOW(), INTERVAL 1 DAY), 0
FROM users
WHERE email = 'maya.perera@example.com'
ON DUPLICATE KEY UPDATE expires_at = VALUES(expires_at), used = VALUES(used);

-- TABLE RESERVATIONS
INSERT INTO restaurant_tables (table_number, capacity, location, current_status, is_active) VALUES
  ('T01', 2, 'Window', 'AVAILABLE', 1),
  ('T02', 4, 'Main Dining', 'RESERVED', 1),
  ('T03', 6, 'Main Dining', 'AVAILABLE', 1),
  ('T04', 8, 'Garden', 'AVAILABLE', 1),
  ('VIP1', 10, 'Private Room', 'RESERVED', 1)
ON DUPLICATE KEY UPDATE
  capacity = VALUES(capacity),
  location = VALUES(location),
  current_status = VALUES(current_status),
  is_active = VALUES(is_active);

INSERT INTO table_reservations (
  booking_reference, customer_id, table_id, reservation_date, start_time, end_time,
  guest_count, seating_preference, special_request, status, contact_name, contact_phone
) VALUES
  (
    'TR-2026-0001',
    (SELECT id FROM users WHERE email = 'maya.perera@example.com'),
    (SELECT id FROM restaurant_tables WHERE table_number = 'T02'),
    '2026-09-20', '19:00:00', '21:00:00', 4, 'Window',
    'Birthday dessert plate', 'CONFIRMED', 'Maya Perera', '+94771234567'
  ),
  (
    'TR-2026-0002',
    (SELECT id FROM users WHERE email = 'nimal.fernando@example.com'),
    (SELECT id FROM restaurant_tables WHERE table_number = 'VIP1'),
    '2026-09-21', '18:30:00', '21:30:00', 8, 'Private',
    'Vegetarian options required', 'PENDING', 'Nimal Fernando', '+94772345678'
  )
ON DUPLICATE KEY UPDATE
  status = VALUES(status),
  guest_count = VALUES(guest_count),
  special_request = VALUES(special_request);

-- MENU & ORDERS
INSERT INTO menu_categories (name, description, display_order, is_active) VALUES
  ('Starters', 'Small plates and appetizers', 1, 1),
  ('Mains', 'Main courses', 2, 1),
  ('Desserts', 'Sweet dishes', 3, 1),
  ('Beverages', 'Hot and cold drinks', 4, 1)
ON DUPLICATE KEY UPDATE
  description = VALUES(description),
  display_order = VALUES(display_order),
  is_active = VALUES(is_active);

INSERT INTO menu_items (
  category_id, name, description, price, image_url, preparation_minutes, is_available, is_active
) VALUES
  ((SELECT id FROM menu_categories WHERE name = 'Starters'), 'Crispy Calamari', 'Fried calamari with lime aioli', 1850.00, NULL, 15, 1, 1),
  ((SELECT id FROM menu_categories WHERE name = 'Mains'), 'Herb Grilled Chicken', 'Grilled chicken with seasonal vegetables', 3200.00, NULL, 25, 1, 1),
  ((SELECT id FROM menu_categories WHERE name = 'Mains'), 'Seafood Fried Rice', 'Rice with prawns, cuttlefish, and vegetables', 2400.00, NULL, 20, 1, 1),
  ((SELECT id FROM menu_categories WHERE name = 'Desserts'), 'Chocolate Lava Cake', 'Warm chocolate cake with vanilla ice cream', 1450.00, NULL, 12, 1, 1),
  ((SELECT id FROM menu_categories WHERE name = 'Beverages'), 'Fresh Lime Soda', 'Chilled lime soda', 650.00, NULL, 5, 1, 1)
ON DUPLICATE KEY UPDATE
  description = VALUES(description),
  price = VALUES(price),
  preparation_minutes = VALUES(preparation_minutes),
  is_available = VALUES(is_available),
  is_active = VALUES(is_active);

INSERT INTO food_orders (
  order_reference, customer_id, table_id, reservation_id, order_type, status, special_note, subtotal
) VALUES
  (
    'FO-2026-0001',
    (SELECT id FROM users WHERE email = 'maya.perera@example.com'),
    (SELECT id FROM restaurant_tables WHERE table_number = 'T02'),
    (SELECT id FROM table_reservations WHERE booking_reference = 'TR-2026-0001'),
    'DINE_IN', 'SERVED', 'Serve dessert after cake cutting', 7950.00
  ),
  (
    'FO-2026-0002',
    (SELECT id FROM users WHERE email = 'sarah.silva@example.com'),
    NULL, NULL, 'TAKEAWAY', 'PREPARING', 'Extra lime soda ice', 3050.00
  )
ON DUPLICATE KEY UPDATE
  status = VALUES(status),
  special_note = VALUES(special_note),
  subtotal = VALUES(subtotal);

INSERT INTO food_order_items (
  order_id, menu_item_id, item_name_snapshot, unit_price_snapshot, quantity, special_note, line_total
) VALUES
  ((SELECT id FROM food_orders WHERE order_reference = 'FO-2026-0001'), (SELECT id FROM menu_items WHERE name = 'Crispy Calamari'), 'Crispy Calamari', 1850.00, 1, NULL, 1850.00),
  ((SELECT id FROM food_orders WHERE order_reference = 'FO-2026-0001'), (SELECT id FROM menu_items WHERE name = 'Herb Grilled Chicken'), 'Herb Grilled Chicken', 3200.00, 1, 'No chili', 3200.00),
  ((SELECT id FROM food_orders WHERE order_reference = 'FO-2026-0001'), (SELECT id FROM menu_items WHERE name = 'Chocolate Lava Cake'), 'Chocolate Lava Cake', 1450.00, 2, NULL, 2900.00),
  ((SELECT id FROM food_orders WHERE order_reference = 'FO-2026-0002'), (SELECT id FROM menu_items WHERE name = 'Seafood Fried Rice'), 'Seafood Fried Rice', 2400.00, 1, NULL, 2400.00),
  ((SELECT id FROM food_orders WHERE order_reference = 'FO-2026-0002'), (SELECT id FROM menu_items WHERE name = 'Fresh Lime Soda'), 'Fresh Lime Soda', 650.00, 1, 'Less sugar', 650.00);

-- EVENTS
INSERT INTO event_halls (name, capacity, location, description, is_active) VALUES
  ('Ocean Hall', 250, 'Level 2', 'Large banquet hall with stage and AV setup', 1),
  ('Garden Pavilion', 120, 'Garden Wing', 'Open-air covered pavilion', 1),
  ('Boardroom A', 30, 'Level 1', 'Private room for meetings and workshops', 1)
ON DUPLICATE KEY UPDATE
  capacity = VALUES(capacity),
  location = VALUES(location),
  description = VALUES(description),
  is_active = VALUES(is_active);

INSERT INTO event_packages (
  name, event_type, description, base_price, minimum_guests, maximum_guests, is_active
) VALUES
  ('Silver Wedding Package', 'WEDDING', 'Buffet, basic decor, and hall setup', 450000.00, 50, 200, 1),
  ('Corporate Meeting Package', 'CORPORATE', 'Tea breaks, lunch, projector, and sound system', 180000.00, 20, 80, 1),
  ('Birthday Celebration Package', 'BIRTHDAY', 'Dinner buffet, cake table, and simple decor', 95000.00, 20, 100, 1);

INSERT INTO event_bookings (
  booking_reference, customer_id, hall_id, package_id, event_date, start_time, end_time,
  guest_count, special_requirements, status, deposit_amount
) VALUES
  (
    'EV-2026-0001',
    (SELECT id FROM users WHERE email = 'nimal.fernando@example.com'),
    (SELECT id FROM event_halls WHERE name = 'Ocean Hall'),
    (SELECT id FROM event_packages WHERE name = 'Silver Wedding Package' LIMIT 1),
    '2026-10-05', '17:00:00', '23:00:00', 180,
    'Add vegetarian buffet counter and welcome drink station', 'CONFIRMED', 100000.00
  ),
  (
    'EV-2026-0002',
    (SELECT id FROM users WHERE email = 'sarah.silva@example.com'),
    (SELECT id FROM event_halls WHERE name = 'Boardroom A'),
    (SELECT id FROM event_packages WHERE name = 'Corporate Meeting Package' LIMIT 1),
    '2026-09-28', '09:00:00', '16:00:00', 25,
    'Projector and two microphones required', 'PENDING', 25000.00
  )
ON DUPLICATE KEY UPDATE
  status = VALUES(status),
  guest_count = VALUES(guest_count),
  deposit_amount = VALUES(deposit_amount);

-- BILLING & PAYMENTS
INSERT INTO invoices (
  invoice_number, customer_id, food_order_id, event_booking_id, invoice_type,
  subtotal, service_charge, tax_amount, discount_amount, total_amount, status
) VALUES
  (
    'INV-2026-0001',
    (SELECT id FROM users WHERE email = 'maya.perera@example.com'),
    (SELECT id FROM food_orders WHERE order_reference = 'FO-2026-0001'),
    NULL, 'FOOD', 7950.00, 795.00, 700.00, 0.00, 9445.00, 'PAID'
  ),
  (
    'INV-2026-0002',
    (SELECT id FROM users WHERE email = 'nimal.fernando@example.com'),
    NULL,
    (SELECT id FROM event_bookings WHERE booking_reference = 'EV-2026-0001'),
    'EVENT', 450000.00, 45000.00, 39600.00, 15000.00, 519600.00, 'PARTIALLY_PAID'
  )
ON DUPLICATE KEY UPDATE
  subtotal = VALUES(subtotal),
  service_charge = VALUES(service_charge),
  tax_amount = VALUES(tax_amount),
  discount_amount = VALUES(discount_amount),
  total_amount = VALUES(total_amount),
  status = VALUES(status);

INSERT INTO invoice_items (invoice_id, description, quantity, unit_price, line_total) VALUES
  ((SELECT id FROM invoices WHERE invoice_number = 'INV-2026-0001'), 'Crispy Calamari', 1, 1850.00, 1850.00),
  ((SELECT id FROM invoices WHERE invoice_number = 'INV-2026-0001'), 'Herb Grilled Chicken', 1, 3200.00, 3200.00),
  ((SELECT id FROM invoices WHERE invoice_number = 'INV-2026-0001'), 'Chocolate Lava Cake', 2, 1450.00, 2900.00),
  ((SELECT id FROM invoices WHERE invoice_number = 'INV-2026-0002'), 'Silver Wedding Package', 1, 450000.00, 450000.00);

INSERT INTO payments (payment_reference, invoice_id, amount, method, status, paid_at, gateway_reference) VALUES
  ('PAY-2026-0001', (SELECT id FROM invoices WHERE invoice_number = 'INV-2026-0001'), 9445.00, 'CARD', 'COMPLETED', '2026-09-20 21:15:00', 'GW-FO-0001'),
  ('PAY-2026-0002', (SELECT id FROM invoices WHERE invoice_number = 'INV-2026-0002'), 100000.00, 'BANK_TRANSFER', 'COMPLETED', '2026-09-15 10:30:00', 'GW-EV-0001')
ON DUPLICATE KEY UPDATE
  amount = VALUES(amount),
  method = VALUES(method),
  status = VALUES(status),
  paid_at = VALUES(paid_at),
  gateway_reference = VALUES(gateway_reference);

-- INVENTORY
INSERT INTO inventory_items (name, unit, current_quantity, reorder_level, is_active) VALUES
  ('Chicken Breast', 'kg', 35.000, 10.000, 1),
  ('Calamari', 'kg', 18.500, 5.000, 1),
  ('Rice', 'kg', 80.000, 20.000, 1),
  ('Lime', 'pcs', 120.000, 30.000, 1),
  ('Chocolate', 'kg', 12.000, 4.000, 1)
ON DUPLICATE KEY UPDATE
  unit = VALUES(unit),
  current_quantity = VALUES(current_quantity),
  reorder_level = VALUES(reorder_level),
  is_active = VALUES(is_active);

INSERT INTO menu_item_ingredients (menu_item_id, inventory_item_id, quantity_required) VALUES
  ((SELECT id FROM menu_items WHERE name = 'Herb Grilled Chicken'), (SELECT id FROM inventory_items WHERE name = 'Chicken Breast'), 0.250),
  ((SELECT id FROM menu_items WHERE name = 'Crispy Calamari'), (SELECT id FROM inventory_items WHERE name = 'Calamari'), 0.180),
  ((SELECT id FROM menu_items WHERE name = 'Seafood Fried Rice'), (SELECT id FROM inventory_items WHERE name = 'Rice'), 0.200),
  ((SELECT id FROM menu_items WHERE name = 'Fresh Lime Soda'), (SELECT id FROM inventory_items WHERE name = 'Lime'), 2.000),
  ((SELECT id FROM menu_items WHERE name = 'Chocolate Lava Cake'), (SELECT id FROM inventory_items WHERE name = 'Chocolate'), 0.080)
ON DUPLICATE KEY UPDATE quantity_required = VALUES(quantity_required);

INSERT INTO suppliers (name, contact_person, phone, email, address, is_active) VALUES
  ('Fresh Farm Supplies', 'Ruwan Dias', '+94112345001', 'orders@freshfarm.test', 'No 12 Market Road, Colombo', 1),
  ('Ocean Catch Seafood', 'Ishara Mendis', '+94112345002', 'sales@oceancatch.test', 'Harbour Street, Negombo', 1),
  ('Ceylon Pantry Wholesale', 'Devika Ramanayake', '+94112345003', 'hello@ceylonpantry.test', 'Industrial Zone, Kelaniya', 1);

INSERT INTO purchase_orders (po_number, supplier_id, status, ordered_at, received_at, notes) VALUES
  ('PO-2026-0001', (SELECT id FROM suppliers WHERE name = 'Fresh Farm Supplies' LIMIT 1), 'RECEIVED', '2026-09-10 09:00:00', '2026-09-11 14:30:00', 'Weekly produce and poultry restock'),
  ('PO-2026-0002', (SELECT id FROM suppliers WHERE name = 'Ocean Catch Seafood' LIMIT 1), 'PENDING', '2026-09-16 11:00:00', NULL, 'Seafood order for weekend service')
ON DUPLICATE KEY UPDATE
  status = VALUES(status),
  received_at = VALUES(received_at),
  notes = VALUES(notes);

INSERT INTO purchase_order_items (
  purchase_order_id, inventory_item_id, quantity_ordered, quantity_received, unit_cost
) VALUES
  ((SELECT id FROM purchase_orders WHERE po_number = 'PO-2026-0001'), (SELECT id FROM inventory_items WHERE name = 'Chicken Breast'), 20.000, 20.000, 1650.00),
  ((SELECT id FROM purchase_orders WHERE po_number = 'PO-2026-0001'), (SELECT id FROM inventory_items WHERE name = 'Lime'), 100.000, 100.000, 35.00),
  ((SELECT id FROM purchase_orders WHERE po_number = 'PO-2026-0002'), (SELECT id FROM inventory_items WHERE name = 'Calamari'), 15.000, 0.000, 2200.00);

INSERT INTO stock_movements (
  inventory_item_id, movement_type, quantity_change, reference_type, reference_id, note
) VALUES
  ((SELECT id FROM inventory_items WHERE name = 'Chicken Breast'), 'IN', 20.000, 'PURCHASE_ORDER', (SELECT id FROM purchase_orders WHERE po_number = 'PO-2026-0001'), 'Received chicken stock'),
  ((SELECT id FROM inventory_items WHERE name = 'Lime'), 'IN', 100.000, 'PURCHASE_ORDER', (SELECT id FROM purchase_orders WHERE po_number = 'PO-2026-0001'), 'Received lime stock'),
  ((SELECT id FROM inventory_items WHERE name = 'Chicken Breast'), 'OUT', -0.250, 'FOOD_ORDER', (SELECT id FROM food_orders WHERE order_reference = 'FO-2026-0001'), 'Used for Herb Grilled Chicken');

-- STAFF
INSERT INTO staff_profiles (user_id, employee_code, job_title, employment_status, joined_date) VALUES
  ((SELECT id FROM users WHERE email = 'kasun.staff@gather.test'), 'EMP-001', 'Restaurant Manager', 'ACTIVE', '2025-04-01'),
  ((SELECT id FROM users WHERE email = 'anjali.staff@gather.test'), 'EMP-002', 'Service Associate', 'ACTIVE', '2025-07-15')
ON DUPLICATE KEY UPDATE
  job_title = VALUES(job_title),
  employment_status = VALUES(employment_status),
  joined_date = VALUES(joined_date);

INSERT INTO shifts (
  shift_date, start_time, end_time, role_required, required_staff_count, status
) VALUES
  ('2026-09-20', '16:00:00', '23:00:00', 'Restaurant Service', 3, 'OPEN'),
  ('2026-09-21', '09:00:00', '17:00:00', 'Event Setup', 4, 'OPEN');

INSERT INTO shift_assignments (shift_id, staff_id, assigned_role, status) VALUES
  (
    (SELECT id FROM shifts WHERE shift_date = '2026-09-20' AND role_required = 'Restaurant Service' LIMIT 1),
    (SELECT id FROM staff_profiles WHERE employee_code = 'EMP-001'),
    'Shift Lead', 'ASSIGNED'
  ),
  (
    (SELECT id FROM shifts WHERE shift_date = '2026-09-20' AND role_required = 'Restaurant Service' LIMIT 1),
    (SELECT id FROM staff_profiles WHERE employee_code = 'EMP-002'),
    'Server', 'ASSIGNED'
  )
ON DUPLICATE KEY UPDATE
  assigned_role = VALUES(assigned_role),
  status = VALUES(status);

INSERT INTO attendance_records (
  shift_assignment_id, check_in_at, check_out_at, attendance_status
) VALUES
  (
    (
      SELECT sa.id
      FROM shift_assignments sa
      JOIN staff_profiles sp ON sp.id = sa.staff_id
      JOIN shifts s ON s.id = sa.shift_id
      WHERE sp.employee_code = 'EMP-001'
        AND s.shift_date = '2026-09-20'
        AND s.role_required = 'Restaurant Service'
      LIMIT 1
    ),
    '2026-09-20 15:55:00', '2026-09-20 23:05:00', 'PRESENT'
  )
ON DUPLICATE KEY UPDATE
  check_in_at = VALUES(check_in_at),
  check_out_at = VALUES(check_out_at),
  attendance_status = VALUES(attendance_status);

-- SHARED / CROSS-MODULE
INSERT INTO notifications (user_id, title, message, type, is_read) VALUES
  ((SELECT id FROM users WHERE email = 'maya.perera@example.com'), 'Reservation Confirmed', 'Your table reservation TR-2026-0001 has been confirmed.', 'RESERVATION', 0),
  ((SELECT id FROM users WHERE email = 'nimal.fernando@example.com'), 'Event Deposit Received', 'We received your deposit for event booking EV-2026-0001.', 'EVENT', 1),
  ((SELECT id FROM users WHERE email = 'admin@gather.test'), 'Low Stock Alert', 'Chocolate is close to its reorder level.', 'INVENTORY', 0);

INSERT INTO feedback (
  customer_id, food_order_id, event_booking_id, rating, comment
) VALUES
  (
    (SELECT id FROM users WHERE email = 'maya.perera@example.com'),
    (SELECT id FROM food_orders WHERE order_reference = 'FO-2026-0001'),
    NULL, 5, 'Excellent service and the dessert was perfect.'
  ),
  (
    (SELECT id FROM users WHERE email = 'nimal.fernando@example.com'),
    NULL,
    (SELECT id FROM event_bookings WHERE booking_reference = 'EV-2026-0001'),
    4, 'Booking process was smooth and staff were helpful.'
  );

INSERT INTO audit_logs (
  user_id, action, entity_name, entity_id, old_value, new_value
) VALUES
  (
    (SELECT id FROM users WHERE email = 'admin@gather.test'),
    'CREATE_SAMPLE_DATA', 'database_seed', NULL, NULL, 'Inserted sample data for development'
  ),
  (
    (SELECT id FROM users WHERE email = 'kasun.staff@gather.test'),
    'CONFIRM_RESERVATION', 'table_reservations',
    (SELECT id FROM table_reservations WHERE booking_reference = 'TR-2026-0001'),
    'PENDING', 'CONFIRMED'
  );

COMMIT;
