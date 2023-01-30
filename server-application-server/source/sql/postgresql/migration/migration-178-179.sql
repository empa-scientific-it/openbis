-- add data type SAMPLE
insert into data_types
(id
 ,code
 ,description)
 values 
 (nextval('DATA_TYPE_ID_SEQ')
 ,'SAMPLE'
 ,'Reference to a sample'
);

-- add sample type FK to property types
alter table PROPERTY_TYPES add column SATY_PROP_ID TECH_ID;
ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_SATY_FK FOREIGN KEY (SATY_PROP_ID) REFERENCES SAMPLE_TYPES(ID) ON DELETE CASCADE;

-- add sample FK to DATA SET properties
alter table DATA_SET_PROPERTIES add column SAMP_PROP_ID TECH_ID;
alter table DATA_SET_PROPERTIES drop constraint DSPR_CK;
ALTER TABLE DATA_SET_PROPERTIES ADD CONSTRAINT DSPR_CK CHECK
    ((VALUE IS NOT NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL) OR 
     (VALUE IS NULL AND CVTE_ID IS NOT NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NOT NULL AND SAMP_PROP_ID IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NOT NULL)
    );
CREATE INDEX DSPR_SAPR_FK_I ON DATA_SET_PROPERTIES (SAMP_PROP_ID);

-- add sample identifier to DATA SET properties history
alter table DATA_SET_PROPERTIES_HISTORY add column SAMPLE IDENTIFIER;
alter table DATA_SET_PROPERTIES_HISTORY drop constraint DSPRH_CK;
ALTER TABLE DATA_SET_PROPERTIES_HISTORY ADD CONSTRAINT DSPRH_CK CHECK
    ((VALUE IS NOT NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL AND SAMPLE IS NULL) OR 
     (VALUE IS NULL AND VOCABULARY_TERM IS NOT NULL AND MATERIAL IS NULL AND SAMPLE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NOT NULL AND SAMPLE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL AND SAMPLE IS NOT NULL)
    );

drop view data_set_history_view;
CREATE VIEW data_set_history_view AS (
  SELECT
    3*id as id,
    main_data_id,
    relation_type,
    ordinal,
    expe_id,
    samp_id,
    data_id,
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

CREATE OR REPLACE RULE data_set_properties_update AS
    ON UPDATE TO data_set_properties 
    WHERE (OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd' AND OLD.VALUE != NEW.VALUE) 
        OR (OLD.CVTE_ID IS NOT NULL AND OLD.CVTE_ID != NEW.CVTE_ID) 
        OR (OLD.MATE_PROP_ID IS NOT NULL AND OLD.MATE_PROP_ID != NEW.MATE_PROP_ID)
        OR (OLD.SAMP_PROP_ID IS NOT NULL AND OLD.SAMP_PROP_ID != NEW.SAMP_PROP_ID)
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
         VALID_UNTIL_TIMESTAMP 
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
         NEW.MODIFICATION_TIMESTAMP
       );

CREATE OR REPLACE RULE data_set_properties_delete AS
    ON DELETE TO data_set_properties 
    WHERE ((OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd')
        OR OLD.CVTE_ID IS NOT NULL 
        OR OLD.MATE_PROP_ID IS NOT NULL
        OR OLD.SAMP_PROP_ID IS NOT NULL)
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
         VALID_UNTIL_TIMESTAMP 
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
         current_timestamp
       );

-- add sample FK to EXPERIMENT properties
alter table EXPERIMENT_PROPERTIES add column SAMP_PROP_ID TECH_ID;
alter table EXPERIMENT_PROPERTIES drop constraint EXPR_CK;
ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_CK CHECK
    ((VALUE IS NOT NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL) OR 
     (VALUE IS NULL AND CVTE_ID IS NOT NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NOT NULL AND SAMP_PROP_ID IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NOT NULL)
    );
CREATE INDEX EXPR_SAPR_FK_I ON EXPERIMENT_PROPERTIES (SAMP_PROP_ID);

-- add sample identifier to EXPERIMENT properties history
alter table EXPERIMENT_PROPERTIES_HISTORY add column SAMPLE IDENTIFIER;
alter table EXPERIMENT_PROPERTIES_HISTORY drop constraint EXPRH_CK;
ALTER TABLE EXPERIMENT_PROPERTIES_HISTORY ADD CONSTRAINT EXPRH_CK CHECK
    ((VALUE IS NOT NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL AND SAMPLE IS NULL) OR 
     (VALUE IS NULL AND VOCABULARY_TERM IS NOT NULL AND MATERIAL IS NULL AND SAMPLE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NOT NULL AND SAMPLE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL AND SAMPLE IS NOT NULL)
    );

drop view experiment_history_view;
CREATE VIEW experiment_history_view AS (
  SELECT
    2*id as id,
    main_expe_id,
    relation_type,
    proj_id,
    samp_id,
    data_id,
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

CREATE OR REPLACE RULE experiment_properties_update AS
    ON UPDATE TO experiment_properties 
    WHERE (OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd' AND OLD.VALUE != NEW.VALUE) 
        OR (OLD.CVTE_ID IS NOT NULL AND OLD.CVTE_ID != NEW.CVTE_ID) 
        OR (OLD.MATE_PROP_ID IS NOT NULL AND OLD.MATE_PROP_ID != NEW.MATE_PROP_ID)
        OR (OLD.SAMP_PROP_ID IS NOT NULL AND OLD.SAMP_PROP_ID != NEW.SAMP_PROP_ID)
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
         VALID_UNTIL_TIMESTAMP 
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
         NEW.MODIFICATION_TIMESTAMP
       );

CREATE OR REPLACE RULE experiment_properties_delete AS
    ON DELETE TO experiment_properties 
    WHERE (OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd')
        OR OLD.CVTE_ID IS NOT NULL 
        OR OLD.MATE_PROP_ID IS NOT NULL
        OR OLD.SAMP_PROP_ID IS NOT NULL
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
         VALID_UNTIL_TIMESTAMP 
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
         current_timestamp
       );

-- add sample FK to SAMPLE properties
alter table SAMPLE_PROPERTIES add column SAMP_PROP_ID TECH_ID;
alter table SAMPLE_PROPERTIES drop constraint SAPR_CK;
ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_CK CHECK 
    ((VALUE IS NOT NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NOT NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NOT NULL AND SAMP_PROP_ID IS NULL) OR
     (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL AND SAMP_PROP_ID IS NOT NULL)
    );
CREATE INDEX SAPR_SAPR_FK_I ON SAMPLE_PROPERTIES (SAMP_PROP_ID);

-- add sample identifier to SAMPLE properties history
alter table SAMPLE_PROPERTIES_HISTORY add column SAMPLE IDENTIFIER;
alter table SAMPLE_PROPERTIES_HISTORY drop constraint SAPRH_CK;
ALTER TABLE SAMPLE_PROPERTIES_HISTORY ADD CONSTRAINT SAPRH_CK CHECK 
    ((VALUE IS NOT NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL AND SAMPLE IS NULL) OR 
     (VALUE IS NULL AND VOCABULARY_TERM IS NOT NULL AND MATERIAL IS NULL AND SAMPLE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NOT NULL AND SAMPLE IS NULL) OR
     (VALUE IS NULL AND VOCABULARY_TERM IS NULL AND MATERIAL IS NULL AND SAMPLE IS NOT NULL)
    );

drop view sample_history_view;
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
    entity_perm_id,
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
    null as entity_perm_id,
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

CREATE OR REPLACE RULE sample_properties_update AS
    ON UPDATE TO sample_properties
    WHERE (OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd' AND OLD.VALUE != NEW.VALUE) 
        OR (OLD.CVTE_ID IS NOT NULL AND OLD.CVTE_ID != NEW.CVTE_ID)
        OR (OLD.MATE_PROP_ID IS NOT NULL AND OLD.MATE_PROP_ID != NEW.MATE_PROP_ID)
        OR (OLD.SAMP_PROP_ID IS NOT NULL AND OLD.SAMP_PROP_ID != NEW.SAMP_PROP_ID)
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
         VALID_UNTIL_TIMESTAMP 
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
         NEW.MODIFICATION_TIMESTAMP
       );
CREATE OR REPLACE RULE sample_properties_delete AS
    ON DELETE TO sample_properties 
    WHERE ((OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd')
        OR OLD.CVTE_ID IS NOT NULL 
        OR OLD.MATE_PROP_ID IS NOT NULL
        OR OLD.SAMP_PROP_ID IS NOT NULL)
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
         VALID_UNTIL_TIMESTAMP 
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
         current_timestamp
       );

