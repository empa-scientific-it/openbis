CREATE DOMAIN IDENTIFIER AS VARCHAR(200);

alter table material_properties_history add column vocabulary_term identifier;
alter table material_properties_history add column material identifier;
update material_properties_history set vocabulary_term = (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = cvte_id) where cvte_id is not null;
update material_properties_history set material = (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = mate_prop_id) where mate_prop_id is not null;
alter table material_properties_history drop column cvte_id cascade;
alter table material_properties_history drop column mate_prop_id cascade;
ALTER TABLE MATERIAL_PROPERTIES_HISTORY ADD CONSTRAINT MAPRH_CK CHECK 
	((VALUE IS NOT NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL) OR 
	 (VALUE IS NULL AND VOCABULARY_TERM IS NOT NULL AND MATERIAL IS NULL) OR
	 (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NOT NULL)
	);

alter table experiment_properties_history add column vocabulary_term identifier;
alter table experiment_properties_history add column material identifier;
update experiment_properties_history set vocabulary_term = (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = cvte_id) where cvte_id is not null;
update experiment_properties_history set material = (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = mate_prop_id) where mate_prop_id is not null;
alter table experiment_properties_history drop column cvte_id cascade;
alter table experiment_properties_history drop column mate_prop_id cascade;
ALTER TABLE EXPERIMENT_PROPERTIES_HISTORY ADD CONSTRAINT EXPRH_CK CHECK 
	((VALUE IS NOT NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL) OR 
	 (VALUE IS NULL AND VOCABULARY_TERM IS NOT NULL AND MATERIAL IS NULL) OR
	 (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NOT NULL)
	);

alter table sample_properties_history add column vocabulary_term identifier;
alter table sample_properties_history add column material identifier;
update sample_properties_history set vocabulary_term = (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = cvte_id) where cvte_id is not null;
update sample_properties_history set material = (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = mate_prop_id) where mate_prop_id is not null;
alter table sample_properties_history drop column cvte_id cascade;
alter table sample_properties_history drop column mate_prop_id cascade;
ALTER TABLE SAMPLE_PROPERTIES_HISTORY ADD CONSTRAINT SAPRH_CK CHECK 
	((VALUE IS NOT NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL) OR 
	 (VALUE IS NULL AND VOCABULARY_TERM IS NOT NULL AND MATERIAL IS NULL) OR
	 (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NOT NULL)
	);

alter table data_set_properties_history add column vocabulary_term identifier;
alter table data_set_properties_history add column material identifier;
update data_set_properties_history set vocabulary_term = (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = cvte_id) where cvte_id is not null;
update data_set_properties_history set material = (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = mate_prop_id) where mate_prop_id is not null;
alter table data_set_properties_history drop column cvte_id cascade;
alter table data_set_properties_history drop column mate_prop_id cascade;
ALTER TABLE DATA_SET_PROPERTIES_HISTORY ADD CONSTRAINT DSPRH_CK CHECK 
	((VALUE IS NOT NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL) OR 
	 (VALUE IS NULL AND VOCABULARY_TERM IS NOT NULL AND MATERIAL IS NULL) OR
	 (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NOT NULL)
	);

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
         VOCABULARY_TERM,
         MATERIAL, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('MATERIAL_PROPERTY_ID_SEQ'), 
         OLD.MATE_ID, 
         OLD.MTPT_ID, 
         OLD.VALUE, 
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
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
         VOCABULARY_TERM,
         MATERIAL, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('MATERIAL_PROPERTY_ID_SEQ'), 
         OLD.MATE_ID, 
         OLD.MTPT_ID, 
         OLD.VALUE, 
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
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
         VOCABULARY_TERM,
         MATERIAL, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('EXPERIMENT_PROPERTY_ID_SEQ'), 
         OLD.EXPE_ID, 
         OLD.ETPT_ID, 
         OLD.VALUE, 
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
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
         VOCABULARY_TERM,
         MATERIAL, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('EXPERIMENT_PROPERTY_ID_SEQ'), 
         OLD.EXPE_ID, 
         OLD.ETPT_ID, 
         OLD.VALUE, 
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
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
         VOCABULARY_TERM,
         MATERIAL, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('SAMPLE_PROPERTY_ID_SEQ'), 
         OLD.SAMP_ID, 
         OLD.STPT_ID, 
         OLD.VALUE, 
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
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
         VOCABULARY_TERM,
         MATERIAL, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('SAMPLE_PROPERTY_ID_SEQ'), 
         OLD.SAMP_ID, 
         OLD.STPT_ID, 
         OLD.VALUE, 
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
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
         VOCABULARY_TERM,
         MATERIAL, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('SAMPLE_PROPERTY_ID_SEQ'), 
         OLD.DS_ID, 
         OLD.DSTPT_ID, 
         OLD.VALUE, 
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
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
         VOCABULARY_TERM,
         MATERIAL, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('SAMPLE_PROPERTY_ID_SEQ'), 
         OLD.DS_ID, 
         OLD.DSTPT_ID, 
         OLD.VALUE, 
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         current_timestamp
       );
