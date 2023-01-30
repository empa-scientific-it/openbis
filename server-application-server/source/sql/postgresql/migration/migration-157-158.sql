CREATE DOMAIN EDMS_ADDRESS_TYPE AS TEXT CHECK (VALUE IN ('OPENBIS', 'URL', 'FILE_SYSTEM'));

ALTER TABLE external_data_management_systems ADD COLUMN address_type EDMS_ADDRESS_TYPE;
UPDATE external_data_management_systems SET address_type='OPENBIS' WHERE is_openbis = true;
UPDATE external_data_management_systems SET address_type='URL' WHERE is_openbis = false;
ALTER TABLE external_data_management_systems ALTER COLUMN address_type SET NOT NULL;

ALTER TABLE external_data_management_systems RENAME COLUMN url_template TO address;
UPDATE external_data_management_systems SET address='UNKNOWN' WHERE address IS NULL;
ALTER TABLE external_data_management_systems ALTER COLUMN address SET NOT NULL;

ALTER TABLE external_data_management_systems DROP COLUMN is_openbis;

UPDATE external_data_management_systems SET code = (SELECT coalesce(code, '') FROM external_data_management_systems order by coalesce(length(code), '0') desc limit 1) || id WHERE code IS NULL;
ALTER TABLE external_data_management_systems ALTER COLUMN code SET NOT NULL;