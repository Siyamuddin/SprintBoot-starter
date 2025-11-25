-- Repair script for V2 migration failure
-- Run this script manually if you encounter "Detected failed migration to version 2" error
-- This script repairs the Flyway schema history table
--
-- Usage: Connect to your database and run this script
-- Example: mysql -u root -p saas-app < scripts/repair_flyway_v2.sql

-- Mark the failed V2 migration as successful (if it was partially applied)
UPDATE flyway_schema_history 
SET success = 1 
WHERE version = '2' 
  AND success = 0;

-- Note: If the migration wasn't recorded at all, you may need to manually verify
-- that the columns exist and then insert a record into flyway_schema_history

