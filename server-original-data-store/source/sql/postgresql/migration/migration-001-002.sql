----------------------------------------------------------------------
--
--  Migration script from version 1 to 2 of the database
--
----------------------------------------------------------------------
CREATE DOMAIN TIME_STAMP AS TIMESTAMP WITH TIME ZONE;

ALTER TABLE DATA_SET_FILES ADD COLUMN LAST_MODIFIED TIME_STAMP;
