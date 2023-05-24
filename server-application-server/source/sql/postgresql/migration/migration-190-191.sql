-- Migration from 190 to 191

-- domains.sql
CREATE DOMAIN LONG_VALUE AS BIGINT;
CREATE DOMAIN DOUBLE_VALUE AS DOUBLE PRECISION;

ALTER TABLE IF EXISTS MATERIAL_PROPERTIES
    ADD COLUMN INTEGER_ARRAY_VALUE LONG_VALUE[];

ALTER TABLE IF EXISTS MATERIAL_PROPERTIES
    ADD COLUMN REAL_ARRAY_VALUE DOUBLE_VALUE[];

ALTER TABLE IF EXISTS MATERIAL_PROPERTIES
    ADD COLUMN TIMESTAMP_ARRAY_VALUE TIME_STAMP[];

ALTER TABLE IF EXISTS MATERIAL_PROPERTIES
    ADD COLUMN STRING_ARRAY_VALUE text_value[];

ALTER TABLE IF EXISTS MATERIAL_PROPERTIES
    ADD COLUMN JSON_VALUE jsonb;

ALTER TABLE IF EXISTS MATERIAL_PROPERTIES_HISTORY
    ADD COLUMN INTEGER_ARRAY_VALUE LONG_VALUE[];

ALTER TABLE IF EXISTS MATERIAL_PROPERTIES_HISTORY
    ADD COLUMN REAL_ARRAY_VALUE DOUBLE_VALUE[];

ALTER TABLE IF EXISTS MATERIAL_PROPERTIES_HISTORY
    ADD COLUMN TIMESTAMP_ARRAY_VALUE TIME_STAMP[];

ALTER TABLE IF EXISTS MATERIAL_PROPERTIES_HISTORY
    ADD COLUMN STRING_ARRAY_VALUE text_value[];

ALTER TABLE IF EXISTS MATERIAL_PROPERTIES_HISTORY
    ADD COLUMN JSON_VALUE jsonb;



ALTER TABLE IF EXISTS EXPERIMENT_PROPERTIES
    ADD COLUMN INTEGER_ARRAY_VALUE LONG_VALUE[];

ALTER TABLE IF EXISTS EXPERIMENT_PROPERTIES
    ADD COLUMN REAL_ARRAY_VALUE DOUBLE_VALUE[];

ALTER TABLE IF EXISTS EXPERIMENT_PROPERTIES
    ADD COLUMN TIMESTAMP_ARRAY_VALUE TIME_STAMP[];

ALTER TABLE IF EXISTS EXPERIMENT_PROPERTIES
    ADD COLUMN STRING_ARRAY_VALUE text_value[];

ALTER TABLE IF EXISTS EXPERIMENT_PROPERTIES
    ADD COLUMN JSON_VALUE jsonb;

-- schema.sql
ALTER TABLE EXPERIMENT_PROPERTIES DROP CONSTRAINT EXPR_CK;
ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_CK CHECK
    ((VALUE IS NOT NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NOT NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NOT NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NOT NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NOT NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NOT NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NOT NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NOT NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NOT NULL)
    );


ALTER TABLE IF EXISTS EXPERIMENT_PROPERTIES_HISTORY
    ADD COLUMN INTEGER_ARRAY_VALUE LONG_VALUE[];

ALTER TABLE IF EXISTS EXPERIMENT_PROPERTIES_HISTORY
    ADD COLUMN REAL_ARRAY_VALUE DOUBLE_VALUE[];

ALTER TABLE IF EXISTS EXPERIMENT_PROPERTIES_HISTORY
    ADD COLUMN TIMESTAMP_ARRAY_VALUE TIME_STAMP[];

ALTER TABLE IF EXISTS EXPERIMENT_PROPERTIES_HISTORY
    ADD COLUMN STRING_ARRAY_VALUE text_value[];

ALTER TABLE IF EXISTS EXPERIMENT_PROPERTIES_HISTORY
    ADD COLUMN JSON_VALUE jsonb;

ALTER TABLE EXPERIMENT_PROPERTIES_HISTORY DROP CONSTRAINT EXPRH_CK;
ALTER TABLE EXPERIMENT_PROPERTIES_HISTORY ADD CONSTRAINT EXPRH_CK CHECK
    ((VALUE IS NOT NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL AND SAMPLE IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NOT NULL AND MATERIAL IS NULL AND SAMPLE IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NOT NULL AND SAMPLE IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL AND SAMPLE IS NOT NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL AND SAMPLE IS NULL AND INTEGER_ARRAY_VALUE IS NOT NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL AND SAMPLE IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NOT NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL AND SAMPLE IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NOT NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL AND SAMPLE IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NOT NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL AND SAMPLE IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NOT NULL)
    );


ALTER TABLE IF EXISTS SAMPLE_PROPERTIES
    ADD COLUMN INTEGER_ARRAY_VALUE LONG_VALUE[];

ALTER TABLE IF EXISTS SAMPLE_PROPERTIES
    ADD COLUMN REAL_ARRAY_VALUE DOUBLE_VALUE[];

ALTER TABLE IF EXISTS SAMPLE_PROPERTIES
    ADD COLUMN TIMESTAMP_ARRAY_VALUE TIME_STAMP[];

ALTER TABLE IF EXISTS SAMPLE_PROPERTIES
    ADD COLUMN STRING_ARRAY_VALUE text_value[];

ALTER TABLE IF EXISTS SAMPLE_PROPERTIES
    ADD COLUMN JSON_VALUE jsonb;

ALTER TABLE SAMPLE_PROPERTIES DROP CONSTRAINT SAPR_CK;
ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_CK CHECK
    ((VALUE IS NOT NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NOT NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NOT NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NOT NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NOT NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NOT NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NOT NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NOT NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NOT NULL)
    );

ALTER TABLE IF EXISTS SAMPLE_PROPERTIES_HISTORY
    ADD COLUMN INTEGER_ARRAY_VALUE LONG_VALUE[];

ALTER TABLE IF EXISTS SAMPLE_PROPERTIES_HISTORY
    ADD COLUMN REAL_ARRAY_VALUE DOUBLE_VALUE[];

ALTER TABLE IF EXISTS SAMPLE_PROPERTIES_HISTORY
    ADD COLUMN TIMESTAMP_ARRAY_VALUE TIME_STAMP[];

ALTER TABLE IF EXISTS SAMPLE_PROPERTIES_HISTORY
    ADD COLUMN STRING_ARRAY_VALUE text_value[];

ALTER TABLE IF EXISTS SAMPLE_PROPERTIES_HISTORY
    ADD COLUMN JSON_VALUE jsonb;

ALTER TABLE SAMPLE_PROPERTIES_HISTORY DROP CONSTRAINT SAPRH_CK;
ALTER TABLE SAMPLE_PROPERTIES_HISTORY ADD CONSTRAINT SAPRH_CK CHECK
    ((VALUE IS NOT NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL AND SAMPLE IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NOT NULL AND MATERIAL IS NULL AND SAMPLE IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NOT NULL AND SAMPLE IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL AND SAMPLE IS NOT NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL AND SAMPLE IS NULL AND INTEGER_ARRAY_VALUE IS NOT NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL AND SAMPLE IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NOT NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL AND SAMPLE IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NOT NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL AND SAMPLE IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NOT NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL AND SAMPLE IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NOT NULL)
    );



ALTER TABLE IF EXISTS DATA_SET_PROPERTIES
    ADD COLUMN INTEGER_ARRAY_VALUE LONG_VALUE[];

ALTER TABLE IF EXISTS DATA_SET_PROPERTIES
    ADD COLUMN REAL_ARRAY_VALUE DOUBLE_VALUE[];

ALTER TABLE IF EXISTS DATA_SET_PROPERTIES
    ADD COLUMN TIMESTAMP_ARRAY_VALUE TIME_STAMP[];

ALTER TABLE IF EXISTS DATA_SET_PROPERTIES
    ADD COLUMN STRING_ARRAY_VALUE text_value[];

ALTER TABLE IF EXISTS DATA_SET_PROPERTIES
    ADD COLUMN JSON_VALUE jsonb;

ALTER TABLE DATA_SET_PROPERTIES DROP CONSTRAINT DSPR_CK;
ALTER TABLE DATA_SET_PROPERTIES ADD CONSTRAINT DSPR_CK CHECK
    ((VALUE IS NOT NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NOT NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NOT NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NOT NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NOT NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NOT NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NOT NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NOT NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NOT NULL)
    );



ALTER TABLE IF EXISTS DATA_SET_PROPERTIES_HISTORY
    ADD COLUMN INTEGER_ARRAY_VALUE LONG_VALUE[];

ALTER TABLE IF EXISTS DATA_SET_PROPERTIES_HISTORY
    ADD COLUMN REAL_ARRAY_VALUE DOUBLE_VALUE[];

ALTER TABLE IF EXISTS DATA_SET_PROPERTIES_HISTORY
    ADD COLUMN TIMESTAMP_ARRAY_VALUE TIME_STAMP[];

ALTER TABLE IF EXISTS DATA_SET_PROPERTIES_HISTORY
    ADD COLUMN STRING_ARRAY_VALUE text_value[];

ALTER TABLE IF EXISTS DATA_SET_PROPERTIES_HISTORY
    ADD COLUMN JSON_VALUE jsonb;

ALTER TABLE DATA_SET_PROPERTIES_HISTORY DROP CONSTRAINT DSPRH_CK;
ALTER TABLE DATA_SET_PROPERTIES_HISTORY ADD CONSTRAINT DSPRH_CK CHECK
    ((VALUE IS NOT NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL AND SAMPLE IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NOT NULL AND MATERIAL IS NULL AND SAMPLE IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NOT NULL AND SAMPLE IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL AND SAMPLE IS NOT NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL AND SAMPLE IS NULL AND INTEGER_ARRAY_VALUE IS NOT NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL AND SAMPLE IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NOT NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL AND SAMPLE IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NOT NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL AND SAMPLE IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NOT NULL AND JSON_VALUE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL AND SAMPLE IS NULL AND INTEGER_ARRAY_VALUE IS NULL AND REAL_ARRAY_VALUE IS NULL AND TIMESTAMP_ARRAY_VALUE IS NULL AND STRING_ARRAY_VALUE IS NULL AND JSON_VALUE IS NOT NULL)
    );





CREATE OR REPLACE VIEW sample_history_view AS (
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


CREATE OR REPLACE VIEW data_set_history_view AS (
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

CREATE OR REPLACE VIEW experiment_history_view AS (
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

-- functions.sql

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

-- data.sql
insert into data_types
(id
,code
,description)
values
(nextval('DATA_TYPE_ID_SEQ')
,'JSON'
,'Json value'
);

insert into data_types
(id
,code
,description)
values
(nextval('DATA_TYPE_ID_SEQ')
,'ARRAY_INTEGER'
,'Array of integer values'
);

insert into data_types
(id
,code
,description)
values
(nextval('DATA_TYPE_ID_SEQ')
,'ARRAY_REAL'
,'Array of floating point values'
);

insert into data_types
(id
,code
,description)
values
(nextval('DATA_TYPE_ID_SEQ')
,'ARRAY_STRING'
,'Array of string values'
);

insert into data_types
(id
,code
,description)
values
(nextval('DATA_TYPE_ID_SEQ')
,'ARRAY_TIMESTAMP'
,'Array of timestamp values'
);





