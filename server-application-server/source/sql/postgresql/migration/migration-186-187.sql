-- Add column ENTITY_KIND to relationships history tables and populate it with existing data (if possible).
-- Add column ENTITY_KIND to history views

ALTER TABLE DATA_SET_RELATIONSHIPS_HISTORY ADD COLUMN IF NOT EXISTS ENTITY_KIND text_value;

UPDATE DATA_SET_RELATIONSHIPS_HISTORY
    SET ENTITY_KIND = CASE WHEN EXPE_ID IS NOT NULL THEN 'EXPERIMENT'
                           WHEN SAMP_ID IS NOT NULL THEN 'SAMPLE'
                           WHEN DATA_ID IS NOT NULL THEN 'DATA SET' END;

DROP VIEW data_set_history_view;
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
    valid_until_timestamp
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
    valid_until_timestamp
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
    valid_until_timestamp
  FROM
    data_set_copies_history
  WHERE
    valid_until_timestamp IS NOT NULL);


ALTER TABLE EXPERIMENT_RELATIONSHIPS_HISTORY ADD COLUMN IF NOT EXISTS ENTITY_KIND text_value;

UPDATE EXPERIMENT_RELATIONSHIPS_HISTORY
    SET ENTITY_KIND = CASE WHEN PROJ_ID IS NOT NULL THEN 'PROJECT'
                           WHEN SAMP_ID IS NOT NULL THEN 'SAMPLE'
                           WHEN DATA_ID IS NOT NULL THEN 'DATA SET' END;

DROP VIEW experiment_history_view;
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
    valid_until_timestamp
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
    valid_until_timestamp
  FROM
    EXPERIMENT_PROPERTIES_HISTORY;


ALTER TABLE PROJECT_RELATIONSHIPS_HISTORY ADD COLUMN IF NOT EXISTS ENTITY_KIND text_value;
ALTER TABLE PROJECT_RELATIONSHIPS_HISTORY ADD COLUMN IF NOT EXISTS SAMP_ID tech_id;
ALTER TABLE PROJECT_RELATIONSHIPS_HISTORY ADD CONSTRAINT PRRELH_SAMP_FK FOREIGN KEY (SAMP_ID) REFERENCES SAMPLES_ALL(ID) ON DELETE SET NULL;
CREATE INDEX PRRELH_MAIN_PROJ_FK_SAMP_FK_I ON PROJECT_RELATIONSHIPS_HISTORY (MAIN_PROJ_ID, SAMP_ID);

UPDATE PROJECT_RELATIONSHIPS_HISTORY
    SET ENTITY_KIND = CASE WHEN EXPE_ID IS NOT NULL THEN 'EXPERIMENT'
                           WHEN SAMP_ID IS NOT NULL THEN 'SAMPLE'
                           WHEN SPACE_ID IS NOT NULL THEN 'SPACE' END;


ALTER TABLE SAMPLE_RELATIONSHIPS_HISTORY ADD COLUMN IF NOT EXISTS ENTITY_KIND text_value;

UPDATE SAMPLE_RELATIONSHIPS_HISTORY
    SET ENTITY_KIND = CASE WHEN PROJ_ID IS NOT NULL THEN 'PROJECT'
                           WHEN EXPE_ID IS NOT NULL THEN 'EXPERIMENT'
                           WHEN SAMP_ID IS NOT NULL THEN 'SAMPLE'
                           WHEN DATA_ID IS NOT NULL THEN 'DATA SET'
                           WHEN SPACE_ID IS NOT NULL THEN 'SPACE' END;

DROP VIEW sample_history_view;
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
    valid_until_timestamp
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
    valid_until_timestamp
  FROM
    SAMPLE_PROPERTIES_HISTORY;

----------------------------------------------------------------------------------------------------
-- Rules for relationships history
----------------------------------------------------------------------------------------------------

-- sample -> experiment

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

-- container samples

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

-- dataset -> eperiment

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

-- dataset -> sample

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

-- data set relationship

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



-- samples parent-child relationship

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

-- experiment -> project

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

-- project -> space

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

-- sample -> project

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

-- rules for shared samples

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
