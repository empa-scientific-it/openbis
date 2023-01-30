ALTER TABLE content_copies ADD COLUMN pers_id_registerer TECH_ID;
ALTER TABLE content_copies ADD COLUMN registration_timestamp TIME_STAMP_DFL;

UPDATE content_copies 
SET pers_id_registerer = x.pers_id_registerer,
    registration_timestamp = x.registration_timestamp
FROM (
  SELECT id, pers_id_registerer, registration_timestamp 
  FROM data
) x
WHERE content_copies.data_id = x.id;

ALTER TABLE content_copies ALTER COLUMN registration_timestamp SET NOT NULL;
ALTER TABLE content_copies ALTER COLUMN registration_timestamp SET DEFAULT CURRENT_TIMESTAMP;


CREATE TABLE data_set_copies_history (
  id TECH_ID NOT NULL,
  cc_id TECH_ID NOT NULL,
  data_id TECH_ID NOT NULL,
  external_code TEXT_VALUE,
  path TEXT_VALUE,
  git_commit_hash TEXT_VALUE, 
  edms_id TECH_ID NOT NULL,
  pers_id_author TECH_ID,
  valid_from_timestamp TIME_STAMP NOT NULL, 
  valid_until_timestamp TIME_STAMP);

ALTER TABLE data_set_copies_history ADD CONSTRAINT dsch_pk PRIMARY KEY(id);
  
CREATE SEQUENCE data_set_copies_history_id_seq;
  
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
    entity_perm_id,
    null as dstpt_id,
    null as value,
    null as vocabulary_term,
    null as material,
    null as external_code,
    null as path,
    null as git_commit_hash,
    null::TECH_ID as edms_id,
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
    null as external_code,
    null as path,
    null as git_commit_hash,
    null as edms_id,
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
    external_code,
    path,
    git_commit_hash,
    edms_id,
    pers_id_author,
    valid_from_timestamp,
    valid_until_timestamp
  FROM
    data_set_copies_history
  WHERE
    valid_until_timestamp IS NOT NULL);
    
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
      edms_id,
      pers_id_author,
      valid_from_timestamp
    ) VALUES (
      nextval('data_set_copies_history_id_seq'), 
      NEW.id,
      NEW.data_id, 
      NEW.external_code,
      NEW.path,
      NEW.git_commit_hash,
      NEW.edms_id,
      NEW.pers_id_registerer,
      NEW.registration_timestamp);
  );

CREATE OR REPLACE RULE content_copies_history_delete AS
  ON DELETE TO content_copies
  DO ALSO (
    UPDATE data_set_copies_history SET valid_until_timestamp = CURRENT_TIMESTAMP
    WHERE cc_id = OLD.id;
  );


  