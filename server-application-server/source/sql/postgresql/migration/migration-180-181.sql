DROP VIEW sample_relationships;

ALTER TABLE sample_relationships_all
    ADD COLUMN child_annotations jsonb,
    ADD COLUMN parent_annotations jsonb;

CREATE VIEW sample_relationships AS
    SELECT sample_relationships_all.id, sample_relationships_all.sample_id_parent, sample_relationships_all.parent_frozen, sample_relationships_all.relationship_id, sample_relationships_all.sample_id_child, sample_relationships_all.child_frozen, sample_relationships_all.del_id, sample_relationships_all.pers_id_author, sample_relationships_all.registration_timestamp, sample_relationships_all.modification_timestamp, sample_relationships_all.child_annotations, sample_relationships_all.parent_annotations FROM public.sample_relationships_all WHERE (sample_relationships_all.del_id IS NULL);


DROP VIEW sample_history_view;

ALTER TABLE sample_relationships_history
    ADD COLUMN annotations jsonb;

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

CREATE OR REPLACE RULE sample_relationships_insert AS
    ON INSERT TO sample_relationships DO INSTEAD 
       INSERT INTO sample_relationships_all (
         id, 
         sample_id_parent,
         parent_frozen,
         relationship_id,
         sample_id_child,
         child_frozen,
         pers_id_author,
         registration_timestamp,
         modification_timestamp,
         child_annotations,
         parent_annotations
       ) VALUES (
         NEW.id, 
         NEW.sample_id_parent,
         NEW.parent_frozen,
         NEW.relationship_id,
         NEW.sample_id_child,
         NEW.child_frozen,
         NEW.pers_id_author,
         NEW.registration_timestamp,
         NEW.modification_timestamp,
         NEW.child_annotations,
         NEW.parent_annotations
       );

CREATE OR REPLACE RULE sample_relationships_update AS
    ON UPDATE TO sample_relationships DO INSTEAD 
       UPDATE sample_relationships_all
          SET 
             sample_id_parent = NEW.sample_id_parent,
             parent_frozen = NEW.parent_frozen,
             relationship_id = NEW.relationship_id,
             sample_id_child = NEW.sample_id_child,
             child_frozen = NEW.child_frozen,
             del_id = NEW.del_id,
             pers_id_author = NEW.pers_id_author,
             registration_timestamp = NEW.registration_timestamp,
             modification_timestamp = NEW.modification_timestamp,
             child_annotations = NEW.child_annotations,
             parent_annotations = NEW.parent_annotations
          WHERE id = NEW.id;

CREATE OR REPLACE RULE sample_relationships_delete AS
    ON DELETE TO sample_relationships DO INSTEAD
       DELETE FROM sample_relationships_all
              WHERE id = OLD.id;

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
           ANNOTATIONS,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP
         ) VALUES (
           nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.SAMPLE_ID_PARENT, 
           'PARENT', 
           NEW.SAMPLE_ID_CHILD, 
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
           ENTITY_PERM_ID,
           ANNOTATIONS,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP
         ) VALUES (
           nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.SAMPLE_ID_CHILD, 
           'CHILD', 
           NEW.SAMPLE_ID_PARENT, 
           (SELECT PERM_ID FROM samples_all WHERE ID = NEW.SAMPLE_ID_PARENT),
           NEW.CHILD_ANNOTATIONS,
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP
         );
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
           ENTITY_PERM_ID,
           ANNOTATIONS,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP
         ) VALUES (
           nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.SAMPLE_ID_PARENT, 
           'PARENT', 
           NEW.SAMPLE_ID_CHILD, 
           (SELECT PERM_ID FROM samples_all WHERE ID = NEW.SAMPLE_ID_CHILD),
           OLD.PARENT_ANNOTATIONS,
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP
         );
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
           ANNOTATIONS,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP
         ) VALUES (
           nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.SAMPLE_ID_PARENT, 
           'PARENT', 
           NEW.SAMPLE_ID_CHILD, 
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
           ENTITY_PERM_ID,
           ANNOTATIONS,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP
         ) VALUES (
           nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.SAMPLE_ID_CHILD, 
           'CHILD', 
           NEW.SAMPLE_ID_PARENT, 
           (SELECT PERM_ID FROM samples_all WHERE ID = NEW.SAMPLE_ID_PARENT),
           NEW.CHILD_ANNOTATIONS,
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP
         );
       );
