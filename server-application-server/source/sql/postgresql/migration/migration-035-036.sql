-- Change type description length

ALTER TABLE sample_types ALTER COLUMN description TYPE DESCRIPTION_250;
ALTER TABLE data_set_types ALTER COLUMN description TYPE DESCRIPTION_250;
ALTER TABLE experiment_types ALTER COLUMN description TYPE DESCRIPTION_250;
ALTER TABLE material_types ALTER COLUMN description TYPE DESCRIPTION_250;
ALTER TABLE controlled_vocabularies ALTER COLUMN description TYPE DESCRIPTION_250;
ALTER TABLE data_types ALTER COLUMN description TYPE DESCRIPTION_250;
ALTER TABLE file_format_types ALTER COLUMN description TYPE DESCRIPTION_250;
ALTER TABLE locator_types ALTER COLUMN description TYPE DESCRIPTION_250;
ALTER TABLE property_types ALTER COLUMN description TYPE DESCRIPTION_250;

DROP TYPE description_80;

-- Add attachment title and description

CREATE DOMAIN TITLE_100 AS VARCHAR(100);

ALTER TABLE attachments ADD COLUMN title TITLE_100;
ALTER TABLE attachments ADD COLUMN description DESCRIPTION_1000;

-- Add 'POWER_USER' to AUTHORIZATION_ROLE domain

ALTER DOMAIN authorization_role DROP CONSTRAINT authorization_role_check;
ALTER DOMAIN authorization_role ADD CONSTRAINT authorization_role_check CHECK (VALUE IN ('ADMIN', 'POWER_USER', 'USER', 'OBSERVER', 'ETL_SERVER'));