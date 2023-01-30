----------------------------------------------------------------------------------------------------
-- Rules for properties history
----------------------------------------------------------------------------------------------------

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
