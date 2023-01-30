-- Migration from 050 to 051

-- change archiving status names to be consistent with UI & introduce a domain 
ALTER TABLE external_data DROP CONSTRAINT exda_status_enum_ck;

UPDATE external_data SET status = 'AVAILABLE' WHERE status = 'ACTIVE';
UPDATE external_data SET status = 'UNARCHIVE_PENDING' WHERE status = 'ACTIVATION_IN_PROGRESS';
UPDATE external_data SET status = 'ARCHIVE_PENDING' WHERE status = 'ARCHIVIZATION_IN_PROGRESS';

CREATE DOMAIN archiving_status AS VARCHAR(100);
ALTER DOMAIN archiving_status ADD CONSTRAINT archiving_status_check 
      CHECK (VALUE IN ('LOCKED', 'AVAILABLE', 'ARCHIVED', 'ARCHIVE_PENDING', 'UNARCHIVE_PENDING'));

ALTER TABLE external_data ALTER COLUMN status TYPE archiving_status;
ALTER TABLE external_data ALTER COLUMN status SET DEFAULT 'AVAILABLE';

-- add is_archiver_configured flag to data_stores table
ALTER TABLE data_stores ADD COLUMN is_archiver_configured BOOLEAN_CHAR NOT NULL DEFAULT 'F';