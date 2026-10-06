-- Adds eight customer accounts and approximately 25 bookings around the current date.
-- New-account password: Customer@1234 (BCrypt). Existing accounts are never overwritten.
-- Run the whole file in MySQL Workbench, or from the repository root:
-- docker compose --env-file restaurant-event-backend/.env exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -u root "$MYSQL_DATABASE"' < database/07_reservations_data.sql
-- Run one copy at a time; stop on errors and ROLLBACK before retrying.
-- Uses the restaurant's local time. Skips occupied slots rather than moving existing bookings.
SET NAMES utf8mb4;
USE restaurant_event_db;
SET @previous_time_zone = @@session.time_zone;
SET time_zone = '+05:30';
START TRANSACTION;
SET @run_now = NOW();
SET @users_before = (SELECT COUNT(*) FROM users);
SET @reservations_before = (SELECT COUNT(*) FROM table_reservations);
SET @notifications_before = (SELECT COUNT(*) FROM notifications);

INSERT INTO users (full_name,email,phone,password_hash,is_active,created_at,updated_at)
SELECT 'Nimal Dissanayake','nimal.dissanayake@example.com','0772345670','$2a$10$mymrjXGO.gefx.Uljx/t/uPgvvzxCRJr2xy4ABEjeSl9suEgEEpRq',1,DATE_SUB(@run_now,INTERVAL 28 DAY),DATE_SUB(@run_now,INTERVAL 28 DAY)
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='nimal.dissanayake@example.com');
INSERT INTO user_roles (user_id,role_id)
SELECT u.id,r.id FROM users u JOIN roles r ON r.name='CUSTOMER'
WHERE u.email='nimal.dissanayake@example.com' AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id=u.id AND ur.role_id=r.id);
INSERT INTO users (full_name,email,phone,password_hash,is_active,created_at,updated_at)
SELECT 'Kavindi Jayasinghe','kavindi.jayasinghe@example.com','0712345671','$2a$10$mymrjXGO.gefx.Uljx/t/uPgvvzxCRJr2xy4ABEjeSl9suEgEEpRq',1,DATE_SUB(@run_now,INTERVAL 28 DAY),DATE_SUB(@run_now,INTERVAL 28 DAY)
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='kavindi.jayasinghe@example.com');
INSERT INTO user_roles (user_id,role_id)
SELECT u.id,r.id FROM users u JOIN roles r ON r.name='CUSTOMER'
WHERE u.email='kavindi.jayasinghe@example.com' AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id=u.id AND ur.role_id=r.id);
INSERT INTO users (full_name,email,phone,password_hash,is_active,created_at,updated_at)
SELECT 'Amal Wijesinghe','amal.wijesinghe@example.com','0762345672','$2a$10$mymrjXGO.gefx.Uljx/t/uPgvvzxCRJr2xy4ABEjeSl9suEgEEpRq',1,DATE_SUB(@run_now,INTERVAL 28 DAY),DATE_SUB(@run_now,INTERVAL 28 DAY)
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='amal.wijesinghe@example.com');
INSERT INTO user_roles (user_id,role_id)
SELECT u.id,r.id FROM users u JOIN roles r ON r.name='CUSTOMER'
WHERE u.email='amal.wijesinghe@example.com' AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id=u.id AND ur.role_id=r.id);
INSERT INTO users (full_name,email,phone,password_hash,is_active,created_at,updated_at)
SELECT 'Dilini Jayawardena','dilini.jayawardena@example.com','0772345673','$2a$10$mymrjXGO.gefx.Uljx/t/uPgvvzxCRJr2xy4ABEjeSl9suEgEEpRq',1,DATE_SUB(@run_now,INTERVAL 28 DAY),DATE_SUB(@run_now,INTERVAL 28 DAY)
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='dilini.jayawardena@example.com');
INSERT INTO user_roles (user_id,role_id)
SELECT u.id,r.id FROM users u JOIN roles r ON r.name='CUSTOMER'
WHERE u.email='dilini.jayawardena@example.com' AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id=u.id AND ur.role_id=r.id);
INSERT INTO users (full_name,email,phone,password_hash,is_active,created_at,updated_at)
SELECT 'Ruwan Bandara','ruwan.bandara@example.com','0712345674','$2a$10$mymrjXGO.gefx.Uljx/t/uPgvvzxCRJr2xy4ABEjeSl9suEgEEpRq',1,DATE_SUB(@run_now,INTERVAL 28 DAY),DATE_SUB(@run_now,INTERVAL 28 DAY)
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='ruwan.bandara@example.com');
INSERT INTO user_roles (user_id,role_id)
SELECT u.id,r.id FROM users u JOIN roles r ON r.name='CUSTOMER'
WHERE u.email='ruwan.bandara@example.com' AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id=u.id AND ur.role_id=r.id);
INSERT INTO users (full_name,email,phone,password_hash,is_active,created_at,updated_at)
SELECT 'Sachini Wickramasinghe','sachini.wickramasinghe@example.com','0762345675','$2a$10$mymrjXGO.gefx.Uljx/t/uPgvvzxCRJr2xy4ABEjeSl9suEgEEpRq',1,DATE_SUB(@run_now,INTERVAL 28 DAY),DATE_SUB(@run_now,INTERVAL 28 DAY)
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='sachini.wickramasinghe@example.com');
INSERT INTO user_roles (user_id,role_id)
SELECT u.id,r.id FROM users u JOIN roles r ON r.name='CUSTOMER'
WHERE u.email='sachini.wickramasinghe@example.com' AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id=u.id AND ur.role_id=r.id);
INSERT INTO users (full_name,email,phone,password_hash,is_active,created_at,updated_at)
SELECT 'Tharindu Rajapaksha','tharindu.rajapaksha@example.com','0772345676','$2a$10$mymrjXGO.gefx.Uljx/t/uPgvvzxCRJr2xy4ABEjeSl9suEgEEpRq',1,DATE_SUB(@run_now,INTERVAL 28 DAY),DATE_SUB(@run_now,INTERVAL 28 DAY)
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='tharindu.rajapaksha@example.com');
INSERT INTO user_roles (user_id,role_id)
SELECT u.id,r.id FROM users u JOIN roles r ON r.name='CUSTOMER'
WHERE u.email='tharindu.rajapaksha@example.com' AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id=u.id AND ur.role_id=r.id);
INSERT INTO users (full_name,email,phone,password_hash,is_active,created_at,updated_at)
SELECT 'Ishara Gunawardena','ishara.gunawardena@example.com','0712345677','$2a$10$mymrjXGO.gefx.Uljx/t/uPgvvzxCRJr2xy4ABEjeSl9suEgEEpRq',1,DATE_SUB(@run_now,INTERVAL 28 DAY),DATE_SUB(@run_now,INTERVAL 28 DAY)
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='ishara.gunawardena@example.com');
INSERT INTO user_roles (user_id,role_id)
SELECT u.id,r.id FROM users u JOIN roles r ON r.name='CUSTOMER'
WHERE u.email='ishara.gunawardena@example.com' AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id=u.id AND ur.role_id=r.id);
-- Stable ordered pools include both existing customers and the new accounts.
CREATE TEMPORARY TABLE IF NOT EXISTS reservation_customer_pool (row_no INT PRIMARY KEY, user_id BIGINT);
DELETE FROM reservation_customer_pool;
INSERT INTO reservation_customer_pool
SELECT ROW_NUMBER() OVER (ORDER BY u.id),u.id FROM users u
WHERE u.is_active=1 AND u.full_name NOT REGEXP '(demo|test|sample)' AND EXISTS (SELECT 1 FROM user_roles ur JOIN roles r ON r.id=ur.role_id WHERE ur.user_id=u.id AND r.name='CUSTOMER');
CREATE TEMPORARY TABLE IF NOT EXISTS reservation_table_pool (row_no INT PRIMARY KEY, table_id BIGINT);
DELETE FROM reservation_table_pool;
INSERT INTO reservation_table_pool
SELECT ROW_NUMBER() OVER (ORDER BY id),id FROM restaurant_tables
WHERE is_active=1 AND current_status<>'OUT_OF_SERVICE' AND capacity>0;
SET @customer_count=(SELECT COUNT(*) FROM reservation_customer_pool);
SET @table_count=(SELECT COUNT(*) FROM reservation_table_pool);

-- Booking 1: a distinct, repeatable table/date/start-time slot.
SET @booking_date=DATE_ADD(CURDATE(),INTERVAL (-1) DAY);
SET @start_time=TIME('12:00:00');
SET @end_time=ADDTIME(@start_time,'02:00:00');
SET @customer_id=(SELECT user_id FROM reservation_customer_pool WHERE row_no=1+MOD(0,NULLIF(@customer_count,0)));
SET @table_id=(SELECT table_id FROM reservation_table_pool WHERE row_no=1+MOD(0,NULLIF(@table_count,0)));
SET @booking_status=CASE
 WHEN 'COMPLETED'='CANCELLED' THEN 'CANCELLED'
 WHEN TIMESTAMP(@booking_date,@end_time)<=@run_now THEN 'COMPLETED'
 WHEN TIMESTAMP(@booking_date,@start_time)<=@run_now THEN 'CHECKED_IN'
 ELSE 'CONFIRMED' END;
SET @created_at=TIMESTAMP(DATE_SUB(@booking_date,INTERVAL (GREATEST(1,DATEDIFF(@booking_date,CURDATE())+1)) DAY),'10:15:00');
SET @reference=CONCAT('RES-',DATE_FORMAT(@booking_date,'%Y%m%d'),'-',UPPER(HEX(RANDOM_BYTES(2))));
INSERT INTO table_reservations
(booking_reference,customer_id,table_id,reservation_date,start_time,end_time,guest_count,seating_preference,special_request,status,contact_name,contact_phone,cancel_reason,created_at,updated_at)
SELECT @reference,u.id,t.id,@booking_date,@start_time,@end_time,GREATEST(1,t.capacity-0),t.location,'One guest is vegan',@booking_status,
u.full_name,
CASE WHEN u.phone REGEXP '^(077|071|076)[0-9]{7}$' THEN u.phone ELSE CONCAT('077',LPAD(MOD(u.id+2345600,10000000),7,'0')) END,
CASE WHEN @booking_status='CANCELLED' THEN 'Change of plans – will rebook next week' ELSE NULL END,
@created_at,CASE WHEN @booking_status IN ('COMPLETED','NO_SHOW') THEN TIMESTAMP(@booking_date,@end_time)
 WHEN @booking_status='CHECKED_IN' THEN TIMESTAMP(@booking_date,@start_time)
 WHEN @booking_status='CANCELLED' THEN LEAST(CAST(@run_now AS DATETIME),DATE_ADD(CAST(@created_at AS DATETIME),INTERVAL 2 HOUR)) ELSE @created_at END
FROM users u JOIN restaurant_tables t ON t.id=@table_id
WHERE u.id=@customer_id AND t.is_active=1 AND t.current_status<>'OUT_OF_SERVICE'
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date AND r.start_time=CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date
 AND r.status IN ('PENDING','CONFIRMED','CHECKED_IN') AND r.start_time<CAST(@end_time AS TIME(6)) AND r.end_time>CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.booking_reference=@reference);
UPDATE restaurant_tables t JOIN table_reservations r ON r.table_id=t.id
SET t.current_status='OCCUPIED'
WHERE r.booking_reference=@reference AND r.status='CHECKED_IN';
INSERT INTO notifications (user_id,title,message,type,is_read,created_at)
SELECT r.customer_id,'Reservation Confirmed',CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'),'RESERVATION',0,r.created_at
FROM table_reservations r WHERE r.booking_reference=@reference AND r.status='CONFIRMED'
AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.user_id=r.customer_id AND n.title='Reservation Confirmed' AND n.type='RESERVATION' AND n.message=CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'));

-- Booking 2: a distinct, repeatable table/date/start-time slot.
SET @booking_date=DATE_ADD(CURDATE(),INTERVAL (-1) DAY);
SET @start_time=TIME('12:30:00');
SET @end_time=ADDTIME(@start_time,'02:00:00');
SET @customer_id=(SELECT user_id FROM reservation_customer_pool WHERE row_no=1+MOD(1,NULLIF(@customer_count,0)));
SET @table_id=(SELECT table_id FROM reservation_table_pool WHERE row_no=1+MOD(1,NULLIF(@table_count,0)));
SET @booking_status=CASE
 WHEN 'COMPLETED'='CANCELLED' THEN 'CANCELLED'
 WHEN TIMESTAMP(@booking_date,@end_time)<=@run_now THEN 'COMPLETED'
 WHEN TIMESTAMP(@booking_date,@start_time)<=@run_now THEN 'CHECKED_IN'
 ELSE 'CONFIRMED' END;
SET @created_at=TIMESTAMP(DATE_SUB(@booking_date,INTERVAL (GREATEST(2,DATEDIFF(@booking_date,CURDATE())+1)) DAY),'10:15:00');
SET @reference=CONCAT('RES-',DATE_FORMAT(@booking_date,'%Y%m%d'),'-',UPPER(HEX(RANDOM_BYTES(2))));
INSERT INTO table_reservations
(booking_reference,customer_id,table_id,reservation_date,start_time,end_time,guest_count,seating_preference,special_request,status,contact_name,contact_phone,cancel_reason,created_at,updated_at)
SELECT @reference,u.id,t.id,@booking_date,@start_time,@end_time,GREATEST(1,t.capacity-1),t.location,NULL,@booking_status,
u.full_name,
CASE WHEN u.phone REGEXP '^(077|071|076)[0-9]{7}$' THEN u.phone ELSE CONCAT('077',LPAD(MOD(u.id+2345600,10000000),7,'0')) END,
CASE WHEN @booking_status='CANCELLED' THEN 'Change of plans – will rebook next week' ELSE NULL END,
@created_at,CASE WHEN @booking_status IN ('COMPLETED','NO_SHOW') THEN TIMESTAMP(@booking_date,@end_time)
 WHEN @booking_status='CHECKED_IN' THEN TIMESTAMP(@booking_date,@start_time)
 WHEN @booking_status='CANCELLED' THEN LEAST(CAST(@run_now AS DATETIME),DATE_ADD(CAST(@created_at AS DATETIME),INTERVAL 2 HOUR)) ELSE @created_at END
FROM users u JOIN restaurant_tables t ON t.id=@table_id
WHERE u.id=@customer_id AND t.is_active=1 AND t.current_status<>'OUT_OF_SERVICE'
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date AND r.start_time=CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date
 AND r.status IN ('PENDING','CONFIRMED','CHECKED_IN') AND r.start_time<CAST(@end_time AS TIME(6)) AND r.end_time>CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.booking_reference=@reference);
UPDATE restaurant_tables t JOIN table_reservations r ON r.table_id=t.id
SET t.current_status='OCCUPIED'
WHERE r.booking_reference=@reference AND r.status='CHECKED_IN';
INSERT INTO notifications (user_id,title,message,type,is_read,created_at)
SELECT r.customer_id,'Reservation Confirmed',CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'),'RESERVATION',0,r.created_at
FROM table_reservations r WHERE r.booking_reference=@reference AND r.status='CONFIRMED'
AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.user_id=r.customer_id AND n.title='Reservation Confirmed' AND n.type='RESERVATION' AND n.message=CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'));

-- Booking 3: a distinct, repeatable table/date/start-time slot.
SET @booking_date=DATE_ADD(CURDATE(),INTERVAL (-1) DAY);
SET @start_time=TIME('13:30:00');
SET @end_time=ADDTIME(@start_time,'02:00:00');
SET @customer_id=(SELECT user_id FROM reservation_customer_pool WHERE row_no=1+MOD(2,NULLIF(@customer_count,0)));
SET @table_id=(SELECT table_id FROM reservation_table_pool WHERE row_no=1+MOD(2,NULLIF(@table_count,0)));
SET @booking_status=CASE
 WHEN 'COMPLETED'='CANCELLED' THEN 'CANCELLED'
 WHEN TIMESTAMP(@booking_date,@end_time)<=@run_now THEN 'COMPLETED'
 WHEN TIMESTAMP(@booking_date,@start_time)<=@run_now THEN 'CHECKED_IN'
 ELSE 'CONFIRMED' END;
SET @created_at=TIMESTAMP(DATE_SUB(@booking_date,INTERVAL (GREATEST(3,DATEDIFF(@booking_date,CURDATE())+1)) DAY),'10:15:00');
SET @reference=CONCAT('RES-',DATE_FORMAT(@booking_date,'%Y%m%d'),'-',UPPER(HEX(RANDOM_BYTES(2))));
INSERT INTO table_reservations
(booking_reference,customer_id,table_id,reservation_date,start_time,end_time,guest_count,seating_preference,special_request,status,contact_name,contact_phone,cancel_reason,created_at,updated_at)
SELECT @reference,u.id,t.id,@booking_date,@start_time,@end_time,GREATEST(1,t.capacity-0),t.location,'Anniversary, a quiet corner if possible',@booking_status,
u.full_name,
CASE WHEN u.phone REGEXP '^(077|071|076)[0-9]{7}$' THEN u.phone ELSE CONCAT('077',LPAD(MOD(u.id+2345600,10000000),7,'0')) END,
CASE WHEN @booking_status='CANCELLED' THEN 'Change of plans – will rebook next week' ELSE NULL END,
@created_at,CASE WHEN @booking_status IN ('COMPLETED','NO_SHOW') THEN TIMESTAMP(@booking_date,@end_time)
 WHEN @booking_status='CHECKED_IN' THEN TIMESTAMP(@booking_date,@start_time)
 WHEN @booking_status='CANCELLED' THEN LEAST(CAST(@run_now AS DATETIME),DATE_ADD(CAST(@created_at AS DATETIME),INTERVAL 2 HOUR)) ELSE @created_at END
FROM users u JOIN restaurant_tables t ON t.id=@table_id
WHERE u.id=@customer_id AND t.is_active=1 AND t.current_status<>'OUT_OF_SERVICE'
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date AND r.start_time=CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date
 AND r.status IN ('PENDING','CONFIRMED','CHECKED_IN') AND r.start_time<CAST(@end_time AS TIME(6)) AND r.end_time>CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.booking_reference=@reference);
UPDATE restaurant_tables t JOIN table_reservations r ON r.table_id=t.id
SET t.current_status='OCCUPIED'
WHERE r.booking_reference=@reference AND r.status='CHECKED_IN';
INSERT INTO notifications (user_id,title,message,type,is_read,created_at)
SELECT r.customer_id,'Reservation Confirmed',CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'),'RESERVATION',0,r.created_at
FROM table_reservations r WHERE r.booking_reference=@reference AND r.status='CONFIRMED'
AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.user_id=r.customer_id AND n.title='Reservation Confirmed' AND n.type='RESERVATION' AND n.message=CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'));

-- Booking 4: a distinct, repeatable table/date/start-time slot.
SET @booking_date=DATE_ADD(CURDATE(),INTERVAL (-1) DAY);
SET @start_time=TIME('18:30:00');
SET @end_time=ADDTIME(@start_time,'02:00:00');
SET @customer_id=(SELECT user_id FROM reservation_customer_pool WHERE row_no=1+MOD(3,NULLIF(@customer_count,0)));
SET @table_id=(SELECT table_id FROM reservation_table_pool WHERE row_no=1+MOD(3,NULLIF(@table_count,0)));
SET @booking_status=CASE
 WHEN 'NO_SHOW'='CANCELLED' THEN 'CANCELLED'
 WHEN TIMESTAMP(@booking_date,@end_time)<=@run_now THEN 'NO_SHOW'
 WHEN TIMESTAMP(@booking_date,@start_time)<=@run_now THEN 'CHECKED_IN'
 ELSE 'CONFIRMED' END;
SET @created_at=TIMESTAMP(DATE_SUB(@booking_date,INTERVAL (GREATEST(4,DATEDIFF(@booking_date,CURDATE())+1)) DAY),'10:15:00');
SET @reference=CONCAT('RES-',DATE_FORMAT(@booking_date,'%Y%m%d'),'-',UPPER(HEX(RANDOM_BYTES(2))));
INSERT INTO table_reservations
(booking_reference,customer_id,table_id,reservation_date,start_time,end_time,guest_count,seating_preference,special_request,status,contact_name,contact_phone,cancel_reason,created_at,updated_at)
SELECT @reference,u.id,t.id,@booking_date,@start_time,@end_time,GREATEST(1,t.capacity-1),t.location,NULL,@booking_status,
u.full_name,
CASE WHEN u.phone REGEXP '^(077|071|076)[0-9]{7}$' THEN u.phone ELSE CONCAT('077',LPAD(MOD(u.id+2345600,10000000),7,'0')) END,
CASE WHEN @booking_status='CANCELLED' THEN 'Change of plans – will rebook next week' ELSE NULL END,
@created_at,CASE WHEN @booking_status IN ('COMPLETED','NO_SHOW') THEN TIMESTAMP(@booking_date,@end_time)
 WHEN @booking_status='CHECKED_IN' THEN TIMESTAMP(@booking_date,@start_time)
 WHEN @booking_status='CANCELLED' THEN LEAST(CAST(@run_now AS DATETIME),DATE_ADD(CAST(@created_at AS DATETIME),INTERVAL 2 HOUR)) ELSE @created_at END
FROM users u JOIN restaurant_tables t ON t.id=@table_id
WHERE u.id=@customer_id AND t.is_active=1 AND t.current_status<>'OUT_OF_SERVICE'
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date AND r.start_time=CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date
 AND r.status IN ('PENDING','CONFIRMED','CHECKED_IN') AND r.start_time<CAST(@end_time AS TIME(6)) AND r.end_time>CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.booking_reference=@reference);
UPDATE restaurant_tables t JOIN table_reservations r ON r.table_id=t.id
SET t.current_status='OCCUPIED'
WHERE r.booking_reference=@reference AND r.status='CHECKED_IN';
INSERT INTO notifications (user_id,title,message,type,is_read,created_at)
SELECT r.customer_id,'Reservation Confirmed',CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'),'RESERVATION',0,r.created_at
FROM table_reservations r WHERE r.booking_reference=@reference AND r.status='CONFIRMED'
AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.user_id=r.customer_id AND n.title='Reservation Confirmed' AND n.type='RESERVATION' AND n.message=CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'));

-- Booking 5: a distinct, repeatable table/date/start-time slot.
SET @booking_date=DATE_ADD(CURDATE(),INTERVAL (-1) DAY);
SET @start_time=TIME('20:00:00');
SET @end_time=ADDTIME(@start_time,'02:00:00');
SET @customer_id=(SELECT user_id FROM reservation_customer_pool WHERE row_no=1+MOD(4,NULLIF(@customer_count,0)));
SET @table_id=(SELECT table_id FROM reservation_table_pool WHERE row_no=1+MOD(4,NULLIF(@table_count,0)));
SET @booking_status=CASE
 WHEN 'CANCELLED'='CANCELLED' THEN 'CANCELLED'
 WHEN TIMESTAMP(@booking_date,@end_time)<=@run_now THEN 'COMPLETED'
 WHEN TIMESTAMP(@booking_date,@start_time)<=@run_now THEN 'CHECKED_IN'
 ELSE 'CONFIRMED' END;
SET @created_at=TIMESTAMP(DATE_SUB(@booking_date,INTERVAL (GREATEST(5,DATEDIFF(@booking_date,CURDATE())+1)) DAY),'10:15:00');
SET @reference=CONCAT('RES-',DATE_FORMAT(@booking_date,'%Y%m%d'),'-',UPPER(HEX(RANDOM_BYTES(2))));
INSERT INTO table_reservations
(booking_reference,customer_id,table_id,reservation_date,start_time,end_time,guest_count,seating_preference,special_request,status,contact_name,contact_phone,cancel_reason,created_at,updated_at)
SELECT @reference,u.id,t.id,@booking_date,@start_time,@end_time,GREATEST(1,t.capacity-0),t.location,'High chair needed for a toddler',@booking_status,
u.full_name,
CASE WHEN u.phone REGEXP '^(077|071|076)[0-9]{7}$' THEN u.phone ELSE CONCAT('077',LPAD(MOD(u.id+2345600,10000000),7,'0')) END,
CASE WHEN @booking_status='CANCELLED' THEN 'Change of plans – will rebook next week' ELSE NULL END,
@created_at,CASE WHEN @booking_status IN ('COMPLETED','NO_SHOW') THEN TIMESTAMP(@booking_date,@end_time)
 WHEN @booking_status='CHECKED_IN' THEN TIMESTAMP(@booking_date,@start_time)
 WHEN @booking_status='CANCELLED' THEN LEAST(CAST(@run_now AS DATETIME),DATE_ADD(CAST(@created_at AS DATETIME),INTERVAL 2 HOUR)) ELSE @created_at END
FROM users u JOIN restaurant_tables t ON t.id=@table_id
WHERE u.id=@customer_id AND t.is_active=1 AND t.current_status<>'OUT_OF_SERVICE'
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date AND r.start_time=CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date
 AND r.status IN ('PENDING','CONFIRMED','CHECKED_IN') AND r.start_time<CAST(@end_time AS TIME(6)) AND r.end_time>CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.booking_reference=@reference);
UPDATE restaurant_tables t JOIN table_reservations r ON r.table_id=t.id
SET t.current_status='OCCUPIED'
WHERE r.booking_reference=@reference AND r.status='CHECKED_IN';
INSERT INTO notifications (user_id,title,message,type,is_read,created_at)
SELECT r.customer_id,'Reservation Confirmed',CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'),'RESERVATION',0,r.created_at
FROM table_reservations r WHERE r.booking_reference=@reference AND r.status='CONFIRMED'
AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.user_id=r.customer_id AND n.title='Reservation Confirmed' AND n.type='RESERVATION' AND n.message=CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'));

-- Booking 6: a distinct, repeatable table/date/start-time slot.
SET @booking_date=DATE_ADD(CURDATE(),INTERVAL (0) DAY);
SET @start_time=TIME('12:00:00');
SET @end_time=ADDTIME(@start_time,'02:00:00');
SET @customer_id=(SELECT user_id FROM reservation_customer_pool WHERE row_no=1+MOD(5,NULLIF(@customer_count,0)));
SET @table_id=(SELECT table_id FROM reservation_table_pool WHERE row_no=1+MOD(5,NULLIF(@table_count,0)));
SET @booking_status=CASE
 WHEN 'CONFIRMED'='CANCELLED' THEN 'CANCELLED'
 WHEN TIMESTAMP(@booking_date,@end_time)<=@run_now THEN 'COMPLETED'
 WHEN TIMESTAMP(@booking_date,@start_time)<=@run_now THEN 'CHECKED_IN'
 ELSE 'CONFIRMED' END;
SET @created_at=TIMESTAMP(DATE_SUB(@booking_date,INTERVAL (GREATEST(6,DATEDIFF(@booking_date,CURDATE())+1)) DAY),'10:15:00');
SET @reference=CONCAT('RES-',DATE_FORMAT(@booking_date,'%Y%m%d'),'-',UPPER(HEX(RANDOM_BYTES(2))));
INSERT INTO table_reservations
(booking_reference,customer_id,table_id,reservation_date,start_time,end_time,guest_count,seating_preference,special_request,status,contact_name,contact_phone,cancel_reason,created_at,updated_at)
SELECT @reference,u.id,t.id,@booking_date,@start_time,@end_time,GREATEST(1,t.capacity-1),t.location,NULL,@booking_status,
u.full_name,
CASE WHEN u.phone REGEXP '^(077|071|076)[0-9]{7}$' THEN u.phone ELSE CONCAT('077',LPAD(MOD(u.id+2345600,10000000),7,'0')) END,
CASE WHEN @booking_status='CANCELLED' THEN 'Change of plans – will rebook next week' ELSE NULL END,
@created_at,CASE WHEN @booking_status IN ('COMPLETED','NO_SHOW') THEN TIMESTAMP(@booking_date,@end_time)
 WHEN @booking_status='CHECKED_IN' THEN TIMESTAMP(@booking_date,@start_time)
 WHEN @booking_status='CANCELLED' THEN LEAST(CAST(@run_now AS DATETIME),DATE_ADD(CAST(@created_at AS DATETIME),INTERVAL 2 HOUR)) ELSE @created_at END
FROM users u JOIN restaurant_tables t ON t.id=@table_id
WHERE u.id=@customer_id AND t.is_active=1 AND t.current_status<>'OUT_OF_SERVICE'
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date AND r.start_time=CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date
 AND r.status IN ('PENDING','CONFIRMED','CHECKED_IN') AND r.start_time<CAST(@end_time AS TIME(6)) AND r.end_time>CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.booking_reference=@reference);
UPDATE restaurant_tables t JOIN table_reservations r ON r.table_id=t.id
SET t.current_status='OCCUPIED'
WHERE r.booking_reference=@reference AND r.status='CHECKED_IN';
INSERT INTO notifications (user_id,title,message,type,is_read,created_at)
SELECT r.customer_id,'Reservation Confirmed',CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'),'RESERVATION',0,r.created_at
FROM table_reservations r WHERE r.booking_reference=@reference AND r.status='CONFIRMED'
AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.user_id=r.customer_id AND n.title='Reservation Confirmed' AND n.type='RESERVATION' AND n.message=CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'));

-- Booking 7: a distinct, repeatable table/date/start-time slot.
SET @booking_date=DATE_ADD(CURDATE(),INTERVAL (0) DAY);
SET @start_time=TIME('13:30:00');
SET @end_time=ADDTIME(@start_time,'02:00:00');
SET @customer_id=(SELECT user_id FROM reservation_customer_pool WHERE row_no=1+MOD(6,NULLIF(@customer_count,0)));
SET @table_id=(SELECT table_id FROM reservation_table_pool WHERE row_no=1+MOD(6,NULLIF(@table_count,0)));
SET @booking_status=CASE
 WHEN 'PENDING'='CANCELLED' THEN 'CANCELLED'
 WHEN TIMESTAMP(@booking_date,@end_time)<=@run_now THEN 'COMPLETED'
 WHEN TIMESTAMP(@booking_date,@start_time)<=@run_now THEN 'CHECKED_IN'
 ELSE 'PENDING' END;
SET @created_at=TIMESTAMP(DATE_SUB(@booking_date,INTERVAL (GREATEST(7,DATEDIFF(@booking_date,CURDATE())+1)) DAY),'10:15:00');
SET @reference=CONCAT('RES-',DATE_FORMAT(@booking_date,'%Y%m%d'),'-',UPPER(HEX(RANDOM_BYTES(2))));
INSERT INTO table_reservations
(booking_reference,customer_id,table_id,reservation_date,start_time,end_time,guest_count,seating_preference,special_request,status,contact_name,contact_phone,cancel_reason,created_at,updated_at)
SELECT @reference,u.id,t.id,@booking_date,@start_time,@end_time,GREATEST(1,t.capacity-0),t.location,'One guest is vegan',@booking_status,
'Sachini Perera',
CASE WHEN u.phone REGEXP '^(077|071|076)[0-9]{7}$' THEN u.phone ELSE CONCAT('077',LPAD(MOD(u.id+2345600,10000000),7,'0')) END,
CASE WHEN @booking_status='CANCELLED' THEN 'Change of plans – will rebook next week' ELSE NULL END,
@created_at,CASE WHEN @booking_status IN ('COMPLETED','NO_SHOW') THEN TIMESTAMP(@booking_date,@end_time)
 WHEN @booking_status='CHECKED_IN' THEN TIMESTAMP(@booking_date,@start_time)
 WHEN @booking_status='CANCELLED' THEN LEAST(CAST(@run_now AS DATETIME),DATE_ADD(CAST(@created_at AS DATETIME),INTERVAL 2 HOUR)) ELSE @created_at END
FROM users u JOIN restaurant_tables t ON t.id=@table_id
WHERE u.id=@customer_id AND t.is_active=1 AND t.current_status<>'OUT_OF_SERVICE'
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date AND r.start_time=CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date
 AND r.status IN ('PENDING','CONFIRMED','CHECKED_IN') AND r.start_time<CAST(@end_time AS TIME(6)) AND r.end_time>CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.booking_reference=@reference);
UPDATE restaurant_tables t JOIN table_reservations r ON r.table_id=t.id
SET t.current_status='OCCUPIED'
WHERE r.booking_reference=@reference AND r.status='CHECKED_IN';
INSERT INTO notifications (user_id,title,message,type,is_read,created_at)
SELECT r.customer_id,'Reservation Confirmed',CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'),'RESERVATION',0,r.created_at
FROM table_reservations r WHERE r.booking_reference=@reference AND r.status='CONFIRMED'
AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.user_id=r.customer_id AND n.title='Reservation Confirmed' AND n.type='RESERVATION' AND n.message=CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'));

-- Booking 8: a distinct, repeatable table/date/start-time slot.
SET @booking_date=DATE_ADD(CURDATE(),INTERVAL (0) DAY);
SET @start_time=TIME('19:00:00');
SET @end_time=ADDTIME(@start_time,'02:00:00');
SET @customer_id=(SELECT user_id FROM reservation_customer_pool WHERE row_no=1+MOD(7,NULLIF(@customer_count,0)));
SET @table_id=(SELECT table_id FROM reservation_table_pool WHERE row_no=1+MOD(7,NULLIF(@table_count,0)));
SET @booking_status=CASE
 WHEN 'CONFIRMED'='CANCELLED' THEN 'CANCELLED'
 WHEN TIMESTAMP(@booking_date,@end_time)<=@run_now THEN 'COMPLETED'
 WHEN TIMESTAMP(@booking_date,@start_time)<=@run_now THEN 'CHECKED_IN'
 ELSE 'CONFIRMED' END;
SET @created_at=TIMESTAMP(DATE_SUB(@booking_date,INTERVAL (GREATEST(8,DATEDIFF(@booking_date,CURDATE())+1)) DAY),'10:15:00');
SET @reference=CONCAT('RES-',DATE_FORMAT(@booking_date,'%Y%m%d'),'-',UPPER(HEX(RANDOM_BYTES(2))));
INSERT INTO table_reservations
(booking_reference,customer_id,table_id,reservation_date,start_time,end_time,guest_count,seating_preference,special_request,status,contact_name,contact_phone,cancel_reason,created_at,updated_at)
SELECT @reference,u.id,t.id,@booking_date,@start_time,@end_time,GREATEST(1,t.capacity-1),t.location,NULL,@booking_status,
u.full_name,
CASE WHEN u.phone REGEXP '^(077|071|076)[0-9]{7}$' THEN u.phone ELSE CONCAT('077',LPAD(MOD(u.id+2345600,10000000),7,'0')) END,
CASE WHEN @booking_status='CANCELLED' THEN 'Change of plans – will rebook next week' ELSE NULL END,
@created_at,CASE WHEN @booking_status IN ('COMPLETED','NO_SHOW') THEN TIMESTAMP(@booking_date,@end_time)
 WHEN @booking_status='CHECKED_IN' THEN TIMESTAMP(@booking_date,@start_time)
 WHEN @booking_status='CANCELLED' THEN LEAST(CAST(@run_now AS DATETIME),DATE_ADD(CAST(@created_at AS DATETIME),INTERVAL 2 HOUR)) ELSE @created_at END
FROM users u JOIN restaurant_tables t ON t.id=@table_id
WHERE u.id=@customer_id AND t.is_active=1 AND t.current_status<>'OUT_OF_SERVICE'
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date AND r.start_time=CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date
 AND r.status IN ('PENDING','CONFIRMED','CHECKED_IN') AND r.start_time<CAST(@end_time AS TIME(6)) AND r.end_time>CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.booking_reference=@reference);
UPDATE restaurant_tables t JOIN table_reservations r ON r.table_id=t.id
SET t.current_status='OCCUPIED'
WHERE r.booking_reference=@reference AND r.status='CHECKED_IN';
INSERT INTO notifications (user_id,title,message,type,is_read,created_at)
SELECT r.customer_id,'Reservation Confirmed',CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'),'RESERVATION',0,r.created_at
FROM table_reservations r WHERE r.booking_reference=@reference AND r.status='CONFIRMED'
AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.user_id=r.customer_id AND n.title='Reservation Confirmed' AND n.type='RESERVATION' AND n.message=CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'));

-- Booking 9: a distinct, repeatable table/date/start-time slot.
SET @booking_date=DATE_ADD(CURDATE(),INTERVAL (0) DAY);
SET @start_time=TIME('21:00:00');
SET @end_time=ADDTIME(@start_time,'02:00:00');
SET @customer_id=(SELECT user_id FROM reservation_customer_pool WHERE row_no=1+MOD(8,NULLIF(@customer_count,0)));
SET @table_id=(SELECT table_id FROM reservation_table_pool WHERE row_no=1+MOD(8,NULLIF(@table_count,0)));
SET @booking_status=CASE
 WHEN 'CONFIRMED'='CANCELLED' THEN 'CANCELLED'
 WHEN TIMESTAMP(@booking_date,@end_time)<=@run_now THEN 'COMPLETED'
 WHEN TIMESTAMP(@booking_date,@start_time)<=@run_now THEN 'CHECKED_IN'
 ELSE 'CONFIRMED' END;
SET @created_at=TIMESTAMP(DATE_SUB(@booking_date,INTERVAL (GREATEST(9,DATEDIFF(@booking_date,CURDATE())+1)) DAY),'10:15:00');
SET @reference=CONCAT('RES-',DATE_FORMAT(@booking_date,'%Y%m%d'),'-',UPPER(HEX(RANDOM_BYTES(2))));
INSERT INTO table_reservations
(booking_reference,customer_id,table_id,reservation_date,start_time,end_time,guest_count,seating_preference,special_request,status,contact_name,contact_phone,cancel_reason,created_at,updated_at)
SELECT @reference,u.id,t.id,@booking_date,@start_time,@end_time,GREATEST(1,t.capacity-0),t.location,'Anniversary, a quiet corner if possible',@booking_status,
u.full_name,
CASE WHEN u.phone REGEXP '^(077|071|076)[0-9]{7}$' THEN u.phone ELSE CONCAT('077',LPAD(MOD(u.id+2345600,10000000),7,'0')) END,
CASE WHEN @booking_status='CANCELLED' THEN 'Change of plans – will rebook next week' ELSE NULL END,
@created_at,CASE WHEN @booking_status IN ('COMPLETED','NO_SHOW') THEN TIMESTAMP(@booking_date,@end_time)
 WHEN @booking_status='CHECKED_IN' THEN TIMESTAMP(@booking_date,@start_time)
 WHEN @booking_status='CANCELLED' THEN LEAST(CAST(@run_now AS DATETIME),DATE_ADD(CAST(@created_at AS DATETIME),INTERVAL 2 HOUR)) ELSE @created_at END
FROM users u JOIN restaurant_tables t ON t.id=@table_id
WHERE u.id=@customer_id AND t.is_active=1 AND t.current_status<>'OUT_OF_SERVICE'
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date AND r.start_time=CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date
 AND r.status IN ('PENDING','CONFIRMED','CHECKED_IN') AND r.start_time<CAST(@end_time AS TIME(6)) AND r.end_time>CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.booking_reference=@reference);
UPDATE restaurant_tables t JOIN table_reservations r ON r.table_id=t.id
SET t.current_status='OCCUPIED'
WHERE r.booking_reference=@reference AND r.status='CHECKED_IN';
INSERT INTO notifications (user_id,title,message,type,is_read,created_at)
SELECT r.customer_id,'Reservation Confirmed',CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'),'RESERVATION',0,r.created_at
FROM table_reservations r WHERE r.booking_reference=@reference AND r.status='CONFIRMED'
AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.user_id=r.customer_id AND n.title='Reservation Confirmed' AND n.type='RESERVATION' AND n.message=CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'));

-- Booking 10: a distinct, repeatable table/date/start-time slot.
SET @booking_date=DATE_ADD(CURDATE(),INTERVAL (1) DAY);
SET @start_time=TIME('12:30:00');
SET @end_time=ADDTIME(@start_time,'02:00:00');
SET @customer_id=(SELECT user_id FROM reservation_customer_pool WHERE row_no=1+MOD(9,NULLIF(@customer_count,0)));
SET @table_id=(SELECT table_id FROM reservation_table_pool WHERE row_no=1+MOD(9,NULLIF(@table_count,0)));
SET @booking_status=CASE
 WHEN 'CONFIRMED'='CANCELLED' THEN 'CANCELLED'
 WHEN TIMESTAMP(@booking_date,@end_time)<=@run_now THEN 'COMPLETED'
 WHEN TIMESTAMP(@booking_date,@start_time)<=@run_now THEN 'CHECKED_IN'
 ELSE 'CONFIRMED' END;
SET @created_at=TIMESTAMP(DATE_SUB(@booking_date,INTERVAL (GREATEST(10,DATEDIFF(@booking_date,CURDATE())+1)) DAY),'10:15:00');
SET @reference=CONCAT('RES-',DATE_FORMAT(@booking_date,'%Y%m%d'),'-',UPPER(HEX(RANDOM_BYTES(2))));
INSERT INTO table_reservations
(booking_reference,customer_id,table_id,reservation_date,start_time,end_time,guest_count,seating_preference,special_request,status,contact_name,contact_phone,cancel_reason,created_at,updated_at)
SELECT @reference,u.id,t.id,@booking_date,@start_time,@end_time,GREATEST(1,t.capacity-1),t.location,NULL,@booking_status,
u.full_name,
CASE WHEN u.phone REGEXP '^(077|071|076)[0-9]{7}$' THEN u.phone ELSE CONCAT('077',LPAD(MOD(u.id+2345600,10000000),7,'0')) END,
CASE WHEN @booking_status='CANCELLED' THEN 'Change of plans – will rebook next week' ELSE NULL END,
@created_at,CASE WHEN @booking_status IN ('COMPLETED','NO_SHOW') THEN TIMESTAMP(@booking_date,@end_time)
 WHEN @booking_status='CHECKED_IN' THEN TIMESTAMP(@booking_date,@start_time)
 WHEN @booking_status='CANCELLED' THEN LEAST(CAST(@run_now AS DATETIME),DATE_ADD(CAST(@created_at AS DATETIME),INTERVAL 2 HOUR)) ELSE @created_at END
FROM users u JOIN restaurant_tables t ON t.id=@table_id
WHERE u.id=@customer_id AND t.is_active=1 AND t.current_status<>'OUT_OF_SERVICE'
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date AND r.start_time=CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date
 AND r.status IN ('PENDING','CONFIRMED','CHECKED_IN') AND r.start_time<CAST(@end_time AS TIME(6)) AND r.end_time>CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.booking_reference=@reference);
UPDATE restaurant_tables t JOIN table_reservations r ON r.table_id=t.id
SET t.current_status='OCCUPIED'
WHERE r.booking_reference=@reference AND r.status='CHECKED_IN';
INSERT INTO notifications (user_id,title,message,type,is_read,created_at)
SELECT r.customer_id,'Reservation Confirmed',CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'),'RESERVATION',0,r.created_at
FROM table_reservations r WHERE r.booking_reference=@reference AND r.status='CONFIRMED'
AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.user_id=r.customer_id AND n.title='Reservation Confirmed' AND n.type='RESERVATION' AND n.message=CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'));

-- Booking 11: a distinct, repeatable table/date/start-time slot.
SET @booking_date=DATE_ADD(CURDATE(),INTERVAL (1) DAY);
SET @start_time=TIME('19:30:00');
SET @end_time=ADDTIME(@start_time,'02:00:00');
SET @customer_id=(SELECT user_id FROM reservation_customer_pool WHERE row_no=1+MOD(10,NULLIF(@customer_count,0)));
SET @table_id=(SELECT table_id FROM reservation_table_pool WHERE row_no=1+MOD(10,NULLIF(@table_count,0)));
SET @booking_status=CASE
 WHEN 'CONFIRMED'='CANCELLED' THEN 'CANCELLED'
 WHEN TIMESTAMP(@booking_date,@end_time)<=@run_now THEN 'COMPLETED'
 WHEN TIMESTAMP(@booking_date,@start_time)<=@run_now THEN 'CHECKED_IN'
 ELSE 'CONFIRMED' END;
SET @created_at=TIMESTAMP(DATE_SUB(@booking_date,INTERVAL (GREATEST(1,DATEDIFF(@booking_date,CURDATE())+1)) DAY),'10:15:00');
SET @reference=CONCAT('RES-',DATE_FORMAT(@booking_date,'%Y%m%d'),'-',UPPER(HEX(RANDOM_BYTES(2))));
INSERT INTO table_reservations
(booking_reference,customer_id,table_id,reservation_date,start_time,end_time,guest_count,seating_preference,special_request,status,contact_name,contact_phone,cancel_reason,created_at,updated_at)
SELECT @reference,u.id,t.id,@booking_date,@start_time,@end_time,GREATEST(1,t.capacity-0),t.location,'Wheelchair access please',@booking_status,
u.full_name,
CASE WHEN u.phone REGEXP '^(077|071|076)[0-9]{7}$' THEN u.phone ELSE CONCAT('077',LPAD(MOD(u.id+2345600,10000000),7,'0')) END,
CASE WHEN @booking_status='CANCELLED' THEN 'Change of plans – will rebook next week' ELSE NULL END,
@created_at,CASE WHEN @booking_status IN ('COMPLETED','NO_SHOW') THEN TIMESTAMP(@booking_date,@end_time)
 WHEN @booking_status='CHECKED_IN' THEN TIMESTAMP(@booking_date,@start_time)
 WHEN @booking_status='CANCELLED' THEN LEAST(CAST(@run_now AS DATETIME),DATE_ADD(CAST(@created_at AS DATETIME),INTERVAL 2 HOUR)) ELSE @created_at END
FROM users u JOIN restaurant_tables t ON t.id=@table_id
WHERE u.id=@customer_id AND t.is_active=1 AND t.current_status<>'OUT_OF_SERVICE'
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date AND r.start_time=CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date
 AND r.status IN ('PENDING','CONFIRMED','CHECKED_IN') AND r.start_time<CAST(@end_time AS TIME(6)) AND r.end_time>CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.booking_reference=@reference);
UPDATE restaurant_tables t JOIN table_reservations r ON r.table_id=t.id
SET t.current_status='OCCUPIED'
WHERE r.booking_reference=@reference AND r.status='CHECKED_IN';
INSERT INTO notifications (user_id,title,message,type,is_read,created_at)
SELECT r.customer_id,'Reservation Confirmed',CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'),'RESERVATION',0,r.created_at
FROM table_reservations r WHERE r.booking_reference=@reference AND r.status='CONFIRMED'
AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.user_id=r.customer_id AND n.title='Reservation Confirmed' AND n.type='RESERVATION' AND n.message=CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'));

-- Booking 12: a distinct, repeatable table/date/start-time slot.
SET @booking_date=DATE_ADD(CURDATE(),INTERVAL (2) DAY);
SET @start_time=TIME('12:30:00');
SET @end_time=ADDTIME(@start_time,'02:00:00');
SET @customer_id=(SELECT user_id FROM reservation_customer_pool WHERE row_no=1+MOD(11,NULLIF(@customer_count,0)));
SET @table_id=(SELECT table_id FROM reservation_table_pool WHERE row_no=1+MOD(11,NULLIF(@table_count,0)));
SET @booking_status=CASE
 WHEN 'PENDING'='CANCELLED' THEN 'CANCELLED'
 WHEN TIMESTAMP(@booking_date,@end_time)<=@run_now THEN 'COMPLETED'
 WHEN TIMESTAMP(@booking_date,@start_time)<=@run_now THEN 'CHECKED_IN'
 ELSE 'PENDING' END;
SET @created_at=TIMESTAMP(DATE_SUB(@booking_date,INTERVAL (GREATEST(2,DATEDIFF(@booking_date,CURDATE())+1)) DAY),'10:15:00');
SET @reference=CONCAT('RES-',DATE_FORMAT(@booking_date,'%Y%m%d'),'-',UPPER(HEX(RANDOM_BYTES(2))));
INSERT INTO table_reservations
(booking_reference,customer_id,table_id,reservation_date,start_time,end_time,guest_count,seating_preference,special_request,status,contact_name,contact_phone,cancel_reason,created_at,updated_at)
SELECT @reference,u.id,t.id,@booking_date,@start_time,@end_time,GREATEST(1,t.capacity-1),t.location,NULL,@booking_status,
u.full_name,
CASE WHEN u.phone REGEXP '^(077|071|076)[0-9]{7}$' THEN u.phone ELSE CONCAT('077',LPAD(MOD(u.id+2345600,10000000),7,'0')) END,
CASE WHEN @booking_status='CANCELLED' THEN 'Change of plans – will rebook next week' ELSE NULL END,
@created_at,CASE WHEN @booking_status IN ('COMPLETED','NO_SHOW') THEN TIMESTAMP(@booking_date,@end_time)
 WHEN @booking_status='CHECKED_IN' THEN TIMESTAMP(@booking_date,@start_time)
 WHEN @booking_status='CANCELLED' THEN LEAST(CAST(@run_now AS DATETIME),DATE_ADD(CAST(@created_at AS DATETIME),INTERVAL 2 HOUR)) ELSE @created_at END
FROM users u JOIN restaurant_tables t ON t.id=@table_id
WHERE u.id=@customer_id AND t.is_active=1 AND t.current_status<>'OUT_OF_SERVICE'
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date AND r.start_time=CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date
 AND r.status IN ('PENDING','CONFIRMED','CHECKED_IN') AND r.start_time<CAST(@end_time AS TIME(6)) AND r.end_time>CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.booking_reference=@reference);
UPDATE restaurant_tables t JOIN table_reservations r ON r.table_id=t.id
SET t.current_status='OCCUPIED'
WHERE r.booking_reference=@reference AND r.status='CHECKED_IN';
INSERT INTO notifications (user_id,title,message,type,is_read,created_at)
SELECT r.customer_id,'Reservation Confirmed',CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'),'RESERVATION',0,r.created_at
FROM table_reservations r WHERE r.booking_reference=@reference AND r.status='CONFIRMED'
AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.user_id=r.customer_id AND n.title='Reservation Confirmed' AND n.type='RESERVATION' AND n.message=CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'));

-- Booking 13: a distinct, repeatable table/date/start-time slot.
SET @booking_date=DATE_ADD(CURDATE(),INTERVAL (2) DAY);
SET @start_time=TIME('19:30:00');
SET @end_time=ADDTIME(@start_time,'02:00:00');
SET @customer_id=(SELECT user_id FROM reservation_customer_pool WHERE row_no=1+MOD(12,NULLIF(@customer_count,0)));
SET @table_id=(SELECT table_id FROM reservation_table_pool WHERE row_no=1+MOD(12,NULLIF(@table_count,0)));
SET @booking_status=CASE
 WHEN 'CONFIRMED'='CANCELLED' THEN 'CANCELLED'
 WHEN TIMESTAMP(@booking_date,@end_time)<=@run_now THEN 'COMPLETED'
 WHEN TIMESTAMP(@booking_date,@start_time)<=@run_now THEN 'CHECKED_IN'
 ELSE 'CONFIRMED' END;
SET @created_at=TIMESTAMP(DATE_SUB(@booking_date,INTERVAL (GREATEST(3,DATEDIFF(@booking_date,CURDATE())+1)) DAY),'10:15:00');
SET @reference=CONCAT('RES-',DATE_FORMAT(@booking_date,'%Y%m%d'),'-',UPPER(HEX(RANDOM_BYTES(2))));
INSERT INTO table_reservations
(booking_reference,customer_id,table_id,reservation_date,start_time,end_time,guest_count,seating_preference,special_request,status,contact_name,contact_phone,cancel_reason,created_at,updated_at)
SELECT @reference,u.id,t.id,@booking_date,@start_time,@end_time,GREATEST(1,t.capacity-0),t.location,'Birthday dinner – please bring the cake out at 8',@booking_status,
u.full_name,
CASE WHEN u.phone REGEXP '^(077|071|076)[0-9]{7}$' THEN u.phone ELSE CONCAT('077',LPAD(MOD(u.id+2345600,10000000),7,'0')) END,
CASE WHEN @booking_status='CANCELLED' THEN 'Change of plans – will rebook next week' ELSE NULL END,
@created_at,CASE WHEN @booking_status IN ('COMPLETED','NO_SHOW') THEN TIMESTAMP(@booking_date,@end_time)
 WHEN @booking_status='CHECKED_IN' THEN TIMESTAMP(@booking_date,@start_time)
 WHEN @booking_status='CANCELLED' THEN LEAST(CAST(@run_now AS DATETIME),DATE_ADD(CAST(@created_at AS DATETIME),INTERVAL 2 HOUR)) ELSE @created_at END
FROM users u JOIN restaurant_tables t ON t.id=@table_id
WHERE u.id=@customer_id AND t.is_active=1 AND t.current_status<>'OUT_OF_SERVICE'
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date AND r.start_time=CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date
 AND r.status IN ('PENDING','CONFIRMED','CHECKED_IN') AND r.start_time<CAST(@end_time AS TIME(6)) AND r.end_time>CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.booking_reference=@reference);
UPDATE restaurant_tables t JOIN table_reservations r ON r.table_id=t.id
SET t.current_status='OCCUPIED'
WHERE r.booking_reference=@reference AND r.status='CHECKED_IN';
INSERT INTO notifications (user_id,title,message,type,is_read,created_at)
SELECT r.customer_id,'Reservation Confirmed',CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'),'RESERVATION',0,r.created_at
FROM table_reservations r WHERE r.booking_reference=@reference AND r.status='CONFIRMED'
AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.user_id=r.customer_id AND n.title='Reservation Confirmed' AND n.type='RESERVATION' AND n.message=CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'));

-- Booking 14: a distinct, repeatable table/date/start-time slot.
SET @booking_date=DATE_ADD(CURDATE(),INTERVAL (3) DAY);
SET @start_time=TIME('12:30:00');
SET @end_time=ADDTIME(@start_time,'02:00:00');
SET @customer_id=(SELECT user_id FROM reservation_customer_pool WHERE row_no=1+MOD(13,NULLIF(@customer_count,0)));
SET @table_id=(SELECT table_id FROM reservation_table_pool WHERE row_no=1+MOD(13,NULLIF(@table_count,0)));
SET @booking_status=CASE
 WHEN 'CONFIRMED'='CANCELLED' THEN 'CANCELLED'
 WHEN TIMESTAMP(@booking_date,@end_time)<=@run_now THEN 'COMPLETED'
 WHEN TIMESTAMP(@booking_date,@start_time)<=@run_now THEN 'CHECKED_IN'
 ELSE 'CONFIRMED' END;
SET @created_at=TIMESTAMP(DATE_SUB(@booking_date,INTERVAL (GREATEST(4,DATEDIFF(@booking_date,CURDATE())+1)) DAY),'10:15:00');
SET @reference=CONCAT('RES-',DATE_FORMAT(@booking_date,'%Y%m%d'),'-',UPPER(HEX(RANDOM_BYTES(2))));
INSERT INTO table_reservations
(booking_reference,customer_id,table_id,reservation_date,start_time,end_time,guest_count,seating_preference,special_request,status,contact_name,contact_phone,cancel_reason,created_at,updated_at)
SELECT @reference,u.id,t.id,@booking_date,@start_time,@end_time,GREATEST(1,t.capacity-1),t.location,NULL,@booking_status,
'Sachini Perera',
CASE WHEN u.phone REGEXP '^(077|071|076)[0-9]{7}$' THEN u.phone ELSE CONCAT('077',LPAD(MOD(u.id+2345600,10000000),7,'0')) END,
CASE WHEN @booking_status='CANCELLED' THEN 'Change of plans – will rebook next week' ELSE NULL END,
@created_at,CASE WHEN @booking_status IN ('COMPLETED','NO_SHOW') THEN TIMESTAMP(@booking_date,@end_time)
 WHEN @booking_status='CHECKED_IN' THEN TIMESTAMP(@booking_date,@start_time)
 WHEN @booking_status='CANCELLED' THEN LEAST(CAST(@run_now AS DATETIME),DATE_ADD(CAST(@created_at AS DATETIME),INTERVAL 2 HOUR)) ELSE @created_at END
FROM users u JOIN restaurant_tables t ON t.id=@table_id
WHERE u.id=@customer_id AND t.is_active=1 AND t.current_status<>'OUT_OF_SERVICE'
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date AND r.start_time=CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date
 AND r.status IN ('PENDING','CONFIRMED','CHECKED_IN') AND r.start_time<CAST(@end_time AS TIME(6)) AND r.end_time>CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.booking_reference=@reference);
UPDATE restaurant_tables t JOIN table_reservations r ON r.table_id=t.id
SET t.current_status='OCCUPIED'
WHERE r.booking_reference=@reference AND r.status='CHECKED_IN';
INSERT INTO notifications (user_id,title,message,type,is_read,created_at)
SELECT r.customer_id,'Reservation Confirmed',CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'),'RESERVATION',0,r.created_at
FROM table_reservations r WHERE r.booking_reference=@reference AND r.status='CONFIRMED'
AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.user_id=r.customer_id AND n.title='Reservation Confirmed' AND n.type='RESERVATION' AND n.message=CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'));

-- Booking 15: a distinct, repeatable table/date/start-time slot.
SET @booking_date=DATE_ADD(CURDATE(),INTERVAL (3) DAY);
SET @start_time=TIME('19:30:00');
SET @end_time=ADDTIME(@start_time,'02:00:00');
SET @customer_id=(SELECT user_id FROM reservation_customer_pool WHERE row_no=1+MOD(14,NULLIF(@customer_count,0)));
SET @table_id=(SELECT table_id FROM reservation_table_pool WHERE row_no=1+MOD(14,NULLIF(@table_count,0)));
SET @booking_status=CASE
 WHEN 'CANCELLED'='CANCELLED' THEN 'CANCELLED'
 WHEN TIMESTAMP(@booking_date,@end_time)<=@run_now THEN 'COMPLETED'
 WHEN TIMESTAMP(@booking_date,@start_time)<=@run_now THEN 'CHECKED_IN'
 ELSE 'CONFIRMED' END;
SET @created_at=TIMESTAMP(DATE_SUB(@booking_date,INTERVAL (GREATEST(5,DATEDIFF(@booking_date,CURDATE())+1)) DAY),'10:15:00');
SET @reference=CONCAT('RES-',DATE_FORMAT(@booking_date,'%Y%m%d'),'-',UPPER(HEX(RANDOM_BYTES(2))));
INSERT INTO table_reservations
(booking_reference,customer_id,table_id,reservation_date,start_time,end_time,guest_count,seating_preference,special_request,status,contact_name,contact_phone,cancel_reason,created_at,updated_at)
SELECT @reference,u.id,t.id,@booking_date,@start_time,@end_time,GREATEST(1,t.capacity-0),t.location,'Anniversary, a quiet corner if possible',@booking_status,
u.full_name,
CASE WHEN u.phone REGEXP '^(077|071|076)[0-9]{7}$' THEN u.phone ELSE CONCAT('077',LPAD(MOD(u.id+2345600,10000000),7,'0')) END,
CASE WHEN @booking_status='CANCELLED' THEN 'Change of plans – will rebook next week' ELSE NULL END,
@created_at,CASE WHEN @booking_status IN ('COMPLETED','NO_SHOW') THEN TIMESTAMP(@booking_date,@end_time)
 WHEN @booking_status='CHECKED_IN' THEN TIMESTAMP(@booking_date,@start_time)
 WHEN @booking_status='CANCELLED' THEN LEAST(CAST(@run_now AS DATETIME),DATE_ADD(CAST(@created_at AS DATETIME),INTERVAL 2 HOUR)) ELSE @created_at END
FROM users u JOIN restaurant_tables t ON t.id=@table_id
WHERE u.id=@customer_id AND t.is_active=1 AND t.current_status<>'OUT_OF_SERVICE'
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date AND r.start_time=CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date
 AND r.status IN ('PENDING','CONFIRMED','CHECKED_IN') AND r.start_time<CAST(@end_time AS TIME(6)) AND r.end_time>CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.booking_reference=@reference);
UPDATE restaurant_tables t JOIN table_reservations r ON r.table_id=t.id
SET t.current_status='OCCUPIED'
WHERE r.booking_reference=@reference AND r.status='CHECKED_IN';
INSERT INTO notifications (user_id,title,message,type,is_read,created_at)
SELECT r.customer_id,'Reservation Confirmed',CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'),'RESERVATION',0,r.created_at
FROM table_reservations r WHERE r.booking_reference=@reference AND r.status='CONFIRMED'
AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.user_id=r.customer_id AND n.title='Reservation Confirmed' AND n.type='RESERVATION' AND n.message=CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'));

-- Booking 16: a distinct, repeatable table/date/start-time slot.
SET @booking_date=DATE_ADD(CURDATE(),INTERVAL (4) DAY);
SET @start_time=TIME('12:30:00');
SET @end_time=ADDTIME(@start_time,'02:00:00');
SET @customer_id=(SELECT user_id FROM reservation_customer_pool WHERE row_no=1+MOD(15,NULLIF(@customer_count,0)));
SET @table_id=(SELECT table_id FROM reservation_table_pool WHERE row_no=1+MOD(15,NULLIF(@table_count,0)));
SET @booking_status=CASE
 WHEN 'CONFIRMED'='CANCELLED' THEN 'CANCELLED'
 WHEN TIMESTAMP(@booking_date,@end_time)<=@run_now THEN 'COMPLETED'
 WHEN TIMESTAMP(@booking_date,@start_time)<=@run_now THEN 'CHECKED_IN'
 ELSE 'CONFIRMED' END;
SET @created_at=TIMESTAMP(DATE_SUB(@booking_date,INTERVAL (GREATEST(6,DATEDIFF(@booking_date,CURDATE())+1)) DAY),'10:15:00');
SET @reference=CONCAT('RES-',DATE_FORMAT(@booking_date,'%Y%m%d'),'-',UPPER(HEX(RANDOM_BYTES(2))));
INSERT INTO table_reservations
(booking_reference,customer_id,table_id,reservation_date,start_time,end_time,guest_count,seating_preference,special_request,status,contact_name,contact_phone,cancel_reason,created_at,updated_at)
SELECT @reference,u.id,t.id,@booking_date,@start_time,@end_time,GREATEST(1,t.capacity-1),t.location,NULL,@booking_status,
u.full_name,
CASE WHEN u.phone REGEXP '^(077|071|076)[0-9]{7}$' THEN u.phone ELSE CONCAT('077',LPAD(MOD(u.id+2345600,10000000),7,'0')) END,
CASE WHEN @booking_status='CANCELLED' THEN 'Change of plans – will rebook next week' ELSE NULL END,
@created_at,CASE WHEN @booking_status IN ('COMPLETED','NO_SHOW') THEN TIMESTAMP(@booking_date,@end_time)
 WHEN @booking_status='CHECKED_IN' THEN TIMESTAMP(@booking_date,@start_time)
 WHEN @booking_status='CANCELLED' THEN LEAST(CAST(@run_now AS DATETIME),DATE_ADD(CAST(@created_at AS DATETIME),INTERVAL 2 HOUR)) ELSE @created_at END
FROM users u JOIN restaurant_tables t ON t.id=@table_id
WHERE u.id=@customer_id AND t.is_active=1 AND t.current_status<>'OUT_OF_SERVICE'
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date AND r.start_time=CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date
 AND r.status IN ('PENDING','CONFIRMED','CHECKED_IN') AND r.start_time<CAST(@end_time AS TIME(6)) AND r.end_time>CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.booking_reference=@reference);
UPDATE restaurant_tables t JOIN table_reservations r ON r.table_id=t.id
SET t.current_status='OCCUPIED'
WHERE r.booking_reference=@reference AND r.status='CHECKED_IN';
INSERT INTO notifications (user_id,title,message,type,is_read,created_at)
SELECT r.customer_id,'Reservation Confirmed',CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'),'RESERVATION',0,r.created_at
FROM table_reservations r WHERE r.booking_reference=@reference AND r.status='CONFIRMED'
AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.user_id=r.customer_id AND n.title='Reservation Confirmed' AND n.type='RESERVATION' AND n.message=CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'));

-- Booking 17: a distinct, repeatable table/date/start-time slot.
SET @booking_date=DATE_ADD(CURDATE(),INTERVAL (4) DAY);
SET @start_time=TIME('19:30:00');
SET @end_time=ADDTIME(@start_time,'02:00:00');
SET @customer_id=(SELECT user_id FROM reservation_customer_pool WHERE row_no=1+MOD(16,NULLIF(@customer_count,0)));
SET @table_id=(SELECT table_id FROM reservation_table_pool WHERE row_no=1+MOD(16,NULLIF(@table_count,0)));
SET @booking_status=CASE
 WHEN 'CONFIRMED'='CANCELLED' THEN 'CANCELLED'
 WHEN TIMESTAMP(@booking_date,@end_time)<=@run_now THEN 'COMPLETED'
 WHEN TIMESTAMP(@booking_date,@start_time)<=@run_now THEN 'CHECKED_IN'
 ELSE 'CONFIRMED' END;
SET @created_at=TIMESTAMP(DATE_SUB(@booking_date,INTERVAL (GREATEST(7,DATEDIFF(@booking_date,CURDATE())+1)) DAY),'10:15:00');
SET @reference=CONCAT('RES-',DATE_FORMAT(@booking_date,'%Y%m%d'),'-',UPPER(HEX(RANDOM_BYTES(2))));
INSERT INTO table_reservations
(booking_reference,customer_id,table_id,reservation_date,start_time,end_time,guest_count,seating_preference,special_request,status,contact_name,contact_phone,cancel_reason,created_at,updated_at)
SELECT @reference,u.id,t.id,@booking_date,@start_time,@end_time,GREATEST(1,t.capacity-0),t.location,'High chair needed for a toddler',@booking_status,
u.full_name,
CASE WHEN u.phone REGEXP '^(077|071|076)[0-9]{7}$' THEN u.phone ELSE CONCAT('077',LPAD(MOD(u.id+2345600,10000000),7,'0')) END,
CASE WHEN @booking_status='CANCELLED' THEN 'Change of plans – will rebook next week' ELSE NULL END,
@created_at,CASE WHEN @booking_status IN ('COMPLETED','NO_SHOW') THEN TIMESTAMP(@booking_date,@end_time)
 WHEN @booking_status='CHECKED_IN' THEN TIMESTAMP(@booking_date,@start_time)
 WHEN @booking_status='CANCELLED' THEN LEAST(CAST(@run_now AS DATETIME),DATE_ADD(CAST(@created_at AS DATETIME),INTERVAL 2 HOUR)) ELSE @created_at END
FROM users u JOIN restaurant_tables t ON t.id=@table_id
WHERE u.id=@customer_id AND t.is_active=1 AND t.current_status<>'OUT_OF_SERVICE'
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date AND r.start_time=CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date
 AND r.status IN ('PENDING','CONFIRMED','CHECKED_IN') AND r.start_time<CAST(@end_time AS TIME(6)) AND r.end_time>CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.booking_reference=@reference);
UPDATE restaurant_tables t JOIN table_reservations r ON r.table_id=t.id
SET t.current_status='OCCUPIED'
WHERE r.booking_reference=@reference AND r.status='CHECKED_IN';
INSERT INTO notifications (user_id,title,message,type,is_read,created_at)
SELECT r.customer_id,'Reservation Confirmed',CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'),'RESERVATION',0,r.created_at
FROM table_reservations r WHERE r.booking_reference=@reference AND r.status='CONFIRMED'
AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.user_id=r.customer_id AND n.title='Reservation Confirmed' AND n.type='RESERVATION' AND n.message=CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'));

-- Booking 18: a distinct, repeatable table/date/start-time slot.
SET @booking_date=DATE_ADD(CURDATE(),INTERVAL (5) DAY);
SET @start_time=TIME('12:30:00');
SET @end_time=ADDTIME(@start_time,'02:00:00');
SET @customer_id=(SELECT user_id FROM reservation_customer_pool WHERE row_no=1+MOD(17,NULLIF(@customer_count,0)));
SET @table_id=(SELECT table_id FROM reservation_table_pool WHERE row_no=1+MOD(17,NULLIF(@table_count,0)));
SET @booking_status=CASE
 WHEN 'CONFIRMED'='CANCELLED' THEN 'CANCELLED'
 WHEN TIMESTAMP(@booking_date,@end_time)<=@run_now THEN 'COMPLETED'
 WHEN TIMESTAMP(@booking_date,@start_time)<=@run_now THEN 'CHECKED_IN'
 ELSE 'CONFIRMED' END;
SET @created_at=TIMESTAMP(DATE_SUB(@booking_date,INTERVAL (GREATEST(8,DATEDIFF(@booking_date,CURDATE())+1)) DAY),'10:15:00');
SET @reference=CONCAT('RES-',DATE_FORMAT(@booking_date,'%Y%m%d'),'-',UPPER(HEX(RANDOM_BYTES(2))));
INSERT INTO table_reservations
(booking_reference,customer_id,table_id,reservation_date,start_time,end_time,guest_count,seating_preference,special_request,status,contact_name,contact_phone,cancel_reason,created_at,updated_at)
SELECT @reference,u.id,t.id,@booking_date,@start_time,@end_time,GREATEST(1,t.capacity-1),t.location,NULL,@booking_status,
u.full_name,
CASE WHEN u.phone REGEXP '^(077|071|076)[0-9]{7}$' THEN u.phone ELSE CONCAT('077',LPAD(MOD(u.id+2345600,10000000),7,'0')) END,
CASE WHEN @booking_status='CANCELLED' THEN 'Change of plans – will rebook next week' ELSE NULL END,
@created_at,CASE WHEN @booking_status IN ('COMPLETED','NO_SHOW') THEN TIMESTAMP(@booking_date,@end_time)
 WHEN @booking_status='CHECKED_IN' THEN TIMESTAMP(@booking_date,@start_time)
 WHEN @booking_status='CANCELLED' THEN LEAST(CAST(@run_now AS DATETIME),DATE_ADD(CAST(@created_at AS DATETIME),INTERVAL 2 HOUR)) ELSE @created_at END
FROM users u JOIN restaurant_tables t ON t.id=@table_id
WHERE u.id=@customer_id AND t.is_active=1 AND t.current_status<>'OUT_OF_SERVICE'
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date AND r.start_time=CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date
 AND r.status IN ('PENDING','CONFIRMED','CHECKED_IN') AND r.start_time<CAST(@end_time AS TIME(6)) AND r.end_time>CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.booking_reference=@reference);
UPDATE restaurant_tables t JOIN table_reservations r ON r.table_id=t.id
SET t.current_status='OCCUPIED'
WHERE r.booking_reference=@reference AND r.status='CHECKED_IN';
INSERT INTO notifications (user_id,title,message,type,is_read,created_at)
SELECT r.customer_id,'Reservation Confirmed',CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'),'RESERVATION',0,r.created_at
FROM table_reservations r WHERE r.booking_reference=@reference AND r.status='CONFIRMED'
AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.user_id=r.customer_id AND n.title='Reservation Confirmed' AND n.type='RESERVATION' AND n.message=CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'));

-- Booking 19: a distinct, repeatable table/date/start-time slot.
SET @booking_date=DATE_ADD(CURDATE(),INTERVAL (5) DAY);
SET @start_time=TIME('19:30:00');
SET @end_time=ADDTIME(@start_time,'02:00:00');
SET @customer_id=(SELECT user_id FROM reservation_customer_pool WHERE row_no=1+MOD(18,NULLIF(@customer_count,0)));
SET @table_id=(SELECT table_id FROM reservation_table_pool WHERE row_no=1+MOD(18,NULLIF(@table_count,0)));
SET @booking_status=CASE
 WHEN 'CONFIRMED'='CANCELLED' THEN 'CANCELLED'
 WHEN TIMESTAMP(@booking_date,@end_time)<=@run_now THEN 'COMPLETED'
 WHEN TIMESTAMP(@booking_date,@start_time)<=@run_now THEN 'CHECKED_IN'
 ELSE 'CONFIRMED' END;
SET @created_at=TIMESTAMP(DATE_SUB(@booking_date,INTERVAL (GREATEST(9,DATEDIFF(@booking_date,CURDATE())+1)) DAY),'10:15:00');
SET @reference=CONCAT('RES-',DATE_FORMAT(@booking_date,'%Y%m%d'),'-',UPPER(HEX(RANDOM_BYTES(2))));
INSERT INTO table_reservations
(booking_reference,customer_id,table_id,reservation_date,start_time,end_time,guest_count,seating_preference,special_request,status,contact_name,contact_phone,cancel_reason,created_at,updated_at)
SELECT @reference,u.id,t.id,@booking_date,@start_time,@end_time,GREATEST(1,t.capacity-0),t.location,'One guest is vegan',@booking_status,
u.full_name,
CASE WHEN u.phone REGEXP '^(077|071|076)[0-9]{7}$' THEN u.phone ELSE CONCAT('077',LPAD(MOD(u.id+2345600,10000000),7,'0')) END,
CASE WHEN @booking_status='CANCELLED' THEN 'Change of plans – will rebook next week' ELSE NULL END,
@created_at,CASE WHEN @booking_status IN ('COMPLETED','NO_SHOW') THEN TIMESTAMP(@booking_date,@end_time)
 WHEN @booking_status='CHECKED_IN' THEN TIMESTAMP(@booking_date,@start_time)
 WHEN @booking_status='CANCELLED' THEN LEAST(CAST(@run_now AS DATETIME),DATE_ADD(CAST(@created_at AS DATETIME),INTERVAL 2 HOUR)) ELSE @created_at END
FROM users u JOIN restaurant_tables t ON t.id=@table_id
WHERE u.id=@customer_id AND t.is_active=1 AND t.current_status<>'OUT_OF_SERVICE'
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date AND r.start_time=CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date
 AND r.status IN ('PENDING','CONFIRMED','CHECKED_IN') AND r.start_time<CAST(@end_time AS TIME(6)) AND r.end_time>CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.booking_reference=@reference);
UPDATE restaurant_tables t JOIN table_reservations r ON r.table_id=t.id
SET t.current_status='OCCUPIED'
WHERE r.booking_reference=@reference AND r.status='CHECKED_IN';
INSERT INTO notifications (user_id,title,message,type,is_read,created_at)
SELECT r.customer_id,'Reservation Confirmed',CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'),'RESERVATION',0,r.created_at
FROM table_reservations r WHERE r.booking_reference=@reference AND r.status='CONFIRMED'
AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.user_id=r.customer_id AND n.title='Reservation Confirmed' AND n.type='RESERVATION' AND n.message=CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'));

-- Booking 20: a distinct, repeatable table/date/start-time slot.
SET @booking_date=DATE_ADD(CURDATE(),INTERVAL (6) DAY);
SET @start_time=TIME('12:30:00');
SET @end_time=ADDTIME(@start_time,'02:00:00');
SET @customer_id=(SELECT user_id FROM reservation_customer_pool WHERE row_no=1+MOD(19,NULLIF(@customer_count,0)));
SET @table_id=(SELECT table_id FROM reservation_table_pool WHERE row_no=1+MOD(19,NULLIF(@table_count,0)));
SET @booking_status=CASE
 WHEN 'CONFIRMED'='CANCELLED' THEN 'CANCELLED'
 WHEN TIMESTAMP(@booking_date,@end_time)<=@run_now THEN 'COMPLETED'
 WHEN TIMESTAMP(@booking_date,@start_time)<=@run_now THEN 'CHECKED_IN'
 ELSE 'CONFIRMED' END;
SET @created_at=TIMESTAMP(DATE_SUB(@booking_date,INTERVAL (GREATEST(10,DATEDIFF(@booking_date,CURDATE())+1)) DAY),'10:15:00');
SET @reference=CONCAT('RES-',DATE_FORMAT(@booking_date,'%Y%m%d'),'-',UPPER(HEX(RANDOM_BYTES(2))));
INSERT INTO table_reservations
(booking_reference,customer_id,table_id,reservation_date,start_time,end_time,guest_count,seating_preference,special_request,status,contact_name,contact_phone,cancel_reason,created_at,updated_at)
SELECT @reference,u.id,t.id,@booking_date,@start_time,@end_time,GREATEST(1,t.capacity-1),t.location,NULL,@booking_status,
u.full_name,
CASE WHEN u.phone REGEXP '^(077|071|076)[0-9]{7}$' THEN u.phone ELSE CONCAT('077',LPAD(MOD(u.id+2345600,10000000),7,'0')) END,
CASE WHEN @booking_status='CANCELLED' THEN 'Change of plans – will rebook next week' ELSE NULL END,
@created_at,CASE WHEN @booking_status IN ('COMPLETED','NO_SHOW') THEN TIMESTAMP(@booking_date,@end_time)
 WHEN @booking_status='CHECKED_IN' THEN TIMESTAMP(@booking_date,@start_time)
 WHEN @booking_status='CANCELLED' THEN LEAST(CAST(@run_now AS DATETIME),DATE_ADD(CAST(@created_at AS DATETIME),INTERVAL 2 HOUR)) ELSE @created_at END
FROM users u JOIN restaurant_tables t ON t.id=@table_id
WHERE u.id=@customer_id AND t.is_active=1 AND t.current_status<>'OUT_OF_SERVICE'
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date AND r.start_time=CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date
 AND r.status IN ('PENDING','CONFIRMED','CHECKED_IN') AND r.start_time<CAST(@end_time AS TIME(6)) AND r.end_time>CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.booking_reference=@reference);
UPDATE restaurant_tables t JOIN table_reservations r ON r.table_id=t.id
SET t.current_status='OCCUPIED'
WHERE r.booking_reference=@reference AND r.status='CHECKED_IN';
INSERT INTO notifications (user_id,title,message,type,is_read,created_at)
SELECT r.customer_id,'Reservation Confirmed',CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'),'RESERVATION',0,r.created_at
FROM table_reservations r WHERE r.booking_reference=@reference AND r.status='CONFIRMED'
AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.user_id=r.customer_id AND n.title='Reservation Confirmed' AND n.type='RESERVATION' AND n.message=CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'));

-- Booking 21: a distinct, repeatable table/date/start-time slot.
SET @booking_date=DATE_ADD(CURDATE(),INTERVAL (6) DAY);
SET @start_time=TIME('19:30:00');
SET @end_time=ADDTIME(@start_time,'02:00:00');
SET @customer_id=(SELECT user_id FROM reservation_customer_pool WHERE row_no=1+MOD(20,NULLIF(@customer_count,0)));
SET @table_id=(SELECT table_id FROM reservation_table_pool WHERE row_no=1+MOD(20,NULLIF(@table_count,0)));
SET @booking_status=CASE
 WHEN 'CONFIRMED'='CANCELLED' THEN 'CANCELLED'
 WHEN TIMESTAMP(@booking_date,@end_time)<=@run_now THEN 'COMPLETED'
 WHEN TIMESTAMP(@booking_date,@start_time)<=@run_now THEN 'CHECKED_IN'
 ELSE 'CONFIRMED' END;
SET @created_at=TIMESTAMP(DATE_SUB(@booking_date,INTERVAL (GREATEST(1,DATEDIFF(@booking_date,CURDATE())+1)) DAY),'10:15:00');
SET @reference=CONCAT('RES-',DATE_FORMAT(@booking_date,'%Y%m%d'),'-',UPPER(HEX(RANDOM_BYTES(2))));
INSERT INTO table_reservations
(booking_reference,customer_id,table_id,reservation_date,start_time,end_time,guest_count,seating_preference,special_request,status,contact_name,contact_phone,cancel_reason,created_at,updated_at)
SELECT @reference,u.id,t.id,@booking_date,@start_time,@end_time,GREATEST(1,t.capacity-0),t.location,'Anniversary, a quiet corner if possible',@booking_status,
'Sachini Perera',
CASE WHEN u.phone REGEXP '^(077|071|076)[0-9]{7}$' THEN u.phone ELSE CONCAT('077',LPAD(MOD(u.id+2345600,10000000),7,'0')) END,
CASE WHEN @booking_status='CANCELLED' THEN 'Change of plans – will rebook next week' ELSE NULL END,
@created_at,CASE WHEN @booking_status IN ('COMPLETED','NO_SHOW') THEN TIMESTAMP(@booking_date,@end_time)
 WHEN @booking_status='CHECKED_IN' THEN TIMESTAMP(@booking_date,@start_time)
 WHEN @booking_status='CANCELLED' THEN LEAST(CAST(@run_now AS DATETIME),DATE_ADD(CAST(@created_at AS DATETIME),INTERVAL 2 HOUR)) ELSE @created_at END
FROM users u JOIN restaurant_tables t ON t.id=@table_id
WHERE u.id=@customer_id AND t.is_active=1 AND t.current_status<>'OUT_OF_SERVICE'
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date AND r.start_time=CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date
 AND r.status IN ('PENDING','CONFIRMED','CHECKED_IN') AND r.start_time<CAST(@end_time AS TIME(6)) AND r.end_time>CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.booking_reference=@reference);
UPDATE restaurant_tables t JOIN table_reservations r ON r.table_id=t.id
SET t.current_status='OCCUPIED'
WHERE r.booking_reference=@reference AND r.status='CHECKED_IN';
INSERT INTO notifications (user_id,title,message,type,is_read,created_at)
SELECT r.customer_id,'Reservation Confirmed',CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'),'RESERVATION',0,r.created_at
FROM table_reservations r WHERE r.booking_reference=@reference AND r.status='CONFIRMED'
AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.user_id=r.customer_id AND n.title='Reservation Confirmed' AND n.type='RESERVATION' AND n.message=CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'));

-- Booking 22: a distinct, repeatable table/date/start-time slot.
SET @booking_date=DATE_ADD(CURDATE(),INTERVAL (MOD(4-WEEKDAY(CURDATE())+7,7)) DAY);
SET @start_time=TIME('18:30:00');
SET @end_time=ADDTIME(@start_time,'02:00:00');
SET @customer_id=(SELECT user_id FROM reservation_customer_pool WHERE row_no=1+MOD(21,NULLIF(@customer_count,0)));
SET @table_id=(SELECT table_id FROM reservation_table_pool WHERE row_no=1+MOD(21,NULLIF(@table_count,0)));
SET @booking_status=CASE
 WHEN 'CONFIRMED'='CANCELLED' THEN 'CANCELLED'
 WHEN TIMESTAMP(@booking_date,@end_time)<=@run_now THEN 'COMPLETED'
 WHEN TIMESTAMP(@booking_date,@start_time)<=@run_now THEN 'CHECKED_IN'
 ELSE 'CONFIRMED' END;
SET @created_at=TIMESTAMP(DATE_SUB(@booking_date,INTERVAL (GREATEST(2,DATEDIFF(@booking_date,CURDATE())+1)) DAY),'10:15:00');
SET @reference=CONCAT('RES-',DATE_FORMAT(@booking_date,'%Y%m%d'),'-',UPPER(HEX(RANDOM_BYTES(2))));
INSERT INTO table_reservations
(booking_reference,customer_id,table_id,reservation_date,start_time,end_time,guest_count,seating_preference,special_request,status,contact_name,contact_phone,cancel_reason,created_at,updated_at)
SELECT @reference,u.id,t.id,@booking_date,@start_time,@end_time,GREATEST(1,t.capacity-1),t.location,NULL,@booking_status,
u.full_name,
CASE WHEN u.phone REGEXP '^(077|071|076)[0-9]{7}$' THEN u.phone ELSE CONCAT('077',LPAD(MOD(u.id+2345600,10000000),7,'0')) END,
CASE WHEN @booking_status='CANCELLED' THEN 'Change of plans – will rebook next week' ELSE NULL END,
@created_at,CASE WHEN @booking_status IN ('COMPLETED','NO_SHOW') THEN TIMESTAMP(@booking_date,@end_time)
 WHEN @booking_status='CHECKED_IN' THEN TIMESTAMP(@booking_date,@start_time)
 WHEN @booking_status='CANCELLED' THEN LEAST(CAST(@run_now AS DATETIME),DATE_ADD(CAST(@created_at AS DATETIME),INTERVAL 2 HOUR)) ELSE @created_at END
FROM users u JOIN restaurant_tables t ON t.id=@table_id
WHERE u.id=@customer_id AND t.is_active=1 AND t.current_status<>'OUT_OF_SERVICE'
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date AND r.start_time=CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date
 AND r.status IN ('PENDING','CONFIRMED','CHECKED_IN') AND r.start_time<CAST(@end_time AS TIME(6)) AND r.end_time>CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.booking_reference=@reference);
UPDATE restaurant_tables t JOIN table_reservations r ON r.table_id=t.id
SET t.current_status='OCCUPIED'
WHERE r.booking_reference=@reference AND r.status='CHECKED_IN';
INSERT INTO notifications (user_id,title,message,type,is_read,created_at)
SELECT r.customer_id,'Reservation Confirmed',CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'),'RESERVATION',0,r.created_at
FROM table_reservations r WHERE r.booking_reference=@reference AND r.status='CONFIRMED'
AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.user_id=r.customer_id AND n.title='Reservation Confirmed' AND n.type='RESERVATION' AND n.message=CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'));

-- Booking 23: a distinct, repeatable table/date/start-time slot.
SET @booking_date=DATE_ADD(CURDATE(),INTERVAL (MOD(4-WEEKDAY(CURDATE())+7,7)) DAY);
SET @start_time=TIME('20:00:00');
SET @end_time=ADDTIME(@start_time,'02:00:00');
SET @customer_id=(SELECT user_id FROM reservation_customer_pool WHERE row_no=1+MOD(22,NULLIF(@customer_count,0)));
SET @table_id=(SELECT table_id FROM reservation_table_pool WHERE row_no=1+MOD(22,NULLIF(@table_count,0)));
SET @booking_status=CASE
 WHEN 'CONFIRMED'='CANCELLED' THEN 'CANCELLED'
 WHEN TIMESTAMP(@booking_date,@end_time)<=@run_now THEN 'COMPLETED'
 WHEN TIMESTAMP(@booking_date,@start_time)<=@run_now THEN 'CHECKED_IN'
 ELSE 'CONFIRMED' END;
SET @created_at=TIMESTAMP(DATE_SUB(@booking_date,INTERVAL (GREATEST(3,DATEDIFF(@booking_date,CURDATE())+1)) DAY),'10:15:00');
SET @reference=CONCAT('RES-',DATE_FORMAT(@booking_date,'%Y%m%d'),'-',UPPER(HEX(RANDOM_BYTES(2))));
INSERT INTO table_reservations
(booking_reference,customer_id,table_id,reservation_date,start_time,end_time,guest_count,seating_preference,special_request,status,contact_name,contact_phone,cancel_reason,created_at,updated_at)
SELECT @reference,u.id,t.id,@booking_date,@start_time,@end_time,GREATEST(1,t.capacity-0),t.location,'Wheelchair access please',@booking_status,
u.full_name,
CASE WHEN u.phone REGEXP '^(077|071|076)[0-9]{7}$' THEN u.phone ELSE CONCAT('077',LPAD(MOD(u.id+2345600,10000000),7,'0')) END,
CASE WHEN @booking_status='CANCELLED' THEN 'Change of plans – will rebook next week' ELSE NULL END,
@created_at,CASE WHEN @booking_status IN ('COMPLETED','NO_SHOW') THEN TIMESTAMP(@booking_date,@end_time)
 WHEN @booking_status='CHECKED_IN' THEN TIMESTAMP(@booking_date,@start_time)
 WHEN @booking_status='CANCELLED' THEN LEAST(CAST(@run_now AS DATETIME),DATE_ADD(CAST(@created_at AS DATETIME),INTERVAL 2 HOUR)) ELSE @created_at END
FROM users u JOIN restaurant_tables t ON t.id=@table_id
WHERE u.id=@customer_id AND t.is_active=1 AND t.current_status<>'OUT_OF_SERVICE'
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date AND r.start_time=CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date
 AND r.status IN ('PENDING','CONFIRMED','CHECKED_IN') AND r.start_time<CAST(@end_time AS TIME(6)) AND r.end_time>CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.booking_reference=@reference);
UPDATE restaurant_tables t JOIN table_reservations r ON r.table_id=t.id
SET t.current_status='OCCUPIED'
WHERE r.booking_reference=@reference AND r.status='CHECKED_IN';
INSERT INTO notifications (user_id,title,message,type,is_read,created_at)
SELECT r.customer_id,'Reservation Confirmed',CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'),'RESERVATION',0,r.created_at
FROM table_reservations r WHERE r.booking_reference=@reference AND r.status='CONFIRMED'
AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.user_id=r.customer_id AND n.title='Reservation Confirmed' AND n.type='RESERVATION' AND n.message=CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'));

-- Booking 24: a distinct, repeatable table/date/start-time slot.
SET @booking_date=DATE_ADD(CURDATE(),INTERVAL (MOD(5-WEEKDAY(CURDATE())+7,7)) DAY);
SET @start_time=TIME('18:30:00');
SET @end_time=ADDTIME(@start_time,'02:00:00');
SET @customer_id=(SELECT user_id FROM reservation_customer_pool WHERE row_no=1+MOD(23,NULLIF(@customer_count,0)));
SET @table_id=(SELECT table_id FROM reservation_table_pool WHERE row_no=1+MOD(23,NULLIF(@table_count,0)));
SET @booking_status=CASE
 WHEN 'CONFIRMED'='CANCELLED' THEN 'CANCELLED'
 WHEN TIMESTAMP(@booking_date,@end_time)<=@run_now THEN 'COMPLETED'
 WHEN TIMESTAMP(@booking_date,@start_time)<=@run_now THEN 'CHECKED_IN'
 ELSE 'CONFIRMED' END;
SET @created_at=TIMESTAMP(DATE_SUB(@booking_date,INTERVAL (GREATEST(4,DATEDIFF(@booking_date,CURDATE())+1)) DAY),'10:15:00');
SET @reference=CONCAT('RES-',DATE_FORMAT(@booking_date,'%Y%m%d'),'-',UPPER(HEX(RANDOM_BYTES(2))));
INSERT INTO table_reservations
(booking_reference,customer_id,table_id,reservation_date,start_time,end_time,guest_count,seating_preference,special_request,status,contact_name,contact_phone,cancel_reason,created_at,updated_at)
SELECT @reference,u.id,t.id,@booking_date,@start_time,@end_time,GREATEST(1,t.capacity-1),t.location,NULL,@booking_status,
u.full_name,
CASE WHEN u.phone REGEXP '^(077|071|076)[0-9]{7}$' THEN u.phone ELSE CONCAT('077',LPAD(MOD(u.id+2345600,10000000),7,'0')) END,
CASE WHEN @booking_status='CANCELLED' THEN 'Change of plans – will rebook next week' ELSE NULL END,
@created_at,CASE WHEN @booking_status IN ('COMPLETED','NO_SHOW') THEN TIMESTAMP(@booking_date,@end_time)
 WHEN @booking_status='CHECKED_IN' THEN TIMESTAMP(@booking_date,@start_time)
 WHEN @booking_status='CANCELLED' THEN LEAST(CAST(@run_now AS DATETIME),DATE_ADD(CAST(@created_at AS DATETIME),INTERVAL 2 HOUR)) ELSE @created_at END
FROM users u JOIN restaurant_tables t ON t.id=@table_id
WHERE u.id=@customer_id AND t.is_active=1 AND t.current_status<>'OUT_OF_SERVICE'
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date AND r.start_time=CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date
 AND r.status IN ('PENDING','CONFIRMED','CHECKED_IN') AND r.start_time<CAST(@end_time AS TIME(6)) AND r.end_time>CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.booking_reference=@reference);
UPDATE restaurant_tables t JOIN table_reservations r ON r.table_id=t.id
SET t.current_status='OCCUPIED'
WHERE r.booking_reference=@reference AND r.status='CHECKED_IN';
INSERT INTO notifications (user_id,title,message,type,is_read,created_at)
SELECT r.customer_id,'Reservation Confirmed',CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'),'RESERVATION',0,r.created_at
FROM table_reservations r WHERE r.booking_reference=@reference AND r.status='CONFIRMED'
AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.user_id=r.customer_id AND n.title='Reservation Confirmed' AND n.type='RESERVATION' AND n.message=CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'));

-- Booking 25: a distinct, repeatable table/date/start-time slot.
SET @booking_date=DATE_ADD(CURDATE(),INTERVAL (MOD(5-WEEKDAY(CURDATE())+7,7)) DAY);
SET @start_time=TIME('20:00:00');
SET @end_time=ADDTIME(@start_time,'02:00:00');
SET @customer_id=(SELECT user_id FROM reservation_customer_pool WHERE row_no=1+MOD(24,NULLIF(@customer_count,0)));
SET @table_id=(SELECT table_id FROM reservation_table_pool WHERE row_no=1+MOD(24,NULLIF(@table_count,0)));
SET @booking_status=CASE
 WHEN 'CONFIRMED'='CANCELLED' THEN 'CANCELLED'
 WHEN TIMESTAMP(@booking_date,@end_time)<=@run_now THEN 'COMPLETED'
 WHEN TIMESTAMP(@booking_date,@start_time)<=@run_now THEN 'CHECKED_IN'
 ELSE 'CONFIRMED' END;
SET @created_at=TIMESTAMP(DATE_SUB(@booking_date,INTERVAL (GREATEST(5,DATEDIFF(@booking_date,CURDATE())+1)) DAY),'10:15:00');
SET @reference=CONCAT('RES-',DATE_FORMAT(@booking_date,'%Y%m%d'),'-',UPPER(HEX(RANDOM_BYTES(2))));
INSERT INTO table_reservations
(booking_reference,customer_id,table_id,reservation_date,start_time,end_time,guest_count,seating_preference,special_request,status,contact_name,contact_phone,cancel_reason,created_at,updated_at)
SELECT @reference,u.id,t.id,@booking_date,@start_time,@end_time,GREATEST(1,t.capacity-0),t.location,'Birthday dinner – please bring the cake out at 8',@booking_status,
u.full_name,
CASE WHEN u.phone REGEXP '^(077|071|076)[0-9]{7}$' THEN u.phone ELSE CONCAT('077',LPAD(MOD(u.id+2345600,10000000),7,'0')) END,
CASE WHEN @booking_status='CANCELLED' THEN 'Change of plans – will rebook next week' ELSE NULL END,
@created_at,CASE WHEN @booking_status IN ('COMPLETED','NO_SHOW') THEN TIMESTAMP(@booking_date,@end_time)
 WHEN @booking_status='CHECKED_IN' THEN TIMESTAMP(@booking_date,@start_time)
 WHEN @booking_status='CANCELLED' THEN LEAST(CAST(@run_now AS DATETIME),DATE_ADD(CAST(@created_at AS DATETIME),INTERVAL 2 HOUR)) ELSE @created_at END
FROM users u JOIN restaurant_tables t ON t.id=@table_id
WHERE u.id=@customer_id AND t.is_active=1 AND t.current_status<>'OUT_OF_SERVICE'
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date AND r.start_time=CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.table_id=t.id AND r.reservation_date=@booking_date
 AND r.status IN ('PENDING','CONFIRMED','CHECKED_IN') AND r.start_time<CAST(@end_time AS TIME(6)) AND r.end_time>CAST(@start_time AS TIME(6)))
AND NOT EXISTS (SELECT 1 FROM table_reservations r WHERE r.booking_reference=@reference);
UPDATE restaurant_tables t JOIN table_reservations r ON r.table_id=t.id
SET t.current_status='OCCUPIED'
WHERE r.booking_reference=@reference AND r.status='CHECKED_IN';
INSERT INTO notifications (user_id,title,message,type,is_read,created_at)
SELECT r.customer_id,'Reservation Confirmed',CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'),'RESERVATION',0,r.created_at
FROM table_reservations r WHERE r.booking_reference=@reference AND r.status='CONFIRMED'
AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.user_id=r.customer_id AND n.title='Reservation Confirmed' AND n.type='RESERVATION' AND n.message=CONCAT('Your reservation ',r.booking_reference,' has been confirmed.'));

COMMIT;
SELECT (SELECT COUNT(*) FROM users)-@users_before AS customers_inserted,
       (SELECT COUNT(*) FROM table_reservations)-@reservations_before AS reservations_inserted,
       (SELECT COUNT(*) FROM notifications)-@notifications_before AS notifications_inserted;
SELECT reservation_date,status,COUNT(*) FROM table_reservations GROUP BY reservation_date,status ORDER BY reservation_date;
-- Must return zero rows: active bookings cannot overlap.
SELECT a.booking_reference AS first_booking,b.booking_reference AS second_booking,a.table_id,a.reservation_date
FROM table_reservations a JOIN table_reservations b ON a.id<b.id AND a.table_id=b.table_id AND a.reservation_date=b.reservation_date
WHERE a.status IN ('PENDING','CONFIRMED','CHECKED_IN') AND b.status IN ('PENDING','CONFIRMED','CHECKED_IN')
AND a.start_time<b.end_time AND a.end_time>b.start_time;
-- Must return zero rows: no expired active bookings, or unarrived past starts.
SELECT booking_reference,status FROM table_reservations
WHERE (status IN ('PENDING','CONFIRMED') AND TIMESTAMP(reservation_date,start_time)<@run_now)
OR (status='CHECKED_IN' AND TIMESTAMP(reservation_date,end_time)<=@run_now);
DROP TEMPORARY TABLE reservation_customer_pool;
DROP TEMPORARY TABLE reservation_table_pool;
SET time_zone=@previous_time_zone;
