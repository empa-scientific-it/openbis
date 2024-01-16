-- Migration from 193 to 194

CREATE DOMAIN EMAIL_VALUE AS VARCHAR(320);

ALTER DOMAIN USER_ID RENAME TO USER_ID_DEPRECATED;

CREATE DOMAIN USER_ID AS VARCHAR(256);

ALTER TABLE IF EXISTS PERSONS
    ALTER COLUMN FIRST_NAME TYPE TEXT,
    ALTER COLUMN LAST_NAME TYPE TEXT,
    ALTER COLUMN EMAIL TYPE EMAIL_VALUE,
    ALTER COLUMN USER_ID TYPE USER_ID;

DROP DOMAIN IF EXISTS USER_ID_DEPRECATED;

-- code type change

ALTER DOMAIN CODE RENAME TO CODE_DEPRECATED;

CREATE DOMAIN CODE AS VARCHAR(256);



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
drop rule sample_child_annotations_update on sample_relationships_all;
drop rule sample_parent_annotations_update on sample_relationships_all;
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
drop rule sample_shared_insert on samples_all;
drop rule sample_shared_update on samples_all;
drop rule sample_shared_remove_update on samples_all;
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
drop rule content_copies_history_insert on content_copies;
drop rule edms_a_insert_content_copy_history on external_data_management_systems;
drop rule edms_b_expire_content_copy_history on external_data_management_systems;
--

DROP VIEW data;
DROP VIEW data_deleted;
DROP VIEW data_set_history_view;
DROP VIEW experiments;
DROP VIEW experiments_deleted;
DROP VIEW experiment_history_view;
DROP VIEW samples;
DROP VIEW samples_deleted;
DROP VIEW sample_history_view;


ALTER TABLE IF EXISTS CONTROLLED_VOCABULARIES
    ALTER COLUMN CODE TYPE CODE;

ALTER TABLE IF EXISTS DATA_ALL
    ALTER COLUMN CODE TYPE CODE,
    ALTER COLUMN DATA_PRODUCER_CODE TYPE CODE;

ALTER TABLE IF EXISTS DATA_STORES
    ALTER COLUMN UUID TYPE CODE,
    ALTER COLUMN CODE TYPE CODE;

ALTER TABLE IF EXISTS DATA_TYPES
    ALTER COLUMN CODE TYPE CODE;

ALTER TABLE IF EXISTS EXPERIMENTS_ALL
    ALTER COLUMN PERM_ID TYPE CODE,
    ALTER COLUMN CODE TYPE CODE;

ALTER TABLE IF EXISTS EXPERIMENT_TYPES
    ALTER COLUMN CODE TYPE CODE;

ALTER TABLE IF EXISTS EXTERNAL_DATA
    ALTER COLUMN SHARE_ID TYPE CODE;

ALTER TABLE IF EXISTS FILE_FORMAT_TYPES
    ALTER COLUMN CODE TYPE CODE;

ALTER TABLE IF EXISTS SPACES
    ALTER COLUMN CODE TYPE CODE;

ALTER TABLE IF EXISTS LOCATOR_TYPES
    ALTER COLUMN CODE TYPE CODE;

ALTER TABLE IF EXISTS MATERIALS
    ALTER COLUMN CODE TYPE CODE;

ALTER TABLE IF EXISTS MATERIAL_TYPES
    ALTER COLUMN CODE TYPE CODE;

ALTER TABLE IF EXISTS DATA_SET_TYPES
    ALTER COLUMN CODE TYPE CODE;

ALTER TABLE IF EXISTS PROJECTS
    ALTER COLUMN PERM_ID TYPE CODE,
    ALTER COLUMN CODE TYPE CODE;

ALTER TABLE IF EXISTS PROPERTY_TYPES
    ALTER COLUMN CODE TYPE CODE;

ALTER TABLE IF EXISTS SAMPLES_ALL
    ALTER COLUMN PERM_ID TYPE CODE,
    ALTER COLUMN CODE TYPE CODE;

ALTER TABLE IF EXISTS SAMPLE_TYPES
    ALTER COLUMN CODE TYPE CODE,
    ALTER COLUMN generated_code_prefix TYPE CODE;

ALTER TABLE IF EXISTS AUTHORIZATION_GROUPS
    ALTER COLUMN CODE TYPE CODE;

ALTER TABLE IF EXISTS QUERIES
    ALTER COLUMN ENTITY_TYPE_CODE TYPE CODE,
    ALTER COLUMN DB_KEY TYPE CODE;

ALTER TABLE IF EXISTS relationship_types
    ALTER COLUMN code TYPE CODE;

ALTER TABLE IF EXISTS EXTERNAL_DATA_MANAGEMENT_SYSTEMS
    ALTER COLUMN CODE TYPE CODE;

ALTER TABLE IF EXISTS METAPROJECTS
    ALTER COLUMN NAME TYPE CODE;

ALTER TABLE IF EXISTS OPERATION_EXECUTIONS
    ALTER COLUMN CODE TYPE CODE;

ALTER TABLE IF EXISTS data_set_copies_history
    ALTER COLUMN edms_code TYPE CODE;

ALTER TABLE IF EXISTS SEMANTIC_ANNOTATIONS
    ALTER COLUMN PERM_ID TYPE CODE;



CREATE OR REPLACE VIEW data AS
     SELECT id, code, dsty_id, dast_id, expe_id, expe_frozen, data_producer_code, production_timestamp, samp_id, samp_frozen,
            registration_timestamp, access_timestamp, pers_id_registerer, pers_id_modifier, is_valid, modification_timestamp,
            is_derived, del_id, orig_del, version, data_set_kind,
            frozen, frozen_for_children, frozen_for_parents, frozen_for_comps, frozen_for_conts, tsvector_document, meta_data
       FROM data_all
      WHERE del_id IS NULL;



CREATE OR REPLACE VIEW data_deleted AS
     SELECT id, code, dsty_id, dast_id, expe_id, data_producer_code, production_timestamp, samp_id, registration_timestamp, access_timestamp, pers_id_registerer, pers_id_modifier, is_valid, modification_timestamp, is_derived, del_id, orig_del, version, data_set_kind
       FROM data_all
      WHERE del_id IS NOT NULL;


CREATE OR REPLACE VIEW experiments AS
     SELECT id, perm_id, code, exty_id, pers_id_registerer, pers_id_modifier, registration_timestamp, modification_timestamp,
            proj_id, proj_frozen, del_id, orig_del, is_public, version, frozen, frozen_for_samp, frozen_for_data, tsvector_document, meta_data
       FROM experiments_all
      WHERE del_id IS NULL;


CREATE OR REPLACE VIEW experiments_deleted AS
     SELECT id, perm_id, code, exty_id, pers_id_registerer, pers_id_modifier, registration_timestamp, modification_timestamp, proj_id, del_id, orig_del, is_public, version
       FROM experiments_all
      WHERE del_id IS NOT NULL;


CREATE OR REPLACE VIEW samples AS
     SELECT id, perm_id, code, proj_id, proj_frozen, expe_id, expe_frozen, saty_id, registration_timestamp,
            modification_timestamp, pers_id_registerer, pers_id_modifier, del_id, orig_del, space_id, space_frozen,
            samp_id_part_of, cont_frozen, version, frozen, frozen_for_comp, frozen_for_children, frozen_for_parents, frozen_for_data, tsvector_document, sample_identifier, meta_data
       FROM samples_all
      WHERE del_id IS NULL;


CREATE OR REPLACE VIEW samples_deleted AS
     SELECT id, perm_id, code, expe_id, saty_id, registration_timestamp, modification_timestamp, pers_id_registerer, pers_id_modifier, del_id, orig_del, space_id, proj_id, samp_id_part_of, version
       FROM samples_all
      WHERE del_id IS NOT NULL;


CREATE VIEW data_set_history_view AS (
  SELECT
    3*id as id,
    main_data_id,
    relation_type,
    ordinal,
    expe_id,
    samp_id,
    data_id,
    entity_kind,
    entity_perm_id,
    null as dstpt_id,
    null as value,
    null as vocabulary_term,
    null as material,
    null as sample,
    null as external_code,
    null as path,
    null as git_commit_hash,
    null as git_repository_id,
    null::TECH_ID as edms_id,
    null as edms_code,
    null as edms_label,
    null as edms_address,
    pers_id_author,
    valid_from_timestamp,
    valid_until_timestamp,
    null as integer_array_value,
    null as real_array_value,
    null as string_array_value,
    null as timestamp_array_value,
    null as json_value
  FROM
    data_set_relationships_history
  WHERE
    valid_until_timestamp IS NOT NULL)
UNION
  SELECT
    3*id+1 as id,
    ds_id as main_data_id,
    null as relation_type,
    null as ordinal,
    null as expe_id,
    null as samp_id,
    null as data_id,
    null as entity_kind,
    null as entity_perm_id,
    dstpt_id,
    value,
    vocabulary_term,
    material,
    sample,
    null as external_code,
    null as path,
    null as git_commit_hash,
    null as git_repository_id,
    null as edms_id,
    null as edms_code,
    null as edms_label,
    null as edms_address,
    pers_id_author,
    valid_from_timestamp,
    valid_until_timestamp,
    integer_array_value,
    real_array_value,
    string_array_value,
    timestamp_array_value,
    json_value
  FROM
    data_set_properties_history
 UNION
  (SELECT
   3*id+2 as id,
    data_id as main_data_id,
    null as relation_type,
    null as ordinal,
    null as expe_id,
    null as samp_id,
    null as data_id,
    null as entity_kind,
    null as entity_perm_id,
    null as dstpt_id,
    null as value,
    null as vocabulary_term,
    null as material,
    null as sample,
    external_code,
    path,
    git_commit_hash,
    git_repository_id,
    edms_id,
    edms_code,
    edms_label,
    edms_address,
    pers_id_author,
    valid_from_timestamp,
    valid_until_timestamp,
    null as integer_array_value,
    null as real_array_value,
    null as string_array_value,
    null as timestamp_array_value,
    null as json_value
  FROM
    data_set_copies_history
  WHERE
    valid_until_timestamp IS NOT NULL);

CREATE VIEW experiment_history_view AS (
  SELECT
    2*id as id,
    main_expe_id,
    relation_type,
    proj_id,
    samp_id,
    data_id,
    entity_kind,
    entity_perm_id,
    null as etpt_id,
    null as value,
    null as vocabulary_term,
    null as material,
    null as sample,
    pers_id_author,
    valid_from_timestamp,
    valid_until_timestamp,
    null as integer_array_value,
    null as real_array_value,
    null as string_array_value,
    null as timestamp_array_value,
    null as json_value
  FROM
    EXPERIMENT_RELATIONSHIPS_HISTORY
  WHERE valid_until_timestamp IS NOT NULL)
UNION
  SELECT
    2*id+1 as id,
    expe_id as main_expe_id,
    null as relation_type,
    null as proj_id,
    null as samp_id,
    null as data_id,
    null as entity_kind,
    null as entity_perm_id,
    etpt_id,
    value,
    vocabulary_term,
    material,
    sample,
    pers_id_author,
    valid_from_timestamp,
    valid_until_timestamp,
    integer_array_value,
    real_array_value,
    string_array_value,
    timestamp_array_value,
    json_value
  FROM
    EXPERIMENT_PROPERTIES_HISTORY;

CREATE VIEW sample_history_view AS (
  SELECT
    2*id as id,
    main_samp_id,
    relation_type,
    space_id,
    expe_id,
    samp_id,
    proj_id,
    data_id,
    entity_kind,
    entity_perm_id,
    annotations,
    null as stpt_id,
    null as value,
    null as vocabulary_term,
    null as material,
    null as sample,
    pers_id_author,
    valid_from_timestamp,
    valid_until_timestamp,
    null as integer_array_value,
    null as real_array_value,
    null as string_array_value,
    null as timestamp_array_value,
    null as json_value
  FROM
    SAMPLE_RELATIONSHIPS_HISTORY
  WHERE
    valid_until_timestamp IS NOT NULL)
UNION
  SELECT
    2*id+1 as id,
    samp_id as main_samp_id,
    null as relation_type,
    null as space_id,
    null as expe_id,
    null as samp_id,
    null as proj_id,
    null as data_id,
    null as entity_kind,
    null as entity_perm_id,
    null as annotations,
    stpt_id,
    value,
    vocabulary_term,
    material,
    sample,
    pers_id_author,
    valid_from_timestamp,
    valid_until_timestamp,
    integer_array_value,
    real_array_value,
    string_array_value,
    timestamp_array_value,
    json_value
  FROM
    SAMPLE_PROPERTIES_HISTORY;


-- RECREATE RULES

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

CREATE OR REPLACE RULE sample_properties_update AS
    ON UPDATE TO sample_properties
    WHERE (OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd' AND OLD.VALUE != NEW.VALUE)
        OR (OLD.CVTE_ID IS NOT NULL AND OLD.CVTE_ID != NEW.CVTE_ID)
        OR (OLD.MATE_PROP_ID IS NOT NULL AND OLD.MATE_PROP_ID != NEW.MATE_PROP_ID)
        OR (OLD.SAMP_PROP_ID IS NOT NULL AND OLD.SAMP_PROP_ID != NEW.SAMP_PROP_ID)
        OR (OLD.INTEGER_ARRAY_VALUE IS NOT NULL AND OLD.INTEGER_ARRAY_VALUE != NEW.INTEGER_ARRAY_VALUE)
        OR (OLD.REAL_ARRAY_VALUE IS NOT NULL AND OLD.REAL_ARRAY_VALUE != NEW.REAL_ARRAY_VALUE)
        OR (OLD.STRING_ARRAY_VALUE IS NOT NULL AND OLD.STRING_ARRAY_VALUE != NEW.STRING_ARRAY_VALUE)
        OR (OLD.TIMESTAMP_ARRAY_VALUE IS NOT NULL AND OLD.TIMESTAMP_ARRAY_VALUE != NEW.TIMESTAMP_ARRAY_VALUE)
        OR (OLD.JSON_VALUE IS NOT NULL AND OLD.JSON_VALUE != NEW.JSON_VALUE)
    DO ALSO
       INSERT INTO sample_properties_history (
         ID,
         SAMP_ID,
         STPT_ID,
         VALUE,
         VOCABULARY_TERM,
         MATERIAL,
         SAMPLE,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP,
         INTEGER_ARRAY_VALUE,
         REAL_ARRAY_VALUE,
         STRING_ARRAY_VALUE,
         TIMESTAMP_ARRAY_VALUE,
         JSON_VALUE
       ) VALUES (
         nextval('SAMPLE_PROPERTY_ID_SEQ'),
         OLD.SAMP_ID,
         OLD.STPT_ID,
         OLD.VALUE,
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
         (select perm_id from samples_all where id = OLD.SAMP_PROP_ID),
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         NEW.MODIFICATION_TIMESTAMP,
         OLD.INTEGER_ARRAY_VALUE,
         OLD.REAL_ARRAY_VALUE,
         OLD.STRING_ARRAY_VALUE,
         OLD.TIMESTAMP_ARRAY_VALUE,
         OLD.JSON_VALUE
       );
CREATE OR REPLACE RULE sample_properties_delete AS
    ON DELETE TO sample_properties
    WHERE ((OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd')
        OR OLD.CVTE_ID IS NOT NULL
        OR OLD.MATE_PROP_ID IS NOT NULL
        OR OLD.SAMP_PROP_ID IS NOT NULL
        OR OLD.INTEGER_ARRAY_VALUE IS NOT NULL
        OR OLD.REAL_ARRAY_VALUE IS NOT NULL
        OR OLD.STRING_ARRAY_VALUE IS NOT NULL
        OR OLD.TIMESTAMP_ARRAY_VALUE IS NOT NULL
        OR OLD.JSON_VALUE IS NOT NULL)
       AND (SELECT DEL_ID FROM SAMPLES_ALL WHERE ID = OLD.SAMP_ID) IS NULL
     DO ALSO
       INSERT INTO sample_properties_history (
         ID,
         SAMP_ID,
         STPT_ID,
         VALUE,
         VOCABULARY_TERM,
         MATERIAL,
         SAMPLE,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP,
         INTEGER_ARRAY_VALUE,
         REAL_ARRAY_VALUE,
         STRING_ARRAY_VALUE,
         TIMESTAMP_ARRAY_VALUE,
         JSON_VALUE
       ) VALUES (
         nextval('SAMPLE_PROPERTY_ID_SEQ'),
         OLD.SAMP_ID,
         OLD.STPT_ID,
         OLD.VALUE,
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
         (select perm_id from samples_all where id = OLD.SAMP_PROP_ID),
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         current_timestamp,
         OLD.INTEGER_ARRAY_VALUE,
         OLD.REAL_ARRAY_VALUE,
         OLD.STRING_ARRAY_VALUE,
         OLD.TIMESTAMP_ARRAY_VALUE,
         OLD.JSON_VALUE
       );

CREATE OR REPLACE RULE data_set_properties_update AS
    ON UPDATE TO data_set_properties
    WHERE (OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd' AND OLD.VALUE != NEW.VALUE)
        OR (OLD.CVTE_ID IS NOT NULL AND OLD.CVTE_ID != NEW.CVTE_ID)
        OR (OLD.MATE_PROP_ID IS NOT NULL AND OLD.MATE_PROP_ID != NEW.MATE_PROP_ID)
        OR (OLD.SAMP_PROP_ID IS NOT NULL AND OLD.SAMP_PROP_ID != NEW.SAMP_PROP_ID)
        OR (OLD.INTEGER_ARRAY_VALUE IS NOT NULL AND OLD.INTEGER_ARRAY_VALUE != NEW.INTEGER_ARRAY_VALUE)
        OR (OLD.REAL_ARRAY_VALUE IS NOT NULL AND OLD.REAL_ARRAY_VALUE != NEW.REAL_ARRAY_VALUE)
        OR (OLD.STRING_ARRAY_VALUE IS NOT NULL AND OLD.STRING_ARRAY_VALUE != NEW.STRING_ARRAY_VALUE)
        OR (OLD.TIMESTAMP_ARRAY_VALUE IS NOT NULL AND OLD.TIMESTAMP_ARRAY_VALUE != NEW.TIMESTAMP_ARRAY_VALUE)
        OR (OLD.JSON_VALUE IS NOT NULL AND OLD.JSON_VALUE != NEW.JSON_VALUE)
    DO ALSO
       INSERT INTO data_set_properties_history (
         ID,
         DS_ID,
         DSTPT_ID,
         VALUE,
         VOCABULARY_TERM,
         MATERIAL,
         SAMPLE,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP,
         INTEGER_ARRAY_VALUE,
         REAL_ARRAY_VALUE,
         STRING_ARRAY_VALUE,
         TIMESTAMP_ARRAY_VALUE,
         JSON_VALUE
       ) VALUES (
         nextval('DATA_SET_PROPERTY_ID_SEQ'),
         OLD.DS_ID,
         OLD.DSTPT_ID,
         OLD.VALUE,
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
         (select perm_id from samples_all where id = OLD.SAMP_PROP_ID),
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         NEW.MODIFICATION_TIMESTAMP,
         OLD.INTEGER_ARRAY_VALUE,
         OLD.REAL_ARRAY_VALUE,
         OLD.STRING_ARRAY_VALUE,
         OLD.TIMESTAMP_ARRAY_VALUE,
         OLD.JSON_VALUE
       );

CREATE OR REPLACE RULE data_set_properties_delete AS
    ON DELETE TO data_set_properties
    WHERE ((OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd')
        OR OLD.CVTE_ID IS NOT NULL
        OR OLD.MATE_PROP_ID IS NOT NULL
        OR OLD.SAMP_PROP_ID IS NOT NULL
        OR OLD.INTEGER_ARRAY_VALUE IS NOT NULL
        OR OLD.REAL_ARRAY_VALUE IS NOT NULL
        OR OLD.STRING_ARRAY_VALUE IS NOT NULL
        OR OLD.TIMESTAMP_ARRAY_VALUE IS NOT NULL
        OR OLD.JSON_VALUE IS NOT NULL)
	   AND (SELECT DEL_ID FROM DATA_ALL WHERE ID = OLD.DS_ID) IS NULL
    DO ALSO
       INSERT INTO data_set_properties_history (
         ID,
         DS_ID,
         DSTPT_ID,
         VALUE,
         VOCABULARY_TERM,
         MATERIAL,
         SAMPLE,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP,
         INTEGER_ARRAY_VALUE,
         REAL_ARRAY_VALUE,
         STRING_ARRAY_VALUE,
         TIMESTAMP_ARRAY_VALUE,
         JSON_VALUE
       ) VALUES (
         nextval('DATA_SET_PROPERTY_ID_SEQ'),
         OLD.DS_ID,
         OLD.DSTPT_ID,
         OLD.VALUE,
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
         (select perm_id from samples_all where id = OLD.SAMP_PROP_ID),
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         current_timestamp,
         OLD.INTEGER_ARRAY_VALUE,
         OLD.REAL_ARRAY_VALUE,
         OLD.STRING_ARRAY_VALUE,
         OLD.TIMESTAMP_ARRAY_VALUE,
         OLD.JSON_VALUE
       );

CREATE OR REPLACE RULE experiment_properties_update AS
    ON UPDATE TO experiment_properties
    WHERE (OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd' AND OLD.VALUE != NEW.VALUE)
        OR (OLD.CVTE_ID IS NOT NULL AND OLD.CVTE_ID != NEW.CVTE_ID)
        OR (OLD.MATE_PROP_ID IS NOT NULL AND OLD.MATE_PROP_ID != NEW.MATE_PROP_ID)
        OR (OLD.SAMP_PROP_ID IS NOT NULL AND OLD.SAMP_PROP_ID != NEW.SAMP_PROP_ID)
        OR (OLD.INTEGER_ARRAY_VALUE IS NOT NULL AND OLD.INTEGER_ARRAY_VALUE != NEW.INTEGER_ARRAY_VALUE)
        OR (OLD.REAL_ARRAY_VALUE IS NOT NULL AND OLD.REAL_ARRAY_VALUE != NEW.REAL_ARRAY_VALUE)
        OR (OLD.STRING_ARRAY_VALUE IS NOT NULL AND OLD.STRING_ARRAY_VALUE != NEW.STRING_ARRAY_VALUE)
        OR (OLD.TIMESTAMP_ARRAY_VALUE IS NOT NULL AND OLD.TIMESTAMP_ARRAY_VALUE != NEW.TIMESTAMP_ARRAY_VALUE)
        OR (OLD.JSON_VALUE IS NOT NULL AND OLD.JSON_VALUE != NEW.JSON_VALUE)
    DO ALSO
       INSERT INTO experiment_properties_history (
         ID,
         EXPE_ID,
         ETPT_ID,
         VALUE,
         VOCABULARY_TERM,
         MATERIAL,
         SAMPLE,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP,
         INTEGER_ARRAY_VALUE,
         REAL_ARRAY_VALUE,
         STRING_ARRAY_VALUE,
         TIMESTAMP_ARRAY_VALUE,
         JSON_VALUE
       ) VALUES (
         nextval('EXPERIMENT_PROPERTY_ID_SEQ'),
         OLD.EXPE_ID,
         OLD.ETPT_ID,
         OLD.VALUE,
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
         (select perm_id from samples_all where id = OLD.SAMP_PROP_ID),
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         NEW.MODIFICATION_TIMESTAMP,
         OLD.INTEGER_ARRAY_VALUE,
         OLD.REAL_ARRAY_VALUE,
         OLD.STRING_ARRAY_VALUE,
         OLD.TIMESTAMP_ARRAY_VALUE,
         OLD.JSON_VALUE
       );

CREATE OR REPLACE RULE experiment_properties_delete AS
    ON DELETE TO experiment_properties
    WHERE (OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd')
        OR OLD.CVTE_ID IS NOT NULL
        OR OLD.MATE_PROP_ID IS NOT NULL
        OR OLD.SAMP_PROP_ID IS NOT NULL
        OR OLD.INTEGER_ARRAY_VALUE IS NOT NULL
        OR OLD.REAL_ARRAY_VALUE IS NOT NULL
        OR OLD.STRING_ARRAY_VALUE IS NOT NULL
        OR OLD.TIMESTAMP_ARRAY_VALUE IS NOT NULL
        OR OLD.JSON_VALUE IS NOT NULL
    DO ALSO
       INSERT INTO experiment_properties_history (
         ID,
         EXPE_ID,
         ETPT_ID,
         VALUE,
         VOCABULARY_TERM,
         MATERIAL,
         SAMPLE,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP,
         INTEGER_ARRAY_VALUE,
         REAL_ARRAY_VALUE,
         STRING_ARRAY_VALUE,
         TIMESTAMP_ARRAY_VALUE,
         JSON_VALUE
       ) VALUES (
         nextval('EXPERIMENT_PROPERTY_ID_SEQ'),
         OLD.EXPE_ID,
         OLD.ETPT_ID,
         OLD.VALUE,
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
         (select perm_id from samples_all where id = OLD.SAMP_PROP_ID),
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         current_timestamp,
         OLD.INTEGER_ARRAY_VALUE,
         OLD.REAL_ARRAY_VALUE,
         OLD.STRING_ARRAY_VALUE,
         OLD.TIMESTAMP_ARRAY_VALUE,
         OLD.JSON_VALUE
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
         ENTITY_KIND,
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('EXPERIMENT_RELATIONSHIPS_HISTORY_ID_SEQ'),
         NEW.EXPE_ID,
         'OWNER',
         NEW.ID,
         'SAMPLE',
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
         ENTITY_KIND,
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'),
         NEW.ID,
         'OWNED',
         NEW.EXPE_ID,
         'EXPERIMENT',
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
         ENTITY_KIND,
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('EXPERIMENT_RELATIONSHIPS_HISTORY_ID_SEQ'),
         NEW.EXPE_ID,
         'OWNER',
         NEW.ID,
         'SAMPLE',
         NEW.PERM_ID,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID,
         MAIN_SAMP_ID,
         RELATION_TYPE,
         EXPE_ID,
         ENTITY_KIND,
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'),
         NEW.ID,
         'OWNED',
         NEW.EXPE_ID,
         'EXPERIMENT',
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
         ENTITY_KIND,
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'),
         NEW.SAMP_ID_PART_OF,
         'CONTAINER',
         NEW.ID,
         'SAMPLE',
         NEW.PERM_ID,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID,
         MAIN_SAMP_ID,
         RELATION_TYPE,
         SAMP_ID,
         ENTITY_KIND,
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'),
         NEW.ID,
         'CONTAINED',
         NEW.SAMP_ID_PART_OF,
         'SAMPLE',
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
         ENTITY_KIND,
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'),
         NEW.SAMP_ID_PART_OF,
         'CONTAINER',
         NEW.ID,
         'SAMPLE',
         NEW.PERM_ID,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID,
         MAIN_SAMP_ID,
         RELATION_TYPE,
         SAMP_ID,
         ENTITY_KIND,
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'),
         NEW.ID,
         'CONTAINED',
         NEW.SAMP_ID_PART_OF,
         'SAMPLE',
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
         ENTITY_KIND,
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('EXPERIMENT_RELATIONSHIPS_HISTORY_ID_SEQ'),
         NEW.EXPE_ID,
         'OWNER',
         NEW.ID,
         'DATA SET',
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
         ENTITY_KIND,
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'),
         NEW.ID,
         'OWNED',
         NEW.EXPE_ID,
         'EXPERIMENT',
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
         ENTITY_KIND,
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('EXPERIMENT_RELATIONSHIPS_HISTORY_ID_SEQ'),
         NEW.EXPE_ID,
         'OWNER',
         NEW.ID,
         'DATA SET',
         NEW.CODE,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
         ID,
         MAIN_DATA_ID,
         RELATION_TYPE,
         EXPE_ID,
         ENTITY_KIND,
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'),
         NEW.ID,
         'OWNED',
         NEW.EXPE_ID,
         'EXPERIMENT',
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
         ENTITY_KIND,
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'),
         NEW.SAMP_ID,
         'OWNER',
         NEW.ID,
         'DATA SET',
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
         ENTITY_KIND,
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'),
         NEW.ID,
         'OWNED',
         NEW.SAMP_ID,
         'SAMPLE',
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
         ENTITY_KIND,
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'),
         NEW.SAMP_ID,
         'OWNER',
         NEW.ID,
         'DATA SET',
         NEW.CODE,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
         ID,
         MAIN_DATA_ID,
         RELATION_TYPE,
         SAMP_ID,
         ENTITY_KIND,
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'),
         NEW.ID,
         'OWNED',
         NEW.SAMP_ID,
         'SAMPLE',
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
           ENTITY_KIND,
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP,
           ORDINAL
         ) VALUES (
           nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'),
           NEW.DATA_ID_PARENT,
           (SELECT UPPER(PARENT_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = NEW.RELATIONSHIP_ID),
           NEW.DATA_ID_CHILD,
           'DATA SET',
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
           ENTITY_KIND,
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP,
           ORDINAL
         ) VALUES (
           nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'),
           NEW.DATA_ID_CHILD,
           (SELECT UPPER(CHILD_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = NEW.RELATIONSHIP_ID),
           NEW.DATA_ID_PARENT,
           'DATA SET',
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
           ENTITY_KIND,
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP,
           ORDINAL
         ) VALUES (
           nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'),
           NEW.DATA_ID_PARENT,
           (SELECT UPPER(PARENT_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = NEW.RELATIONSHIP_ID),
           NEW.DATA_ID_CHILD,
           'DATA SET',
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
           ENTITY_KIND,
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP,
           ORDINAL
         ) VALUES (
           nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'),
           NEW.DATA_ID_CHILD,
           (SELECT UPPER(CHILD_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = NEW.RELATIONSHIP_ID),
           NEW.DATA_ID_PARENT,
           'DATA SET',
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
           ENTITY_KIND,
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP,
           ORDINAL
         ) VALUES (
           nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'),
           NEW.DATA_ID_PARENT,
           (SELECT UPPER(PARENT_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = NEW.RELATIONSHIP_ID),
           NEW.DATA_ID_CHILD,
           'DATA SET',
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
           ENTITY_KIND,
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP,
           ORDINAL
         ) VALUES (
           nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'),
           NEW.DATA_ID_CHILD,
           (SELECT UPPER(CHILD_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = NEW.RELATIONSHIP_ID),
           NEW.DATA_ID_PARENT,
           'DATA SET',
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
           ENTITY_KIND,
           ENTITY_PERM_ID,
           ANNOTATIONS,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP
         ) VALUES (
           nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'),
           NEW.SAMPLE_ID_PARENT,
           'PARENT',
           NEW.SAMPLE_ID_CHILD,
           'SAMPLE',
           (SELECT PERM_ID FROM samples_all WHERE ID = NEW.SAMPLE_ID_CHILD),
           NEW.PARENT_ANNOTATIONS,
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP
         );
         INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
           ID,
           MAIN_SAMP_ID,
           RELATION_TYPE,
           SAMP_ID,
           ENTITY_KIND,
           ENTITY_PERM_ID,
           ANNOTATIONS,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP
         ) VALUES (
           nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'),
           NEW.SAMPLE_ID_CHILD,
           'CHILD',
           NEW.SAMPLE_ID_PARENT,
           'SAMPLE',
           (SELECT PERM_ID FROM samples_all WHERE ID = NEW.SAMPLE_ID_PARENT),
           NEW.CHILD_ANNOTATIONS,
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

CREATE OR REPLACE RULE sample_child_annotations_update AS
    ON UPDATE TO sample_relationships_all
    WHERE OLD.DEL_ID IS NULL AND NEW.DEL_ID IS NULL
          AND OLD.SAMPLE_ID_CHILD = NEW.SAMPLE_ID_CHILD AND OLD.SAMPLE_ID_PARENT = NEW.SAMPLE_ID_PARENT
          AND OLD.CHILD_ANNOTATIONS <> NEW.CHILD_ANNOTATIONS
       DO ALSO (
         INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
           ID,
           MAIN_SAMP_ID,
           RELATION_TYPE,
           SAMP_ID,
           ENTITY_KIND,
           ENTITY_PERM_ID,
           ANNOTATIONS,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP,
           VALID_UNTIL_TIMESTAMP
         ) VALUES (
           nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'),
           NEW.SAMPLE_ID_CHILD,
           'CHILD',
           NEW.SAMPLE_ID_PARENT,
           'SAMPLE',
           (SELECT PERM_ID FROM samples_all WHERE ID = NEW.SAMPLE_ID_PARENT),
           OLD.CHILD_ANNOTATIONS,
           NEW.PERS_ID_AUTHOR,
           OLD.MODIFICATION_TIMESTAMP,
           NEW.MODIFICATION_TIMESTAMP
         );
       );

CREATE OR REPLACE RULE sample_parent_annotations_update AS
    ON UPDATE TO sample_relationships_all
    WHERE OLD.DEL_ID IS NULL AND NEW.DEL_ID IS NULL
          AND OLD.SAMPLE_ID_CHILD = NEW.SAMPLE_ID_CHILD AND OLD.SAMPLE_ID_PARENT = NEW.SAMPLE_ID_PARENT
          AND OLD.PARENT_ANNOTATIONS <> NEW.PARENT_ANNOTATIONS
       DO ALSO (
         INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
           ID,
           MAIN_SAMP_ID,
           RELATION_TYPE,
           SAMP_ID,
           ENTITY_KIND,
           ENTITY_PERM_ID,
           ANNOTATIONS,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP
         ) VALUES (
           nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'),
           NEW.SAMPLE_ID_PARENT,
           'PARENT',
           NEW.SAMPLE_ID_CHILD,
           'SAMPLE',
           (SELECT PERM_ID FROM samples_all WHERE ID = NEW.SAMPLE_ID_CHILD),
           OLD.PARENT_ANNOTATIONS,
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP
         );
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
           ENTITY_KIND,
           ENTITY_PERM_ID,
           ANNOTATIONS,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP
         ) VALUES (
           nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'),
           NEW.SAMPLE_ID_PARENT,
           'PARENT',
           NEW.SAMPLE_ID_CHILD,
           'SAMPLE',
           (SELECT PERM_ID FROM samples_all WHERE ID = NEW.SAMPLE_ID_CHILD),
           NEW.PARENT_ANNOTATIONS,
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP
         );
         INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
           ID,
           MAIN_SAMP_ID,
           RELATION_TYPE,
           SAMP_ID,
           ENTITY_KIND,
           ENTITY_PERM_ID,
           ANNOTATIONS,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP
         ) VALUES (
           nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'),
           NEW.SAMPLE_ID_CHILD,
           'CHILD',
           NEW.SAMPLE_ID_PARENT,
           'SAMPLE',
           (SELECT PERM_ID FROM samples_all WHERE ID = NEW.SAMPLE_ID_PARENT),
           NEW.CHILD_ANNOTATIONS,
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
         ENTITY_KIND,
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('PROJECT_RELATIONSHIPS_HISTORY_ID_SEQ'),
         NEW.PROJ_ID,
         'OWNER',
         NEW.ID,
         'EXPERIMENT',
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
         ENTITY_KIND,
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('EXPERIMENT_RELATIONSHIPS_HISTORY_ID_SEQ'),
         NEW.ID,
         'OWNED',
         NEW.PROJ_ID,
         'PROJECT',
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
         ENTITY_KIND,
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('PROJECT_RELATIONSHIPS_HISTORY_ID_SEQ'),
         NEW.PROJ_ID,
         'OWNER',
         NEW.ID,
         'EXPERIMENT',
         NEW.PERM_ID,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       INSERT INTO EXPERIMENT_RELATIONSHIPS_HISTORY (
         ID,
         MAIN_EXPE_ID,
         RELATION_TYPE,
         PROJ_ID,
         ENTITY_KIND,
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('EXPERIMENT_RELATIONSHIPS_HISTORY_ID_SEQ'),
         NEW.ID,
         'OWNED',
         NEW.PROJ_ID,
         'PROJECT',
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
         ENTITY_KIND,
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('PROJECT_RELATIONSHIPS_HISTORY_ID_SEQ'),
         NEW.ID,
         'OWNED',
         NEW.SPACE_ID,
         'SPACE',
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
         ENTITY_KIND,
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('PROJECT_RELATIONSHIPS_HISTORY_ID_SEQ'),
         NEW.ID,
         'OWNED',
         NEW.SPACE_ID,
         'SPACE',
         (SELECT CODE FROM SPACES WHERE ID = NEW.SPACE_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
   );

-- sample -> project

CREATE OR REPLACE RULE sample_project_update AS
    ON UPDATE TO samples_all
    WHERE (OLD.PROJ_ID != NEW.PROJ_ID OR OLD.PROJ_ID IS NULL OR OLD.EXPE_ID IS NOT NULL) AND NEW.PROJ_ID IS NOT NULL AND NEW.EXPE_ID IS NULL
    DO ALSO (
       UPDATE PROJECT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_PROJ_ID = OLD.PROJ_ID AND SAMP_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       INSERT INTO PROJECT_RELATIONSHIPS_HISTORY (
         ID,
         MAIN_PROJ_ID,
         RELATION_TYPE,
         SAMP_ID,
         ENTITY_KIND,
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('PROJECT_RELATIONSHIPS_HISTORY_ID_SEQ'),
         NEW.PROJ_ID,
         'OWNER',
         NEW.ID,
         'SAMPLE',
         NEW.PERM_ID,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_SAMP_ID = OLD.ID AND PROJ_ID = OLD.PROJ_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID,
         MAIN_SAMP_ID,
         RELATION_TYPE,
         PROJ_ID,
         ENTITY_KIND,
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'),
         NEW.ID,
         'OWNED',
         NEW.PROJ_ID,
         'PROJECT',
         (SELECT PERM_ID FROM PROJECTS WHERE ID = NEW.PROJ_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
    );

CREATE OR REPLACE RULE sample_project_remove_update AS
    ON UPDATE TO samples_all
    WHERE OLD.PROJ_ID IS NOT NULL AND (NEW.PROJ_ID IS NULL OR (OLD.EXPE_ID IS NULL AND NEW.EXPE_ID IS NOT NULL))
    DO ALSO (
       UPDATE PROJECT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_PROJ_ID = OLD.PROJ_ID AND SAMP_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_SAMP_ID = OLD.ID AND PROJ_ID = OLD.PROJ_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
    );

CREATE OR REPLACE RULE sample_project_insert AS
    ON INSERT TO samples_all
    WHERE NEW.EXPE_ID IS NULL AND NEW.PROJ_ID IS NOT NULL
    DO ALSO (
       INSERT INTO PROJECT_RELATIONSHIPS_HISTORY (
         ID,
         MAIN_PROJ_ID,
         RELATION_TYPE,
         SAMP_ID,
         ENTITY_KIND,
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('PROJECT_RELATIONSHIPS_HISTORY_ID_SEQ'),
         NEW.PROJ_ID,
         'OWNER',
         NEW.ID,
         'SAMPLE',
         NEW.PERM_ID,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
      INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID,
         MAIN_SAMP_ID,
         RELATION_TYPE,
         PROJ_ID,
         ENTITY_KIND,
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'),
         NEW.ID,
         'OWNED',
         NEW.PROJ_ID,
         'PROJECT',
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
         ENTITY_KIND,
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'),
         NEW.ID,
         'OWNED',
         NEW.SPACE_ID,
         'SPACE',
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
         ENTITY_KIND,
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'),
         NEW.ID,
         'OWNED',
         NEW.SPACE_ID,
         'SPACE',
         (SELECT CODE FROM SPACES WHERE ID = NEW.SPACE_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
   );


CREATE OR REPLACE RULE sample_shared_insert AS
    ON INSERT TO samples_all
    WHERE NEW.SPACE_ID IS NULL
       DO ALSO (
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID,
         MAIN_SAMP_ID,
         RELATION_TYPE,
         EXPE_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'),
         NEW.ID,
         'OWNED',
         NEW.EXPE_ID,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
   );

CREATE OR REPLACE RULE sample_shared_update AS
    ON UPDATE TO samples_all
    WHERE OLD.SPACE_ID IS NOT NULL AND NEW.SPACE_ID IS NULL
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
         NULL,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
    );

CREATE OR REPLACE RULE sample_shared_remove_update AS
    ON UPDATE TO samples_all
    WHERE OLD.SPACE_ID IS NULL AND NEW.SPACE_ID IS NOT NULL
    DO ALSO (
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_SAMP_ID = OLD.ID AND SPACE_ID IS NULL AND ENTITY_PERM_ID IS NULL AND PROJ_ID IS NULL
               AND EXPE_ID IS NULL AND VALID_UNTIL_TIMESTAMP IS NULL;
    );

CREATE OR REPLACE RULE sample_insert AS
    ON INSERT TO samples DO INSTEAD
       INSERT INTO samples_all (
         id,
         frozen,
         frozen_for_comp,
         frozen_for_children,
         frozen_for_parents,
         frozen_for_data,
         code,
         del_id,
         orig_del,
         expe_id,
         expe_frozen,
         proj_id,
         proj_frozen,
         modification_timestamp,
         perm_id,
         pers_id_registerer,
         pers_id_modifier,
         registration_timestamp,
         samp_id_part_of,
         cont_frozen,
         saty_id,
         space_id,
         space_frozen,
         version,
         meta_data
       ) VALUES (
         NEW.id,
         NEW.frozen,
         NEW.frozen_for_comp,
         NEW.frozen_for_children,
         NEW.frozen_for_parents,
         NEW.frozen_for_data,
         NEW.code,
         NEW.del_id,
         NEW.orig_del,
         NEW.expe_id,
         NEW.expe_frozen,
         NEW.proj_id,
         NEW.proj_frozen,
         NEW.modification_timestamp,
         NEW.perm_id,
         NEW.pers_id_registerer,
         NEW.pers_id_modifier,
         NEW.registration_timestamp,
         NEW.samp_id_part_of,
         NEW.cont_frozen,
         NEW.saty_id,
         NEW.space_id,
         NEW.space_frozen,
         NEW.version,
         NEW.meta_data
       );

CREATE OR REPLACE RULE sample_update AS
    ON UPDATE TO samples DO INSTEAD
       UPDATE samples_all
          SET code = NEW.code,
              frozen = NEW.frozen,
              frozen_for_comp = NEW.frozen_for_comp,
              frozen_for_children = NEW.frozen_for_children,
              frozen_for_parents = NEW.frozen_for_parents,
              frozen_for_data = NEW.frozen_for_data,
              del_id = NEW.del_id,
              orig_del = NEW.orig_del,
              expe_id = NEW.expe_id,
              expe_frozen = NEW.expe_frozen,
              proj_id = NEW.proj_id,
              proj_frozen = NEW.proj_frozen,
              modification_timestamp = NEW.modification_timestamp,
              perm_id = NEW.perm_id,
              pers_id_registerer = NEW.pers_id_registerer,
              pers_id_modifier = NEW.pers_id_modifier,
              registration_timestamp = NEW.registration_timestamp,
              samp_id_part_of = NEW.samp_id_part_of,
              cont_frozen = NEW.cont_frozen,
              saty_id = NEW.saty_id,
              space_id = NEW.space_id,
              space_frozen = NEW.space_frozen,
              version = NEW.version,
              meta_data = NEW.meta_data
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
       frozen,
       frozen_for_samp,
       frozen_for_data,
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
       proj_frozen,
       registration_timestamp,
       version,
       meta_data
     ) VALUES (
       NEW.id,
       NEW.frozen,
       NEW.frozen_for_samp,
       NEW.frozen_for_data,
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
       NEW.proj_frozen,
       NEW.registration_timestamp,
       NEW.version,
       NEW.meta_data
     );

CREATE OR REPLACE RULE experiment_update AS
    ON UPDATE TO experiments DO INSTEAD
       UPDATE experiments_all
          SET code = NEW.code,
              frozen = NEW.frozen,
              frozen_for_samp = NEW.frozen_for_samp,
              frozen_for_data = NEW.frozen_for_data,
              del_id = NEW.del_id,
              orig_del = NEW.orig_del,
              exty_id = NEW.exty_id,
              is_public = NEW.is_public,
              modification_timestamp = NEW.modification_timestamp,
              perm_id = NEW.perm_id,
              pers_id_registerer = NEW.pers_id_registerer,
              pers_id_modifier = NEW.pers_id_modifier,
              proj_id = NEW.proj_id,
              proj_frozen = NEW.proj_frozen,
              registration_timestamp = NEW.registration_timestamp,
              version = NEW.version,
              meta_data = NEW.meta_data
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
       frozen,
       frozen_for_children,
       frozen_for_parents,
       frozen_for_comps,
       frozen_for_conts,
       code,
       del_id,
       orig_del,
       expe_id,
       expe_frozen,
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
       samp_frozen,
       version,
       data_set_kind,
       meta_data
     ) VALUES (
       NEW.id,
       NEW.frozen,
       NEW.frozen_for_children,
       NEW.frozen_for_parents,
       NEW.frozen_for_comps,
       NEW.frozen_for_conts,
       NEW.code,
       NEW.del_id,
       NEW.orig_del,
       NEW.expe_id,
       NEW.expe_frozen,
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
       NEW.samp_frozen,
       NEW.version,
       NEW.data_set_kind,
       NEW.meta_data
     );

CREATE OR REPLACE RULE data_update AS
    ON UPDATE TO data DO INSTEAD
       UPDATE data_all
          SET code = NEW.code,
              frozen = NEW.frozen,
              frozen_for_children = NEW.frozen_for_children,
              frozen_for_parents = NEW.frozen_for_parents,
              frozen_for_comps = NEW.frozen_for_comps,
              frozen_for_conts = NEW.frozen_for_conts,
              del_id = NEW.del_id,
              orig_del = NEW.orig_del,
              expe_id = NEW.expe_id,
              expe_frozen = NEW.expe_frozen,
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
              samp_frozen = NEW.samp_frozen,
              version = NEW.version,
              data_set_kind = NEW.data_set_kind,
              meta_data = NEW.meta_data
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


CREATE OR REPLACE RULE content_copies_history_insert AS
  ON INSERT TO content_copies
  DO ALSO (
    INSERT INTO data_set_copies_history (
      id,
      cc_id,
      data_id,
      external_code,
      path,
      git_commit_hash,
      git_repository_id,
      edms_id,
      edms_code,
      edms_label,
      edms_address,
      pers_id_author,
      valid_from_timestamp
    ) VALUES (
      nextval('data_set_copies_history_id_seq'),
      NEW.id,
      NEW.data_id,
      NEW.external_code,
      NEW.path,
      NEW.git_commit_hash,
      NEW.git_repository_id,
      NEW.edms_id,
      (SELECT code FROM external_data_management_systems WHERE id = NEW.edms_id),
      (SELECT label FROM external_data_management_systems WHERE id = NEW.edms_id),
      (SELECT address FROM external_data_management_systems WHERE id = NEW.edms_id),
      NEW.pers_id_registerer,
      NEW.registration_timestamp);
  );

CREATE OR REPLACE RULE edms_a_insert_content_copy_history AS
  ON UPDATE TO external_data_management_systems
  DO ALSO (
    INSERT INTO data_set_copies_history (
      id,
      cc_id,
      data_id,
      external_code,
      path,
      git_commit_hash,
      git_repository_id,
      edms_id,
      edms_code,
      edms_label,
      edms_address,
      pers_id_author,
      valid_from_timestamp
    )
    SELECT
    nextval('data_set_copies_history_id_seq'),
    dsch.cc_id,
    dsch.data_id,
    dsch.external_code,
    dsch.path,
    dsch.git_commit_hash,
    dsch.git_repository_id,
    dsch.edms_id,
    NEW.code,
    NEW.label,
    NEW.address,
    dsch.pers_id_author,
    CURRENT_TIMESTAMP
    FROM data_set_copies_history dsch
    JOIN external_data_management_systems edms
    ON edms.id = dsch.edms_id
    WHERE NEW.id = dsch.edms_id AND dsch.valid_until_timestamp IS NULL;
  );

-- expire content copy history entry on external dms change
CREATE OR REPLACE RULE edms_b_expire_content_copy_history AS
  ON UPDATE TO external_data_management_systems
  DO ALSO (
    UPDATE
    data_set_copies_history SET valid_until_timestamp = CURRENT_TIMESTAMP
    WHERE valid_until_timestamp IS NULL
    AND edms_id = NEW.id
    AND valid_from_timestamp <> CURRENT_TIMESTAMP;
);


-- RECREATE FUNCTIONS

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
      where is_managed_internally = true and
         id = (select covo_id from controlled_vocabulary_terms where id = NEW.cvte_id_stor_fmt);
   -- Check if the data storage format is a term of the controlled vocabulary "STORAGE_FORMAT"
   if v_covo_code != 'STORAGE_FORMAT' then
      select code into data_code from data_all where id = NEW.id;
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
	owner_code	CODE;
	owner_del_id	TECH_ID;
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
	owner_code	CODE;
	owner_del_id	TECH_ID;
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

CREATE OR REPLACE FUNCTION RAISE_EXCEPTION_FROZEN_DATA_SET_RELATIONSHIP() RETURNS trigger AS $$
DECLARE
    parent_id           TECH_ID;
    child_id            TECH_ID;
    relationship_id     TECH_ID;
    relationship        CODE;
    parent_child_frozen BOOLEAN_CHAR;
    cont_comp_frozen    BOOLEAN_CHAR;
BEGIN
    IF (TG_OP = 'DELETE') THEN
        parent_id = OLD.data_id_parent;
        child_id = OLD.data_id_child;
        relationship_id = OLD.relationship_id;
        parent_child_frozen = OLD.parent_frozen OR OLD.child_frozen;
        cont_comp_frozen = OLD.cont_frozen OR OLD.comp_frozen;
    ELSEIF (TG_OP = 'INSERT') THEN
        parent_id = NEW.data_id_parent;
        child_id = NEW.data_id_child;
        relationship_id = NEW.relationship_id;
        parent_child_frozen = NEW.parent_frozen OR NEW.child_frozen;
        cont_comp_frozen = NEW.cont_frozen OR NEW.comp_frozen;
    END IF;
    SELECT code INTO relationship FROM relationship_types WHERE id = relationship_id;
    IF (relationship = 'PARENT_CHILD' AND parent_child_frozen) OR (relationship = 'CONTAINER_COMPONENT' AND cont_comp_frozen) THEN
       RAISE EXCEPTION 'Operation % % is not allowed because data set % or % is frozen.', TG_OP, relationship,
            (select code from data_all where id = parent_id),
            (select code from data_all where id = child_id);
    END IF;
    IF (TG_OP = 'DELETE') THEN
        RETURN OLD;
    ELSEIF (TG_OP = 'INSERT') THEN
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE 'plpgsql';



DROP DOMAIN IF EXISTS CODE_DEPRECATED;
