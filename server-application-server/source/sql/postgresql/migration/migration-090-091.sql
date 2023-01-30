-- Migration from 090 to 091
-- S121
-- Change type definition of QUERIES.EXPRESSION and FILTERS_EXPRESSION from VARCHAR(2000) to TEXT
-- Add this column to property tables:
--   PERS_ID_AUTHOR TECH_ID NOT NULL
-- Add these columns to property history tables:
--   PERS_ID_AUTHOR TECH_ID NOT NULL
--   VALID_FROM_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP 
-- and make them be filled properly by the history trigger condition.

-- Relax length restriction on QUERIES.EXPRESSION
ALTER TABLE queries ALTER COLUMN expression SET DATA TYPE text;

-- Relax length restriction on FILTERS.EXPRESSION
ALTER TABLE filters ALTER COLUMN expression SET DATA TYPE text;

-- Experiment properties
ALTER TABLE experiment_properties ADD COLUMN pers_id_author TECH_ID;
UPDATE experiment_properties SET pers_id_author=(SELECT id FROM persons WHERE user_id = 'system');
ALTER TABLE experiment_properties ALTER COLUMN pers_id_author SET NOT NULL;

ALTER TABLE experiment_properties_history ADD COLUMN pers_id_author TECH_ID;
UPDATE experiment_properties_history SET pers_id_author=(SELECT id FROM persons WHERE user_id = 'system');
ALTER TABLE experiment_properties_history ALTER COLUMN pers_id_author SET NOT NULL;

ALTER TABLE experiment_properties_history ADD COLUMN valid_from_timestamp TIME_STAMP;
UPDATE experiment_properties_history SET valid_from_timestamp=(SELECT registration_timestamp FROM persons WHERE user_id = 'system');
ALTER TABLE experiment_properties_history ALTER COLUMN valid_from_timestamp SET NOT NULL;

-- Sample properties
ALTER TABLE sample_properties ADD COLUMN pers_id_author TECH_ID;
UPDATE sample_properties SET pers_id_author=(SELECT id FROM persons WHERE user_id = 'system');
ALTER TABLE sample_properties ALTER COLUMN pers_id_author SET NOT NULL;

ALTER TABLE sample_properties_history ADD COLUMN pers_id_author TECH_ID;
UPDATE sample_properties_history SET pers_id_author=(SELECT id FROM persons WHERE user_id = 'system');
ALTER TABLE sample_properties_history ALTER COLUMN pers_id_author SET NOT NULL;

ALTER TABLE sample_properties_history ADD COLUMN valid_from_timestamp TIME_STAMP;
UPDATE sample_properties_history SET valid_from_timestamp=(SELECT registration_timestamp FROM persons WHERE user_id = 'system');
ALTER TABLE sample_properties_history ALTER COLUMN valid_from_timestamp SET NOT NULL;

-- Dataset properties
ALTER TABLE data_set_properties ADD COLUMN pers_id_author TECH_ID;
UPDATE data_set_properties SET pers_id_author=(SELECT id FROM persons WHERE user_id = 'system');
ALTER TABLE data_set_properties ALTER COLUMN pers_id_author SET NOT NULL;

ALTER TABLE data_set_properties_history ADD COLUMN pers_id_author TECH_ID;
UPDATE data_set_properties_history SET pers_id_author=(SELECT id FROM persons WHERE user_id = 'system');
ALTER TABLE data_set_properties_history ALTER COLUMN pers_id_author SET NOT NULL;

ALTER TABLE data_set_properties_history ADD COLUMN valid_from_timestamp TIME_STAMP;
UPDATE data_set_properties_history SET valid_from_timestamp=(SELECT registration_timestamp FROM persons WHERE user_id = 'system');
ALTER TABLE data_set_properties_history ALTER COLUMN valid_from_timestamp SET NOT NULL;

-- Material properties
ALTER TABLE material_properties ADD COLUMN pers_id_author TECH_ID;
UPDATE material_properties SET pers_id_author=(SELECT id FROM persons WHERE user_id = 'system');
ALTER TABLE material_properties ALTER COLUMN pers_id_author SET NOT NULL;

ALTER TABLE material_properties_history ADD COLUMN pers_id_author TECH_ID;
UPDATE material_properties_history SET pers_id_author=(SELECT id FROM persons WHERE user_id = 'system');
ALTER TABLE material_properties_history ALTER COLUMN pers_id_author SET NOT NULL;

ALTER TABLE material_properties_history ADD COLUMN valid_from_timestamp TIME_STAMP;
UPDATE material_properties_history SET valid_from_timestamp=(SELECT registration_timestamp FROM persons WHERE user_id = 'system');
ALTER TABLE material_properties_history ALTER COLUMN valid_from_timestamp SET NOT NULL;

----------------------------------------------------------------------------------------------------
-- Rules for properties history
----------------------------------------------------------------------------------------------------

-- Material Properties --

CREATE OR REPLACE RULE material_properties_update AS
    ON UPDATE TO material_properties 
    WHERE (OLD.VALUE IS NOT NULL AND decode(substring(OLD.value from 1 for 1), 'escape') != E'\\xefbfbd' AND OLD.VALUE != NEW.VALUE) 
        OR (OLD.CVTE_ID IS NOT NULL AND OLD.CVTE_ID != NEW.CVTE_ID) 
        OR (OLD.MATE_PROP_ID IS NOT NULL AND OLD.MATE_PROP_ID != NEW.MATE_PROP_ID)
    DO ALSO 
       INSERT INTO material_properties_history (
         ID, 
         MATE_ID, 
         MTPT_ID, 
         VALUE, 
         CVTE_ID, 
         MATE_PROP_ID, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('MATERIAL_PROPERTY_ID_SEQ'), 
         OLD.MATE_ID, 
         OLD.MTPT_ID, 
         OLD.VALUE, 
         OLD.CVTE_ID, 
         OLD.MATE_PROP_ID, 
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         current_timestamp
       );
       
CREATE OR REPLACE RULE material_properties_delete AS
    ON DELETE TO material_properties 
    WHERE (OLD.VALUE IS NOT NULL AND decode(substring(OLD.value from 1 for 1), 'escape') != E'\\xefbfbd')
        OR OLD.CVTE_ID IS NOT NULL 
        OR OLD.MATE_PROP_ID IS NOT NULL
    DO ALSO 
       INSERT INTO material_properties_history (
         ID, 
         MATE_ID, 
         MTPT_ID, 
         VALUE, 
         CVTE_ID, 
         MATE_PROP_ID, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('MATERIAL_PROPERTY_ID_SEQ'), 
         OLD.MATE_ID, 
         OLD.MTPT_ID, 
         OLD.VALUE, 
         OLD.CVTE_ID, 
         OLD.MATE_PROP_ID, 
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         current_timestamp
       );
       
-- Experiment Properties --

CREATE OR REPLACE RULE experiment_properties_update AS
    ON UPDATE TO experiment_properties 
    WHERE (OLD.VALUE IS NOT NULL AND decode(substring(OLD.value from 1 for 1), 'escape') != E'\\xefbfbd' AND OLD.VALUE != NEW.VALUE) 
        OR (OLD.CVTE_ID IS NOT NULL AND OLD.CVTE_ID != NEW.CVTE_ID) 
        OR (OLD.MATE_PROP_ID IS NOT NULL AND OLD.MATE_PROP_ID != NEW.MATE_PROP_ID)
    DO ALSO 
       INSERT INTO experiment_properties_history (
         ID, 
         EXPE_ID,
         ETPT_ID, 
         VALUE, 
         CVTE_ID, 
         MATE_PROP_ID, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('EXPERIMENT_PROPERTY_ID_SEQ'), 
         OLD.EXPE_ID, 
         OLD.ETPT_ID, 
         OLD.VALUE, 
         OLD.CVTE_ID, 
         OLD.MATE_PROP_ID, 
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         current_timestamp
       );
       
CREATE OR REPLACE RULE experiment_properties_delete AS
    ON DELETE TO experiment_properties 
    WHERE (OLD.VALUE IS NOT NULL AND decode(substring(OLD.value from 1 for 1), 'escape') != E'\\xefbfbd')
        OR OLD.CVTE_ID IS NOT NULL 
        OR OLD.MATE_PROP_ID IS NOT NULL
    DO ALSO 
       INSERT INTO experiment_properties_history (
         ID, 
         EXPE_ID,
         ETPT_ID, 
         VALUE, 
         CVTE_ID, 
         MATE_PROP_ID, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('EXPERIMENT_PROPERTY_ID_SEQ'), 
         OLD.EXPE_ID, 
         OLD.ETPT_ID, 
         OLD.VALUE, 
         OLD.CVTE_ID, 
         OLD.MATE_PROP_ID, 
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         current_timestamp
       );
       
-- Sample Properties --

CREATE OR REPLACE RULE sample_properties_update AS
    ON UPDATE TO sample_properties
    WHERE (OLD.VALUE IS NOT NULL AND decode(substring(OLD.value from 1 for 1), 'escape') != E'\\xefbfbd' AND OLD.VALUE != NEW.VALUE) 
        OR (OLD.CVTE_ID IS NOT NULL AND OLD.CVTE_ID != NEW.CVTE_ID) 
        OR (OLD.MATE_PROP_ID IS NOT NULL AND OLD.MATE_PROP_ID != NEW.MATE_PROP_ID)
    DO ALSO
       INSERT INTO sample_properties_history (
         ID, 
         SAMP_ID,
         STPT_ID, 
         VALUE, 
         CVTE_ID, 
         MATE_PROP_ID, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('SAMPLE_PROPERTY_ID_SEQ'), 
         OLD.SAMP_ID, 
         OLD.STPT_ID, 
         OLD.VALUE, 
         OLD.CVTE_ID, 
         OLD.MATE_PROP_ID, 
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         current_timestamp
       );
       
CREATE OR REPLACE RULE sample_properties_delete AS
    ON DELETE TO sample_properties 
    WHERE (OLD.VALUE IS NOT NULL AND decode(substring(OLD.value from 1 for 1), 'escape') != E'\\xefbfbd')
        OR OLD.CVTE_ID IS NOT NULL 
        OR OLD.MATE_PROP_ID IS NOT NULL
    DO ALSO
       INSERT INTO sample_properties_history (
         ID, 
         SAMP_ID,
         STPT_ID, 
         VALUE, 
         CVTE_ID, 
         MATE_PROP_ID, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('SAMPLE_PROPERTY_ID_SEQ'), 
         OLD.SAMP_ID, 
         OLD.STPT_ID, 
         OLD.VALUE, 
         OLD.CVTE_ID, 
         OLD.MATE_PROP_ID, 
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         current_timestamp
       );
       
-- Data Set Properties --

CREATE OR REPLACE RULE data_set_properties_update AS
    ON UPDATE TO data_set_properties 
    WHERE (OLD.VALUE IS NOT NULL AND decode(substring(OLD.value from 1 for 1), 'escape') != E'\\xefbfbd' AND OLD.VALUE != NEW.VALUE) 
        OR (OLD.CVTE_ID IS NOT NULL AND OLD.CVTE_ID != NEW.CVTE_ID) 
        OR (OLD.MATE_PROP_ID IS NOT NULL AND OLD.MATE_PROP_ID != NEW.MATE_PROP_ID)
    DO ALSO
       INSERT INTO data_set_properties_history (
         ID, 
         DS_ID,
         DSTPT_ID, 
         VALUE, 
         CVTE_ID, 
         MATE_PROP_ID, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('DATA_SET_PROPERTY_ID_SEQ'), 
         OLD.DS_ID, 
         OLD.DSTPT_ID, 
         OLD.VALUE, 
         OLD.CVTE_ID, 
         OLD.MATE_PROP_ID, 
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         current_timestamp
       );

CREATE OR REPLACE RULE data_set_properties_delete AS
    ON DELETE TO data_set_properties 
    WHERE (OLD.VALUE IS NOT NULL AND decode(substring(OLD.value from 1 for 1), 'escape') != E'\\xefbfbd')
        OR OLD.CVTE_ID IS NOT NULL 
        OR OLD.MATE_PROP_ID IS NOT NULL
    DO ALSO
       INSERT INTO data_set_properties_history (
         ID, 
         DS_ID,
         DSTPT_ID, 
         VALUE, 
         CVTE_ID, 
         MATE_PROP_ID, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('DATA_SET_PROPERTY_ID_SEQ'), 
         OLD.DS_ID, 
         OLD.DSTPT_ID, 
         OLD.VALUE, 
         OLD.CVTE_ID, 
         OLD.MATE_PROP_ID, 
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         current_timestamp
       );
