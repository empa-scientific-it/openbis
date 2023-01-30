-- Migration from 065 to 066


ALTER TABLE EXTERNAL_DATA ADD COLUMN SHARE_ID CODE;
ALTER TABLE EXTERNAL_DATA ADD COLUMN SIZE ORDINAL_INT;

----------------------------------------------------------------------------------------------------
-- Redo part of migration from 056 to 057
--
-- Introduction of a new data type - XML, was done in migration but data.sql wasn't updated.
-- Add the XML data type it if it doesn't exist.
----------------------------------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION insert_xml_data_type_if_not_exists() RETURNS void AS $$
DECLARE
	exists bool;
BEGIN
    SELECT true INTO exists
    FROM data_types WHERE code = 'XML';
    
    IF exists IS NULL then
       -- XML data type doesn't exist - insert it
       INSERT INTO data_types (id, code, description) 
              VALUES (nextval('data_type_id_seq'), 'XML', 'XML document');
    END IF;	
END;
$$ LANGUAGE 'plpgsql';


SELECT insert_xml_data_type_if_not_exists();
DROP FUNCTION insert_xml_data_type_if_not_exists();

----------------------------------------------------------------------------------------------------
-- Redo part of migration from 063 to 064
--
-- Domain SCRIPT_TYPE created in migration has different values than the one in domain.sql.
-- Alter the domain.
----------------------------------------------------------------------------------------------------

ALTER DOMAIN script_type DROP CONSTRAINT script_type_check;
ALTER DOMAIN script_type ADD CONSTRAINT script_type_check CHECK (VALUE IN ('DYNAMIC_PROPERTY', 'MANAGED_PROPERTY'));

--------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------
-- This is a screening specific migration. Nothing will be performed on openBIS databases 
-- which are not screening specific.
-- 
-- This migration for each existing connection between oligo well, oligo material and gene material
-- creates a direct connection between the well and the gene. 
--------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------
 
CREATE OR REPLACE FUNCTION INSERT_DATASET_TYPE_IF_NOT_PRESENT(new_code text, new_description text) RETURNS void AS $$
DECLARE
	exists bool;
BEGIN
	
	select true into exists from data_set_types	where code = new_code;
	
	if exists IS NOT NULL then 
		return;
	end if;   
	
	insert into data_set_types(
		id, 
		code, description, 
		dbin_id)
	values(
			nextval('data_set_type_id_seq'), 
			new_code,
			new_description,
			(select id from database_instances where is_original_source = 'T')
		);
		
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION CREATE_HCS_DATASET_TYPES() RETURNS void AS $$
DECLARE
	hcs_image_dataset_exists bool;
	unknown_file_format_exists bool;
BEGIN
	
	select true 
	into hcs_image_dataset_exists
	from data_set_types 
	where code = 'HCS_IMAGE';
	
	if hcs_image_dataset_exists IS NULL then 
		-- skip migration if there is not HCS_IMAGE dataset type
		return;
	end if;   
	
	-- insert unknown file format if it is not yet present -- 
	select true into unknown_file_format_exists from file_format_types where code = 'UNKNOWN';
	if unknown_file_format_exists IS NULL then 
			insert into file_format_types(
				id, 
				code, 
				description,
				dbin_id) 
			values(
				nextval('file_format_type_id_seq'), 
				'UNKNOWN', 
				'Unknown file format',
				(select id from database_instances where is_original_source = 'T')
			);	
	end if;   
	
	PERFORM INSERT_DATASET_TYPE_IF_NOT_PRESENT(
			'HCS_IMAGE_RAW',	
			'Raw High Content Screening Images');
	PERFORM INSERT_DATASET_TYPE_IF_NOT_PRESENT(
			'HCS_IMAGE_OVERVIEW',
			'Overview High Content Screening Images. Generated from raw images.');
	PERFORM INSERT_DATASET_TYPE_IF_NOT_PRESENT(
			'HCS_IMAGE_SEGMENTATION',
			'HCS Segmentation Images (overlays)');
	PERFORM INSERT_DATASET_TYPE_IF_NOT_PRESENT(
			'HCS_ANALYSIS_WELL_FEATURES',
			'HCS image analysis well feature vectors.');
	PERFORM INSERT_DATASET_TYPE_IF_NOT_PRESENT(
			'HCS_ANALYSIS_CELL_SEGMENTATION',
			'HCS image analysis cell segmentation');
	PERFORM INSERT_DATASET_TYPE_IF_NOT_PRESENT(	
			'HCS_ANALYSIS_CELL_FEATURES',
			'HCS image analysis cell feature vectors');
	PERFORM INSERT_DATASET_TYPE_IF_NOT_PRESENT(
			'HCS_ANALYSIS_CELL_CLASS',
			'HCS image analysis cell classification');

END;
$$ LANGUAGE 'plpgsql';

select CREATE_HCS_DATASET_TYPES();
drop function CREATE_HCS_DATASET_TYPES();
drop function INSERT_DATASET_TYPE_IF_NOT_PRESENT(text, text);

--------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------=======
