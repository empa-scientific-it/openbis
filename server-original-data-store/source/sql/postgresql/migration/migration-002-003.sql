----------------------------------------------------------------------
--
--  Migration script from version 2 to 3 of the database
--
----------------------------------------------------------------------
ALTER TABLE data_set_files ALTER COLUMN last_modified SET DEFAULT NOW();
UPDATE data_set_files SET last_modified = DEFAULT WHERE last_modified IS NULL; 
ALTER TABLE data_set_files ALTER COLUMN last_modified SET NOT NULL; 
