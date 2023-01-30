-- Migration from 168 to 169

------------------------------------------------------------------------------------
--  Purpose:  allow longer codes 
------------------------------------------------------------------------------------

-- drop rules for tbale and views which depend on CODE
drop rule material_properties_update on material_properties;
drop rule material_properties_delete on material_properties;
drop rule sample_properties_update on sample_properties;
drop rule sample_properties_delete on sample_properties;
drop rule experiment_properties_update on experiment_properties;
drop rule experiment_properties_delete on experiment_properties;
drop rule data_set_properties_update on data_set_properties;
drop rule data_set_properties_delete on data_set_properties;
drop rule sample_experiment_update on samples_all;
drop rule sample_experiment_remove_update on samples_all;
drop rule sample_experiment_insert on samples_all;
drop rule sample_experiment_delete on samples_all;
drop rule sample_container_update on samples_all;
drop rule sample_container_remove_update on samples_all;
drop rule sample_container_insert on samples_all;
drop rule sample_container_delete on samples_all;
drop rule dataset_experiment_update on data_all;
drop rule dataset_experiment_remove_update on data_all;
drop rule dataset_experiment_insert on data_all;
drop rule dataset_experiment_delete on data_all;
drop rule dataset_sample_update on data_all;
drop rule dataset_sample_remove_update on data_all;
drop rule dataset_sample_insert on data_all;
drop rule dataset_sample_delete on data_all;
drop rule data_relationship_insert on data_set_relationships_all;
drop rule data_relationship_delete on data_set_relationships_all;
drop rule data_relationship_update on data_set_relationships_all;
drop rule data_relationship_trash_update on data_set_relationships_all;
drop rule data_relationship_trash_revert_update on data_set_relationships_all;
drop rule sample_parent_child_insert on sample_relationships_all;
drop rule sample_parent_child_delete on sample_relationships_all;
drop rule sample_parent_child_update on sample_relationships_all;
drop rule sample_parent_child_revert_update on sample_relationships_all;
drop rule experiment_project_update on experiments_all;
drop rule experiment_project_remove_update on experiments_all;
drop rule experiment_project_insert on experiments_all;
drop rule experiment_project_delete on experiments_all;
drop rule project_space_update on projects;
drop rule project_space_remove_update on projects;
drop rule project_space_insert on projects;
drop rule sample_project_update on samples_all;
drop rule sample_project_remove_update on samples_all;
drop rule sample_project_insert on samples_all;
drop rule sample_space_update on samples_all;
drop rule sample_space_remove_update on samples_all;
drop rule sample_space_insert on samples_all;
drop rule sample_insert on samples;
drop rule sample_update on samples;
drop rule sample_delete on samples;
drop rule sample_deleted_update on samples_deleted;
drop rule sample_deleted_delete on samples_deleted;
drop rule experiment_insert on experiments;
drop rule experiment_update on experiments;
drop rule experiment_delete on experiments;
drop rule experiments_deleted_update on experiments_deleted; 
drop rule experiments_deleted_delete on experiments_deleted;
drop rule data_insert on data;
drop rule data_update on data;
drop rule data_all on data;
drop rule data_deleted_update on data_deleted;
drop rule data_deleted_delete on data_deleted;

-- drop views depending on CODE
drop view data;
drop view data_deleted;
drop view experiments;
drop view experiments_deleted;
drop view samples;
drop view samples_deleted;

-- Switch all uses of the domain code (there are a lot of them!) to use varchar(60)
alter table AUTHORIZATION_GROUPS alter column CODE type varchar(60);
alter table CONTROLLED_VOCABULARIES alter column CODE type varchar(60);
alter table DATA_ALL alter column CODE type varchar(60), alter column DATA_PRODUCER_CODE type varchar(60);
alter table DATA_SET_TYPES alter column CODE type varchar(60);
alter table DATA_STORES alter column CODE type varchar(60), alter column UUID type varchar(60);
alter table DATA_TYPES alter column CODE type varchar(60);
alter table EXPERIMENT_TYPES alter column CODE type varchar(60);
alter table EXPERIMENTS_ALL alter column PERM_ID type varchar(60), alter column CODE type varchar(60);
alter table EXTERNAL_DATA alter column SHARE_ID type varchar(60);
alter table EXTERNAL_DATA_MANAGEMENT_SYSTEMS alter column CODE type varchar(60);
alter table FILE_FORMAT_TYPES alter column CODE type varchar(60);
alter table LOCATOR_TYPES alter column CODE type varchar(60);
alter table MATERIAL_TYPES alter column CODE type varchar(60);
alter table MATERIALS alter column CODE type varchar(60);
alter table METAPROJECTS alter column NAME type varchar(60);
alter table OPERATION_EXECUTIONS alter column CODE type varchar(60);
alter table PROJECTS alter column CODE type varchar(60), alter column PERM_ID type varchar(60);
alter table PROPERTY_TYPES alter column CODE type varchar(60);
alter table RELATIONSHIP_TYPES alter column CODE type varchar(60);
alter table QUERIES alter column DB_KEY type varchar(60), alter column ENTITY_TYPE_CODE type varchar(60);
alter table SAMPLE_TYPES alter column CODE type varchar(60), alter column generated_code_prefix type varchar(60);
alter table SAMPLES_ALL alter column PERM_ID type varchar(60), alter column CODE type varchar(60);
alter table SEMANTIC_ANNOTATIONS alter column PERM_ID type varchar(60);
alter table SPACES alter column CODE type varchar(60);

-- Convert CODE to VARCHAR(100)
drop DOMAIN CODE;
create DOMAIN CODE as varchar(100);

-- Switch all columns back to using the domain code
alter table AUTHORIZATION_GROUPS alter column CODE type CODE;
alter table CONTROLLED_VOCABULARIES alter column CODE type CODE;
alter table DATA_ALL alter column CODE type CODE, alter column DATA_PRODUCER_CODE type CODE;
alter table DATA_SET_TYPES alter column CODE type CODE;
alter table DATA_STORES alter column CODE type CODE, alter column UUID type CODE;
alter table DATA_TYPES alter column CODE type CODE;
alter table EXPERIMENT_TYPES alter column CODE type CODE;
alter table EXPERIMENTS_ALL alter column PERM_ID type CODE, alter column CODE type CODE;
alter table EXTERNAL_DATA alter column SHARE_ID type CODE;
alter table EXTERNAL_DATA_MANAGEMENT_SYSTEMS alter column CODE type CODE;
alter table FILE_FORMAT_TYPES alter column CODE type CODE;
alter table LOCATOR_TYPES alter column CODE type CODE;
alter table MATERIAL_TYPES alter column CODE type CODE;
alter table MATERIALS alter column CODE type CODE;
alter table METAPROJECTS alter column NAME type CODE;
alter table OPERATION_EXECUTIONS alter column CODE type CODE;
alter table PROJECTS alter column CODE type CODE, alter column PERM_ID type CODE;
alter table PROPERTY_TYPES alter column CODE type CODE;
alter table RELATIONSHIP_TYPES alter column CODE type CODE;
alter table QUERIES alter column DB_KEY type CODE, alter column ENTITY_TYPE_CODE type CODE;
alter table SAMPLE_TYPES alter column CODE type CODE, alter column generated_code_prefix type CODE;
alter table SAMPLES_ALL alter column PERM_ID type CODE, alter column CODE type CODE;
alter table SEMANTIC_ANNOTATIONS alter column PERM_ID type CODE;
alter table SPACES alter column CODE type CODE;

-- Recreate views
CREATE VIEW data AS
     SELECT id, code, dsty_id, dast_id, expe_id, data_producer_code, production_timestamp, samp_id, registration_timestamp, access_timestamp, pers_id_registerer, pers_id_modifier, is_valid, modification_timestamp, is_derived, del_id, orig_del, version, data_set_kind 
       FROM data_all 
      WHERE del_id IS NULL;

CREATE VIEW data_deleted AS
     SELECT id, code, dsty_id, dast_id, expe_id, data_producer_code, production_timestamp, samp_id, registration_timestamp, access_timestamp, pers_id_registerer, pers_id_modifier, is_valid, modification_timestamp, is_derived, del_id, orig_del, version, data_set_kind 
       FROM data_all 
      WHERE del_id IS NOT NULL;

CREATE VIEW experiments AS
     SELECT id, perm_id, code, exty_id, pers_id_registerer, pers_id_modifier, registration_timestamp, modification_timestamp, proj_id, del_id, orig_del, is_public, version 
       FROM experiments_all 
      WHERE del_id IS NULL;

CREATE VIEW experiments_deleted AS
     SELECT id, perm_id, code, exty_id, pers_id_registerer, pers_id_modifier, registration_timestamp, modification_timestamp, proj_id, del_id, orig_del, is_public, version 
       FROM experiments_all 
      WHERE del_id IS NOT NULL;

CREATE VIEW samples AS
     SELECT id, perm_id, code, proj_id, expe_id, saty_id, registration_timestamp, modification_timestamp, pers_id_registerer, pers_id_modifier, del_id, orig_del, space_id, samp_id_part_of, version 
       FROM samples_all 
      WHERE del_id IS NULL;

CREATE VIEW samples_deleted AS
     SELECT id, perm_id, code, expe_id, saty_id, registration_timestamp, modification_timestamp, pers_id_registerer, pers_id_modifier, del_id, orig_del, space_id, proj_id, samp_id_part_of, version 
       FROM samples_all 
      WHERE del_id IS NOT NULL;

-- Recreate rules
CREATE OR REPLACE RULE material_properties_update AS
    ON UPDATE TO material_properties 
    WHERE (OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd' AND OLD.VALUE != NEW.VALUE) 
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
         NEW.MODIFICATION_TIMESTAMP
       );

CREATE OR REPLACE RULE material_properties_delete AS
    ON DELETE TO material_properties 
    WHERE (OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd')
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

CREATE OR REPLACE RULE experiment_properties_update AS
    ON UPDATE TO experiment_properties 
    WHERE (OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd' AND OLD.VALUE != NEW.VALUE) 
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
         NEW.MODIFICATION_TIMESTAMP
       );
       
CREATE OR REPLACE RULE experiment_properties_delete AS
    ON DELETE TO experiment_properties 
    WHERE (OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd')
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

CREATE OR REPLACE RULE sample_properties_update AS
    ON UPDATE TO sample_properties
    WHERE (OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd' AND OLD.VALUE != NEW.VALUE) 
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
         NEW.MODIFICATION_TIMESTAMP
       );
              
CREATE OR REPLACE RULE sample_properties_delete AS
    ON DELETE TO sample_properties 
    WHERE ((OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd')
        OR OLD.CVTE_ID IS NOT NULL 
        OR OLD.MATE_PROP_ID IS NOT NULL)
     AND (SELECT DEL_ID FROM SAMPLES_ALL WHERE ID = OLD.SAMP_ID) IS NULL
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

CREATE OR REPLACE RULE data_set_properties_update AS
    ON UPDATE TO data_set_properties 
    WHERE (OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd' AND OLD.VALUE != NEW.VALUE) 
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
         nextval('DATA_SET_PROPERTY_ID_SEQ'), 
         OLD.DS_ID, 
         OLD.DSTPT_ID, 
         OLD.VALUE, 
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         NEW.MODIFICATION_TIMESTAMP
       );

CREATE OR REPLACE RULE data_set_properties_delete AS
    ON DELETE TO data_set_properties 
    WHERE ((OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd')
        OR OLD.CVTE_ID IS NOT NULL 
        OR OLD.MATE_PROP_ID IS NOT NULL)
     AND (SELECT DEL_ID FROM DATA_ALL WHERE ID = OLD.DS_ID) IS NULL
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
         nextval('DATA_SET_PROPERTY_ID_SEQ'), 
         OLD.DS_ID, 
         OLD.DSTPT_ID, 
         OLD.VALUE, 
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         current_timestamp
       );
       
CREATE OR REPLACE RULE sample_experiment_update AS
    ON UPDATE TO samples_all 
    WHERE (OLD.EXPE_ID != NEW.EXPE_ID OR OLD.EXPE_ID IS NULL) AND NEW.EXPE_ID IS NOT NULL
    DO ALSO (
       UPDATE EXPERIMENT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_EXPE_ID = OLD.EXPE_ID AND SAMP_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       INSERT INTO EXPERIMENT_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_EXPE_ID,
         RELATION_TYPE, 
         SAMP_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('EXPERIMENT_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.EXPE_ID, 
         'OWNER', 
         NEW.ID, 
         NEW.PERM_ID,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE MAIN_SAMP_ID = OLD.ID AND EXPE_ID = OLD.EXPE_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         EXPE_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.EXPE_ID, 
         (SELECT PERM_ID FROM EXPERIMENTS_ALL WHERE ID = NEW.EXPE_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
    );
    
CREATE OR REPLACE RULE sample_experiment_remove_update AS
    ON UPDATE TO samples_all 
    WHERE OLD.EXPE_ID IS NOT NULL AND NEW.EXPE_ID IS NULL
    DO ALSO (
       UPDATE EXPERIMENT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_EXPE_ID = OLD.EXPE_ID AND SAMP_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE MAIN_SAMP_ID = OLD.ID AND EXPE_ID = OLD.EXPE_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
    );
    
CREATE OR REPLACE RULE sample_experiment_insert AS
    ON INSERT TO samples_all 
    WHERE NEW.EXPE_ID IS NOT NULL
       DO ALSO (
       INSERT INTO EXPERIMENT_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_EXPE_ID,
         RELATION_TYPE, 
         SAMP_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('EXPERIMENT_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.EXPE_ID, 
         'OWNER', 
         NEW.ID, 
         NEW.PERM_ID,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         EXPE_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.EXPE_ID, 
         (SELECT PERM_ID FROM EXPERIMENTS_ALL WHERE ID = NEW.EXPE_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
   );
   
CREATE OR REPLACE RULE sample_experiment_delete AS
    ON DELETE TO samples_all 
    WHERE OLD.EXPE_ID IS NOT NULL
       DO ALSO 
       UPDATE EXPERIMENT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = current_timestamp 
         WHERE MAIN_EXPE_ID = OLD.EXPE_ID AND SAMP_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;
     
CREATE OR REPLACE RULE sample_container_update AS
    ON UPDATE TO samples_all 
    WHERE (OLD.SAMP_ID_PART_OF != NEW.SAMP_ID_PART_OF OR OLD.SAMP_ID_PART_OF IS NULL) AND NEW.SAMP_ID_PART_OF IS NOT NULL
    DO ALSO (
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE (MAIN_SAMP_ID = OLD.SAMP_ID_PART_OF AND SAMP_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL AND RELATION_TYPE = 'CONTAINER')
           OR (MAIN_SAMP_ID = OLD.ID AND SAMP_ID = OLD.SAMP_ID_PART_OF AND VALID_UNTIL_TIMESTAMP IS NULL AND RELATION_TYPE = 'CONTAINED');
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         SAMP_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.SAMP_ID_PART_OF, 
         'CONTAINER', 
         NEW.ID, 
         NEW.PERM_ID,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         SAMP_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'CONTAINED', 
         NEW.SAMP_ID_PART_OF, 
         (SELECT PERM_ID FROM SAMPLES_ALL WHERE ID = NEW.SAMP_ID_PART_OF),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
    );
    
CREATE OR REPLACE RULE sample_container_remove_update AS
    ON UPDATE TO samples_all 
    WHERE OLD.SAMP_ID_PART_OF IS NOT NULL AND NEW.SAMP_ID_PART_OF IS NULL
    DO ALSO (
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE (MAIN_SAMP_ID = OLD.SAMP_ID_PART_OF AND SAMP_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL AND RELATION_TYPE = 'CONTAINER')
           OR (MAIN_SAMP_ID = OLD.ID AND SAMP_ID = OLD.SAMP_ID_PART_OF AND VALID_UNTIL_TIMESTAMP IS NULL AND RELATION_TYPE = 'CONTAINED');
    );
    
CREATE OR REPLACE RULE sample_container_insert AS
    ON INSERT TO samples_all 
    WHERE NEW.SAMP_ID_PART_OF IS NOT NULL
       DO ALSO (
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         SAMP_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.SAMP_ID_PART_OF, 
         'CONTAINER', 
         NEW.ID, 
         NEW.PERM_ID,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         SAMP_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'CONTAINED', 
         NEW.SAMP_ID_PART_OF, 
         (SELECT PERM_ID FROM SAMPLES_ALL WHERE ID = NEW.SAMP_ID_PART_OF),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
   );
   
CREATE OR REPLACE RULE sample_container_delete AS
    ON DELETE TO samples_all 
    WHERE OLD.SAMP_ID_PART_OF IS NOT NULL
       DO ALSO 
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = current_timestamp 
         WHERE MAIN_SAMP_ID = OLD.SAMP_ID_PART_OF AND SAMP_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL AND RELATION_TYPE = 'CONTAINER';

CREATE OR REPLACE RULE dataset_experiment_update AS
    ON UPDATE TO data_all 
    WHERE (OLD.EXPE_ID != NEW.EXPE_ID OR OLD.SAMP_ID IS NOT NULL) AND NEW.SAMP_ID IS NULL
    DO ALSO (
       UPDATE EXPERIMENT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_EXPE_ID = OLD.EXPE_ID AND DATA_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       INSERT INTO EXPERIMENT_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_EXPE_ID,
         RELATION_TYPE, 
         DATA_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('EXPERIMENT_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.EXPE_ID, 
         'OWNER', 
         NEW.ID, 
         NEW.CODE,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       UPDATE DATA_SET_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE (MAIN_DATA_ID = OLD.ID AND EXPE_ID = OLD.EXPE_ID AND VALID_UNTIL_TIMESTAMP IS NULL);
       INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_DATA_ID,
         RELATION_TYPE, 
         EXPE_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.EXPE_ID, 
         (SELECT PERM_ID FROM EXPERIMENTS_ALL WHERE ID = NEW.EXPE_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
    );
    
CREATE OR REPLACE RULE dataset_experiment_remove_update AS
    ON UPDATE TO data_all 
    WHERE OLD.SAMP_ID IS NULL AND NEW.SAMP_ID IS NOT NULL
    DO ALSO (
       UPDATE EXPERIMENT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_EXPE_ID = OLD.EXPE_ID AND DATA_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       UPDATE DATA_SET_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE MAIN_DATA_ID = OLD.ID AND EXPE_ID = OLD.EXPE_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
    );
    
CREATE OR REPLACE RULE dataset_experiment_insert AS
    ON INSERT TO data_all 
    WHERE NEW.EXPE_ID IS NOT NULL AND NEW.SAMP_ID IS NULL
       DO ALSO (
       INSERT INTO EXPERIMENT_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_EXPE_ID,
         RELATION_TYPE, 
         DATA_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('EXPERIMENT_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.EXPE_ID, 
         'OWNER', 
         NEW.ID, 
         NEW.CODE,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_DATA_ID,
         RELATION_TYPE, 
         EXPE_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.EXPE_ID, 
         (SELECT PERM_ID FROM EXPERIMENTS_ALL WHERE ID = NEW.EXPE_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
   );
   
CREATE OR REPLACE RULE dataset_experiment_delete AS
    ON DELETE TO data_all 
    WHERE OLD.EXPE_ID IS NOT NULL AND OLD.SAMP_ID IS NULL
       DO ALSO 
       UPDATE EXPERIMENT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = current_timestamp 
         WHERE MAIN_EXPE_ID = OLD.EXPE_ID AND DATA_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;

CREATE OR REPLACE RULE dataset_sample_update AS
    ON UPDATE TO data_all 
    WHERE (OLD.SAMP_ID != NEW.SAMP_ID OR OLD.SAMP_ID IS NULL) AND NEW.SAMP_ID IS NOT NULL
    DO ALSO (
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_SAMP_ID = OLD.SAMP_ID AND DATA_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         DATA_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.SAMP_ID, 
         'OWNER', 
         NEW.ID, 
         NEW.CODE,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       UPDATE DATA_SET_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE (MAIN_DATA_ID = OLD.ID AND SAMP_ID = OLD.SAMP_ID AND VALID_UNTIL_TIMESTAMP IS NULL);
       INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_DATA_ID,
         RELATION_TYPE, 
         SAMP_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.SAMP_ID, 
         (SELECT PERM_ID FROM SAMPLES_ALL WHERE ID = NEW.SAMP_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
    );
    
CREATE OR REPLACE RULE dataset_sample_remove_update AS
    ON UPDATE TO data_all 
    WHERE OLD.SAMP_ID IS NOT NULL AND NEW.SAMP_ID IS NULL
    DO ALSO (
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_SAMP_ID = OLD.SAMP_ID AND DATA_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       UPDATE DATA_SET_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE MAIN_DATA_ID = OLD.ID AND SAMP_ID = OLD.SAMP_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
    );
    
CREATE OR REPLACE RULE dataset_sample_insert AS
    ON INSERT TO data_all 
    WHERE NEW.SAMP_ID IS NOT NULL
       DO ALSO (
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         DATA_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.SAMP_ID, 
         'OWNER', 
         NEW.ID, 
         NEW.CODE,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_DATA_ID,
         RELATION_TYPE, 
         SAMP_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.SAMP_ID, 
         (SELECT PERM_ID FROM SAMPLES_ALL WHERE ID = NEW.SAMP_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
   );
   
CREATE OR REPLACE RULE dataset_sample_delete AS
    ON DELETE TO data_all 
    WHERE OLD.SAMP_ID IS NOT NULL
       DO ALSO 
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = current_timestamp 
         WHERE MAIN_SAMP_ID = OLD.SAMP_ID AND DATA_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;

CREATE OR REPLACE RULE data_relationship_insert AS
    ON INSERT TO data_set_relationships_all
    WHERE NEW.DEL_ID IS NULL
       DO ALSO (
         INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
           ID, 
           MAIN_DATA_ID,
           RELATION_TYPE, 
           DATA_ID, 
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP,
           ORDINAL
         ) VALUES (
           nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.DATA_ID_PARENT, 
           (SELECT UPPER(PARENT_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = NEW.RELATIONSHIP_ID), 
           NEW.DATA_ID_CHILD, 
           (SELECT CODE FROM data_all WHERE ID = NEW.DATA_ID_CHILD),
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP,
           NEW.ORDINAL
         );
         INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
           ID, 
           MAIN_DATA_ID,
           RELATION_TYPE, 
           DATA_ID, 
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP,
           ORDINAL
         ) VALUES (
           nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.DATA_ID_CHILD, 
           (SELECT UPPER(CHILD_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = NEW.RELATIONSHIP_ID), 
           NEW.DATA_ID_PARENT, 
           (SELECT CODE FROM data_all WHERE ID = NEW.DATA_ID_PARENT),
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP,
           NEW.ORDINAL
         );
       );

CREATE OR REPLACE RULE data_relationship_delete AS
    ON DELETE TO data_set_relationships_all
    WHERE OLD.DEL_ID IS NULL
       DO ALSO (
         UPDATE DATA_SET_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = current_timestamp
           WHERE (MAIN_DATA_ID = OLD.DATA_ID_PARENT 
                  AND DATA_ID = OLD.DATA_ID_CHILD
                  AND RELATION_TYPE = (SELECT UPPER(PARENT_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = OLD.RELATIONSHIP_ID)
                  AND VALID_UNTIL_TIMESTAMP IS NULL)
             OR (MAIN_DATA_ID = OLD.DATA_ID_CHILD 
                 AND DATA_ID = OLD.DATA_ID_PARENT 
                 AND RELATION_TYPE = (SELECT UPPER(CHILD_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = OLD.RELATIONSHIP_ID)
                 AND VALID_UNTIL_TIMESTAMP IS NULL);
       );
       
CREATE OR REPLACE RULE data_relationship_update AS
    ON UPDATE TO data_set_relationships_all
    WHERE NEW.DEL_ID IS NULL AND OLD.DEL_ID IS NULL
       DO ALSO (
         UPDATE DATA_SET_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = current_timestamp
           WHERE (MAIN_DATA_ID = OLD.DATA_ID_PARENT 
                  AND DATA_ID = OLD.DATA_ID_CHILD
                  AND RELATION_TYPE = (SELECT UPPER(PARENT_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = OLD.RELATIONSHIP_ID)
                  AND VALID_UNTIL_TIMESTAMP IS NULL)
             OR (MAIN_DATA_ID = OLD.DATA_ID_CHILD 
                 AND DATA_ID = OLD.DATA_ID_PARENT 
                 AND RELATION_TYPE = (SELECT UPPER(CHILD_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = OLD.RELATIONSHIP_ID)
                 AND VALID_UNTIL_TIMESTAMP IS NULL);
         INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
           ID, 
           MAIN_DATA_ID,
           RELATION_TYPE, 
           DATA_ID, 
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP,
           ORDINAL
         ) VALUES (
           nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.DATA_ID_PARENT, 
           (SELECT UPPER(PARENT_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = NEW.RELATIONSHIP_ID), 
           NEW.DATA_ID_CHILD, 
           (SELECT CODE FROM data_all WHERE ID = NEW.DATA_ID_CHILD),
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP,
           NEW.ORDINAL
         );
         INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
           ID, 
           MAIN_DATA_ID,
           RELATION_TYPE, 
           DATA_ID, 
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP,
           ORDINAL
         ) VALUES (
           nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.DATA_ID_CHILD, 
           (SELECT UPPER(CHILD_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = NEW.RELATIONSHIP_ID), 
           NEW.DATA_ID_PARENT, 
           (SELECT CODE FROM data_all WHERE ID = NEW.DATA_ID_PARENT),
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP,
           NEW.ORDINAL
         );
       );
       
CREATE OR REPLACE RULE data_relationship_trash_update AS
    ON UPDATE TO data_set_relationships_all
    WHERE NEW.DEL_ID IS NOT NULL AND OLD.DEL_ID IS NULL
       DO ALSO (
         UPDATE DATA_SET_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = current_timestamp
           WHERE (MAIN_DATA_ID = OLD.DATA_ID_PARENT 
                  AND DATA_ID = OLD.DATA_ID_CHILD
                  AND RELATION_TYPE = (SELECT UPPER(PARENT_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = OLD.RELATIONSHIP_ID)
                  AND VALID_UNTIL_TIMESTAMP IS NULL)
             OR (MAIN_DATA_ID = OLD.DATA_ID_CHILD 
                 AND DATA_ID = OLD.DATA_ID_PARENT 
                 AND RELATION_TYPE = (SELECT UPPER(CHILD_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = OLD.RELATIONSHIP_ID)
                 AND VALID_UNTIL_TIMESTAMP IS NULL);
       );
       
CREATE OR REPLACE RULE data_relationship_trash_revert_update AS
    ON UPDATE TO data_set_relationships_all
    WHERE OLD.DEL_ID IS NOT NULL AND NEW.DEL_ID IS NULL
       DO ALSO (
         INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
           ID, 
           MAIN_DATA_ID,
           RELATION_TYPE, 
           DATA_ID, 
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP,
           ORDINAL
         ) VALUES (
           nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.DATA_ID_PARENT, 
           (SELECT UPPER(PARENT_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = NEW.RELATIONSHIP_ID), 
           NEW.DATA_ID_CHILD, 
           (SELECT CODE FROM data_all WHERE ID = NEW.DATA_ID_CHILD),
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP,
           NEW.ORDINAL
         );
         INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
           ID, 
           MAIN_DATA_ID,
           RELATION_TYPE, 
           DATA_ID, 
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP,
           ORDINAL
         ) VALUES (
           nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.DATA_ID_CHILD, 
           (SELECT UPPER(CHILD_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = NEW.RELATIONSHIP_ID), 
           NEW.DATA_ID_PARENT, 
           (SELECT CODE FROM data_all WHERE ID = NEW.DATA_ID_PARENT),
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP,
           NEW.ORDINAL
         );
       );

CREATE OR REPLACE RULE sample_parent_child_insert AS
    ON INSERT TO sample_relationships_all
    WHERE NEW.DEL_ID IS NULL
       DO ALSO (
         INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
           ID, 
           MAIN_SAMP_ID,
           RELATION_TYPE, 
           SAMP_ID, 
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP
         ) VALUES (
           nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.SAMPLE_ID_PARENT, 
           'PARENT', 
           NEW.SAMPLE_ID_CHILD, 
           (SELECT PERM_ID FROM samples_all WHERE ID = NEW.SAMPLE_ID_CHILD),
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP
         );
         INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
           ID, 
           MAIN_SAMP_ID,
           RELATION_TYPE, 
           SAMP_ID, 
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP
         ) VALUES (
           nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.SAMPLE_ID_CHILD, 
           'CHILD', 
           NEW.SAMPLE_ID_PARENT, 
           (SELECT PERM_ID FROM samples_all WHERE ID = NEW.SAMPLE_ID_PARENT),
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP
         );
       );
       
CREATE OR REPLACE RULE sample_parent_child_delete AS
    ON DELETE TO sample_relationships_all
    WHERE OLD.DEL_ID IS NULL
       DO ALSO (
         UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = current_timestamp
           WHERE (MAIN_SAMP_ID = OLD.SAMPLE_ID_PARENT AND SAMP_ID = OLD.SAMPLE_ID_CHILD AND VALID_UNTIL_TIMESTAMP IS NULL)
             OR (MAIN_SAMP_ID = OLD.SAMPLE_ID_CHILD AND SAMP_ID = OLD.SAMPLE_ID_PARENT AND VALID_UNTIL_TIMESTAMP IS NULL);
       );

CREATE OR REPLACE RULE sample_parent_child_update AS
    ON UPDATE TO sample_relationships_all
    WHERE NEW.DEL_ID IS NOT NULL AND OLD.DEL_ID IS NULL
       DO ALSO (
         UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = current_timestamp
           WHERE (MAIN_SAMP_ID = OLD.SAMPLE_ID_PARENT AND SAMP_ID = OLD.SAMPLE_ID_CHILD AND VALID_UNTIL_TIMESTAMP IS NULL)
             OR (MAIN_SAMP_ID = OLD.SAMPLE_ID_CHILD AND SAMP_ID = OLD.SAMPLE_ID_PARENT AND VALID_UNTIL_TIMESTAMP IS NULL);
       );

CREATE OR REPLACE RULE sample_parent_child_revert_update AS
    ON UPDATE TO sample_relationships_all
    WHERE NEW.DEL_ID IS NULL AND OLD.DEL_ID IS NOT NULL
       DO ALSO (
         INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
           ID, 
           MAIN_SAMP_ID,
           RELATION_TYPE, 
           SAMP_ID, 
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP
         ) VALUES (
           nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.SAMPLE_ID_PARENT, 
           'PARENT', 
           NEW.SAMPLE_ID_CHILD, 
           (SELECT PERM_ID FROM samples_all WHERE ID = NEW.SAMPLE_ID_CHILD),
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP
         );
         INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
           ID, 
           MAIN_SAMP_ID,
           RELATION_TYPE, 
           SAMP_ID, 
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP
         ) VALUES (
           nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.SAMPLE_ID_CHILD, 
           'CHILD', 
           NEW.SAMPLE_ID_PARENT, 
           (SELECT PERM_ID FROM samples_all WHERE ID = NEW.SAMPLE_ID_PARENT),
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP
         );
       );

CREATE OR REPLACE RULE experiment_project_update AS
    ON UPDATE TO experiments_all 
    WHERE (OLD.PROJ_ID != NEW.PROJ_ID OR OLD.PROJ_ID IS NULL) AND NEW.PROJ_ID IS NOT NULL
    DO ALSO (
       UPDATE PROJECT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_PROJ_ID = OLD.PROJ_ID AND EXPE_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       INSERT INTO PROJECT_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_PROJ_ID,
         RELATION_TYPE, 
         EXPE_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('PROJECT_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.PROJ_ID, 
         'OWNER', 
         NEW.ID, 
         NEW.PERM_ID,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       UPDATE EXPERIMENT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE MAIN_EXPE_ID = OLD.ID AND PROJ_ID = OLD.PROJ_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       INSERT INTO EXPERIMENT_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_EXPE_ID,
         RELATION_TYPE, 
         PROJ_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('EXPERIMENT_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.PROJ_ID, 
         (SELECT perm_id FROM PROJECTS WHERE ID = NEW.PROJ_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
    );
    
CREATE OR REPLACE RULE experiment_project_remove_update AS
    ON UPDATE TO experiments_all 
    WHERE OLD.PROJ_ID IS NOT NULL AND NEW.PROJ_ID IS NULL
    DO ALSO (
       UPDATE PROJECT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_PROJ_ID = OLD.PROJ_ID AND EXPE_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       UPDATE EXPERIMENT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE MAIN_EXPE_ID = OLD.ID AND PROJ_ID = OLD.PROJ_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
    );
    
CREATE OR REPLACE RULE experiment_project_insert AS
    ON INSERT TO experiments_all 
    WHERE NEW.PROJ_ID IS NOT NULL
       DO ALSO (
       INSERT INTO PROJECT_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_PROJ_ID,
         RELATION_TYPE, 
         EXPE_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('PROJECT_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.PROJ_ID, 
         'OWNER', 
         NEW.ID, 
         NEW.PERM_ID,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       INSERT INTO EXPERIMENT_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_EXPE_ID,
         RELATION_TYPE, 
         PROJ_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('EXPERIMENT_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.PROJ_ID, 
         (SELECT perm_id FROM PROJECTS WHERE ID = NEW.PROJ_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
   );
   
CREATE OR REPLACE RULE experiment_project_delete AS
    ON DELETE TO experiments_all 
    WHERE OLD.PROJ_ID IS NOT NULL
       DO ALSO 
       UPDATE PROJECT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = current_timestamp 
         WHERE MAIN_PROJ_ID = OLD.PROJ_ID AND EXPE_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;

CREATE OR REPLACE RULE project_space_update AS
    ON UPDATE TO projects 
    WHERE (OLD.SPACE_ID != NEW.SPACE_ID OR OLD.SPACE_ID IS NULL) AND NEW.SPACE_ID IS NOT NULL
    DO ALSO (
       UPDATE PROJECT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_PROJ_ID = OLD.ID AND SPACE_ID = OLD.SPACE_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       INSERT INTO PROJECT_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_PROJ_ID,
         RELATION_TYPE, 
         SPACE_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('PROJECT_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.SPACE_ID, 
         (SELECT CODE FROM SPACES WHERE ID = NEW.SPACE_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
    );
    
CREATE OR REPLACE RULE project_space_remove_update AS
    ON UPDATE TO projects 
    WHERE OLD.SPACE_ID IS NOT NULL AND NEW.SPACE_ID IS NULL
    DO ALSO (
       UPDATE PROJECT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_PROJ_ID = OLD.ID AND SPACE_ID = OLD.SPACE_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
    );
    
CREATE OR REPLACE RULE project_space_insert AS
    ON INSERT TO projects 
    WHERE NEW.SPACE_ID IS NOT NULL
       DO ALSO (
       INSERT INTO PROJECT_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_PROJ_ID,
         RELATION_TYPE, 
         SPACE_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('PROJECT_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.SPACE_ID, 
         (SELECT CODE FROM SPACES WHERE ID = NEW.SPACE_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
   );

CREATE OR REPLACE RULE sample_project_update AS
    ON UPDATE TO samples_all 
    WHERE (OLD.PROJ_ID != NEW.PROJ_ID OR OLD.PROJ_ID IS NULL OR OLD.EXPE_ID IS NOT NULL) AND NEW.PROJ_ID IS NOT NULL AND NEW.EXPE_ID IS NULL
    DO ALSO (
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE MAIN_SAMP_ID = OLD.ID AND PROJ_ID = OLD.PROJ_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         PROJ_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.PROJ_ID, 
         (SELECT PERM_ID FROM PROJECTS WHERE ID = NEW.PROJ_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
    );

CREATE OR REPLACE RULE sample_project_remove_update AS
    ON UPDATE TO samples_all 
    WHERE OLD.PROJ_ID IS NOT NULL AND (NEW.PROJ_ID IS NULL OR (OLD.EXPE_ID IS NULL AND NEW.EXPE_ID IS NOT NULL))
    DO ALSO (
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE MAIN_SAMP_ID = OLD.ID AND PROJ_ID = OLD.PROJ_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
    );

CREATE OR REPLACE RULE sample_project_insert AS
    ON INSERT TO samples_all 
    WHERE NEW.EXPE_ID IS NULL AND NEW.PROJ_ID IS NOT NULL
    DO ALSO (
      INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         PROJ_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.PROJ_ID, 
         (SELECT PERM_ID FROM PROJECTS WHERE ID = NEW.PROJ_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
   );

CREATE OR REPLACE RULE sample_space_update AS
    ON UPDATE TO samples_all 
    WHERE (OLD.SPACE_ID != NEW.SPACE_ID OR OLD.SPACE_ID IS NULL OR OLD.EXPE_ID IS NOT NULL OR OLD.PROJ_ID IS NOT NULL) AND NEW.SPACE_ID IS NOT NULL AND NEW.EXPE_ID IS NULL AND NEW.PROJ_ID IS NULL
    DO ALSO (
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE MAIN_SAMP_ID = OLD.ID AND SPACE_ID = OLD.SPACE_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         SPACE_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.SPACE_ID, 
         (SELECT CODE FROM SPACES WHERE ID = NEW.SPACE_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
    );

CREATE OR REPLACE RULE sample_space_remove_update AS
    ON UPDATE TO samples_all 
    WHERE OLD.SPACE_ID IS NOT NULL AND (NEW.SPACE_ID IS NULL OR (OLD.EXPE_ID IS NULL AND NEW.EXPE_ID IS NOT NULL) OR (OLD.PROJ_ID IS NULL AND NEW.PROJ_ID IS NOT NULL))
    DO ALSO (
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE MAIN_SAMP_ID = OLD.ID AND SPACE_ID = OLD.SPACE_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
    );

CREATE OR REPLACE RULE sample_space_insert AS
    ON INSERT TO samples_all 
    WHERE NEW.EXPE_ID IS NULL AND NEW.SPACE_ID IS NOT NULL AND NEW.PROJ_ID IS NULL
    DO ALSO (
      INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         SPACE_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.SPACE_ID, 
         (SELECT CODE FROM SPACES WHERE ID = NEW.SPACE_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
   );

CREATE OR REPLACE RULE sample_insert AS
    ON INSERT TO samples DO INSTEAD 
       INSERT INTO samples_all (
         id, 
         code, 
         del_id,
         orig_del,
         expe_id,
         proj_id,
         modification_timestamp,
         perm_id,
         pers_id_registerer, 
         pers_id_modifier, 
         registration_timestamp, 
         samp_id_part_of,
         saty_id, 
         space_id,
         version
       ) VALUES (
         NEW.id, 
         NEW.code, 
         NEW.del_id,
         NEW.orig_del,
         NEW.expe_id,
         NEW.proj_id,
         NEW.modification_timestamp,
         NEW.perm_id,
         NEW.pers_id_registerer, 
         NEW.pers_id_modifier, 
         NEW.registration_timestamp, 
         NEW.samp_id_part_of,
         NEW.saty_id, 
         NEW.space_id,
         NEW.version
       );
     
CREATE OR REPLACE RULE sample_update AS
    ON UPDATE TO samples DO INSTEAD 
       UPDATE samples_all
          SET code = NEW.code,
              del_id = NEW.del_id,
              orig_del = NEW.orig_del,
              expe_id = NEW.expe_id,
              proj_id = NEW.proj_id,
              modification_timestamp = NEW.modification_timestamp,
              perm_id = NEW.perm_id,
              pers_id_registerer = NEW.pers_id_registerer,
              pers_id_modifier = NEW.pers_id_modifier,
              registration_timestamp = NEW.registration_timestamp,
              samp_id_part_of = NEW.samp_id_part_of,
              saty_id = NEW.saty_id,
              space_id = NEW.space_id,
              version = NEW.version
          WHERE id = NEW.id;
     
CREATE OR REPLACE RULE sample_delete AS
    ON DELETE TO samples DO INSTEAD
       DELETE FROM samples_all
              WHERE id = OLD.id;
              
CREATE OR REPLACE RULE sample_deleted_update AS
    ON UPDATE TO samples_deleted DO INSTEAD
       UPDATE samples_all
          SET del_id = NEW.del_id,
              orig_del = NEW.orig_del,
              modification_timestamp = NEW.modification_timestamp,
              version = NEW.version
          WHERE id = NEW.id;
     
CREATE OR REPLACE RULE sample_deleted_delete AS
    ON DELETE TO samples_deleted DO INSTEAD
       DELETE FROM samples_all
              WHERE id = OLD.id;

CREATE OR REPLACE RULE experiment_insert AS
  ON INSERT TO experiments DO INSTEAD 
     INSERT INTO experiments_all (
       id, 
       code, 
       del_id,
       orig_del,
       exty_id, 
       is_public,
       modification_timestamp,
       perm_id,
       pers_id_registerer, 
       pers_id_modifier, 
       proj_id,
       registration_timestamp,
       version
     ) VALUES (
       NEW.id, 
       NEW.code, 
       NEW.del_id,
       NEW.orig_del,
       NEW.exty_id, 
       NEW.is_public,
       NEW.modification_timestamp,
       NEW.perm_id,
       NEW.pers_id_registerer, 
       NEW.pers_id_modifier, 
       NEW.proj_id,
       NEW.registration_timestamp,
       NEW.version
     );
     
CREATE OR REPLACE RULE experiment_update AS
    ON UPDATE TO experiments DO INSTEAD 
       UPDATE experiments_all
          SET code = NEW.code,
              del_id = NEW.del_id,
              orig_del = NEW.orig_del,
              exty_id = NEW.exty_id,
              is_public = NEW.is_public,
              modification_timestamp = NEW.modification_timestamp,
              perm_id = NEW.perm_id,
              pers_id_registerer = NEW.pers_id_registerer,
              pers_id_modifier = NEW.pers_id_modifier,
              proj_id = NEW.proj_id,
              registration_timestamp = NEW.registration_timestamp,
              version = NEW.version
          WHERE id = NEW.id;
     
CREATE OR REPLACE RULE experiment_delete AS
    ON DELETE TO experiments DO INSTEAD
       DELETE FROM experiments_all
              WHERE id = OLD.id;
      
CREATE OR REPLACE RULE experiments_deleted_update AS
    ON UPDATE TO experiments_deleted DO INSTEAD 
       UPDATE experiments_all
          SET del_id = NEW.del_id,
              orig_del = NEW.orig_del,
              modification_timestamp = NEW.modification_timestamp,
              version = NEW.version
          WHERE id = NEW.id;
     
CREATE OR REPLACE RULE experiments_deleted_delete AS
    ON DELETE TO experiments_deleted DO INSTEAD
       DELETE FROM experiments_all
              WHERE id = OLD.id;
      
CREATE OR REPLACE RULE data_insert AS
  ON INSERT TO data DO INSTEAD 
     INSERT INTO data_all (
       id, 
       code, 
       del_id,
       orig_del,
       expe_id,
       dast_id,
       data_producer_code,
       dsty_id,
       is_derived,
       is_valid,
       modification_timestamp,
       access_timestamp,
       pers_id_registerer,
       pers_id_modifier,
       production_timestamp,
       registration_timestamp,
       samp_id,
       version,
       data_set_kind
     ) VALUES (
       NEW.id, 
       NEW.code, 
       NEW.del_id, 
       NEW.orig_del,
       NEW.expe_id,
       NEW.dast_id,
       NEW.data_producer_code,
       NEW.dsty_id,
       NEW.is_derived, 
       NEW.is_valid,
       NEW.modification_timestamp,
       NEW.access_timestamp,
       NEW.pers_id_registerer,
       NEW.pers_id_modifier,
       NEW.production_timestamp,
       NEW.registration_timestamp,
       NEW.samp_id,
       NEW.version,
       NEW.data_set_kind
     );
     
CREATE OR REPLACE RULE data_update AS
    ON UPDATE TO data DO INSTEAD 
       UPDATE data_all
          SET code = NEW.code,
              del_id = NEW.del_id,
              orig_del = NEW.orig_del,
              expe_id = NEW.expe_id,
              dast_id = NEW.dast_id,
              data_producer_code = NEW.data_producer_code,
              dsty_id = NEW.dsty_id,
              is_derived = NEW.is_derived,
              is_valid = NEW.is_valid,
              modification_timestamp = NEW.modification_timestamp,
              access_timestamp = NEW.access_timestamp,
              pers_id_registerer = NEW.pers_id_registerer,
              pers_id_modifier = NEW.pers_id_modifier,
              production_timestamp = NEW.production_timestamp,
              registration_timestamp = NEW.registration_timestamp,
              samp_id = NEW.samp_id,
              version = NEW.version,
              data_set_kind = NEW.data_set_kind
       WHERE id = NEW.id;
              
CREATE OR REPLACE RULE data_all AS
    ON DELETE TO data DO INSTEAD
       DELETE FROM data_all
              WHERE id = OLD.id;
              
CREATE OR REPLACE RULE data_deleted_update AS
    ON UPDATE TO data_deleted DO INSTEAD 
       UPDATE data_all
          SET del_id = NEW.del_id,
              orig_del = NEW.orig_del,
              modification_timestamp = NEW.modification_timestamp,
              version = NEW.version
          WHERE id = NEW.id;
     
CREATE OR REPLACE RULE data_deleted_delete AS
    ON DELETE TO data_deleted DO INSTEAD
       DELETE FROM data_all
              WHERE id = OLD.id;

-- recreating functions using local variables of type CODE. Otherwise one might get a "cache lookup failed for type" error 

CREATE OR REPLACE FUNCTION CONTROLLED_VOCABULARY_CHECK() RETURNS trigger AS $$
DECLARE
   v_code  CODE;
BEGIN

   select code into v_code from data_types where id = NEW.daty_id;

   -- Check if the data is of type "CONTROLLEDVOCABULARY"
   if v_code = 'CONTROLLEDVOCABULARY' then
      if NEW.covo_id IS NULL then
         RAISE EXCEPTION 'Insert/Update of Property Type (Code: %) failed, as its Data Type is CONTROLLEDVOCABULARY, but it is not linked to a Controlled Vocabulary.', NEW.code;
      end if;
   end if;

   RETURN NEW;

END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION EXTERNAL_DATA_STORAGE_FORMAT_CHECK() RETURNS trigger AS $$
DECLARE
   v_covo_code  CODE;
   data_code CODE;
BEGIN

   select code into v_covo_code from controlled_vocabularies
      where is_internal_namespace = true and 
         id = (select covo_id from controlled_vocabulary_terms where id = NEW.cvte_id_stor_fmt);
   -- Check if the data storage format is a term of the controlled vocabulary "STORAGE_FORMAT"
   if v_covo_code != 'STORAGE_FORMAT' then
      select code into data_code from data_all where id = NEW.data_id; 
      RAISE EXCEPTION 'Insert/Update of Data (Code: %) failed, as its Storage Format is %, but is required to be STORAGE_FORMAT.', data_code, v_covo_code;
   end if;

   RETURN NEW;

END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION data_exp_or_sample_link_check() RETURNS trigger AS $$
DECLARE
  space_id CODE;
  sample_code CODE;
BEGIN
  if NEW.expe_id IS NOT NULL then
    RETURN NEW;
  end if;
  if NEW.samp_id IS NULL then
    RAISE EXCEPTION 'Neither experiment nor sample is specified for data set %', NEW.code;
  end if;
  select s.id, s.code into space_id, sample_code from samples_all s where s.id = NEW.samp_id;
  if space_id is NULL then
    RAISE EXCEPTION 'Sample % is a shared sample.', sample_code;
  end if;
  RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION MATERIAL_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK() RETURNS trigger AS $$
DECLARE
   v_type_id  CODE;
   v_type_id_prop  CODE;
BEGIN
   if NEW.mate_prop_id IS NOT NULL then
      -- find material type id of the property type 
      select pt.maty_prop_id into v_type_id_prop 
        from material_type_property_types etpt, property_types pt 
       where NEW.mtpt_id = etpt.id AND etpt.prty_id = pt.id;
    
      if v_type_id_prop IS NOT NULL then
        -- find material type id of the material which consists the entity's property value
        select entity.maty_id into v_type_id 
          from materials entity
         where NEW.mate_prop_id = entity.id;
        if v_type_id != v_type_id_prop then
          RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', 
               NEW.mate_prop_id, v_type_id, v_type_id_prop;
        end if;
      end if;
   end if;
   RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION SAMPLE_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK() RETURNS trigger AS $$
DECLARE
   v_type_id  CODE;
   v_type_id_prop  CODE;
BEGIN
   if NEW.mate_prop_id IS NOT NULL then
      -- find material type id of the property type 
      select pt.maty_prop_id into v_type_id_prop 
        from sample_type_property_types etpt, property_types pt 
       where NEW.stpt_id = etpt.id AND etpt.prty_id = pt.id;
    
      if v_type_id_prop IS NOT NULL then
        -- find material type id of the material which consists the entity's property value
        select entity.maty_id into v_type_id 
          from materials entity
         where NEW.mate_prop_id = entity.id;
        if v_type_id != v_type_id_prop then
          RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', 
                         NEW.mate_prop_id, v_type_id, v_type_id_prop;
        end if;
      end if;
   end if;
   RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION EXPERIMENT_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK() RETURNS trigger AS $$
DECLARE
   v_type_id  CODE;
   v_type_id_prop  CODE;
BEGIN
   if NEW.mate_prop_id IS NOT NULL then
      -- find material type id of the property type 
      select pt.maty_prop_id into v_type_id_prop 
        from experiment_type_property_types etpt, property_types pt 
       where NEW.etpt_id = etpt.id AND etpt.prty_id = pt.id;
    
      if v_type_id_prop IS NOT NULL then
        -- find material type id of the material which consists the entity's property value
        select entity.maty_id into v_type_id 
          from materials entity
         where NEW.mate_prop_id = entity.id;
        if v_type_id != v_type_id_prop then
          RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', 
                         NEW.mate_prop_id, v_type_id, v_type_id_prop;
        end if;
      end if;
   end if;
   RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION DATA_SET_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK() RETURNS trigger AS $$
DECLARE
   v_type_id  CODE;
   v_type_id_prop  CODE;
BEGIN
   if NEW.mate_prop_id IS NOT NULL then
      -- find material type id of the property type 
      select pt.maty_prop_id into v_type_id_prop 
        from data_set_type_property_types dstpt, property_types pt 
       where NEW.dstpt_id = dstpt.id AND dstpt.prty_id = pt.id;
    
      if v_type_id_prop IS NOT NULL then
        -- find material type id of the material which consists the entity's property value
        select entity.maty_id into v_type_id 
          from materials entity
         where NEW.mate_prop_id = entity.id;
        if v_type_id != v_type_id_prop then
          RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', 
                         NEW.mate_prop_id, v_type_id, v_type_id_prop;
        end if;
      end if;
   end if;
   RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION check_created_or_modified_data_set_owner_is_alive() RETURNS trigger AS $$
DECLARE
  owner_code  CODE;
  owner_del_id  TECH_ID;
BEGIN
  IF (NEW.del_id IS NOT NULL) THEN
    RETURN NEW;
  END IF;

  -- check sample
  IF (NEW.samp_id IS NOT NULL) THEN
    SELECT del_id, code INTO owner_del_id, owner_code
      FROM samples 
      WHERE id = NEW.samp_id;
    IF (owner_del_id IS NOT NULL) THEN 
      RAISE EXCEPTION 'Data Set (Code: %) cannot be connected to a Sample (Code: %) %.', 
                      NEW.code, owner_code, deletion_description(owner_del_id);
    END IF;
  END IF;
  -- check experiment
  IF (NEW.expe_id IS NOT NULL) THEN
    SELECT del_id, code INTO owner_del_id, owner_code
      FROM experiments 
      WHERE id = NEW.expe_id;
    IF (owner_del_id IS NOT NULL) THEN 
      RAISE EXCEPTION 'Data Set (Code: %) cannot be connected to an Experiment (Code: %) %.', 
                      NEW.code, owner_code, deletion_description(owner_del_id);
    END IF; 
  END IF; 
  RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';
  
CREATE OR REPLACE FUNCTION check_created_or_modified_sample_owner_is_alive() RETURNS trigger AS $$
DECLARE
  owner_code  CODE;
  owner_del_id  TECH_ID;
BEGIN
  IF (NEW.del_id IS NOT NULL) THEN
    RETURN NEW;
  END IF;

  -- check experiment (can't be deleted)
  IF (NEW.expe_id IS NOT NULL) THEN
    SELECT del_id, code INTO owner_del_id, owner_code
      FROM experiments 
      WHERE id = NEW.expe_id;
    IF (owner_del_id IS NOT NULL) THEN 
      RAISE EXCEPTION 'Sample (Code: %) cannot be connected to an Experiment (Code: %) %.', 
                      NEW.code, owner_code, deletion_description(owner_del_id);
    END IF;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';
  
