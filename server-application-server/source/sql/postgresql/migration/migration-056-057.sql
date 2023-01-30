-- Migration from 056 to 057

-- Introduction of a new data type - XML.

INSERT INTO data_types (id, code, description) VALUES (nextval('data_type_id_seq'), 'XML', 'XML document');

-- Property types of XML data type can optionally hold XMLSchema and XSLT. 

CREATE DOMAIN text_value AS text;

ALTER TABLE property_types ADD COLUMN schema text_value;
ALTER TABLE property_types ADD COLUMN transformation text_value;

-- Remove length restriction for property values - change domain from generic_value to text_value.
ALTER TABLE data_set_properties ALTER COLUMN value TYPE text_value;
ALTER TABLE experiment_properties ALTER COLUMN value TYPE text_value;
ALTER TABLE material_properties ALTER COLUMN value TYPE text_value;
ALTER TABLE sample_properties ALTER COLUMN value TYPE text_value;
DROP DOMAIN generic_value;