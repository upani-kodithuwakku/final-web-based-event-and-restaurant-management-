-- Small-group occasions, also seeded for new installations by EventCatalogSeeder.
-- Safe to rerun; preserves existing staff edits.
SET NAMES utf8mb4;
START TRANSACTION;
INSERT INTO event_packages (name,event_type,description,base_price,minimum_guests,maximum_guests,is_active,created_at,updated_at)
SELECT 'Birthday Table for Your Favourite People','BIRTHDAY','An intimate birthday dinner with a sharing menu, a mini celebration cake and a decorated table for your closest people.',12000,2,8,1,NOW(),NOW()
WHERE NOT EXISTS (SELECT 1 FROM event_packages WHERE name='Birthday Table for Your Favourite People');
INSERT INTO event_packages (name,event_type,description,base_price,minimum_guests,maximum_guests,is_active,created_at,updated_at)
SELECT 'Just the Two of Us','ANNIVERSARY','A candlelit anniversary dinner with a three-course menu, a floral table setting and a dessert to share.',15000,2,8,1,NOW(),NOW()
WHERE NOT EXISTS (SELECT 1 FROM event_packages WHERE name='Just the Two of Us');
INSERT INTO event_packages (name,event_type,description,base_price,minimum_guests,maximum_guests,is_active,created_at,updated_at)
SELECT 'A Little Family Celebration','FAMILY','Bring your closest family together for a Sri Lankan sharing feast, dessert and a relaxed private table.',18000,3,8,1,NOW(),NOW()
WHERE NOT EXISTS (SELECT 1 FROM event_packages WHERE name='A Little Family Celebration');
COMMIT;
