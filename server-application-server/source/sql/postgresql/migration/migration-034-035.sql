------------------------------------------------------------------------------------
-- Extend events table
------------------------------------------------------------------------------------

ALTER TABLE events
		ADD COLUMN entity_type VARCHAR(80);
ALTER TABLE events
		ADD COLUMN identifier VARCHAR(250);
		
-- Creating check constraints

ALTER TABLE events ADD CONSTRAINT evnt_et_enum_ck CHECK 
		(entity_type IN ('ATTACHMENT', 'DATASET', 'EXPERIMENT', 'MATERIAL', 'PROJECT', 'PROPERTY_TYPE', 'SAMPLE', 'VOCABULARY')); 

-- simple migration of events (all old rows contain data set deletion information)

UPDATE events SET 
		entity_type = 'DATASET',
		identifier = data_id,
		description = data_id;

ALTER TABLE events
    DROP COLUMN data_id;

-- remove deleted data sets with their properties and relations to other data sets

CREATE VIEW deleted_data_ids AS
    SELECT id FROM data WHERE is_deleted = 'TRUE';
DELETE FROM data_set_relationships 
		WHERE data_id_parent IN (SELECT * FROM deleted_data_ids) 
		OR data_id_child IN (SELECT * FROM deleted_data_ids);
DELETE FROM data_set_properties 
    WHERE ds_id IN (SELECT * FROM deleted_data_ids);
DELETE FROM external_data
		WHERE data_id IN (SELECT * FROM deleted_data_ids);
DELETE FROM data WHERE is_deleted = 'TRUE';    
DROP VIEW deleted_data_ids;

-- remove old columns

ALTER TABLE data
		DROP COLUMN is_deleted;
		
-- add constraints

ALTER TABLE events
    ALTER COLUMN entity_type SET NOT NULL;
ALTER TABLE events
    ALTER COLUMN identifier SET NOT NULL;
ALTER TABLE events
		ADD CONSTRAINT evnt_bk_uk UNIQUE(EVENT_TYPE,ENTITY_TYPE,IDENTIFIER);