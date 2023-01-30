-- Migration from 170 to 171

------------------------------------------------------------------------------------
--  Purpose:  add external dms fields to content copy history
------------------------------------------------------------------------------------

ALTER TABLE data_set_copies_history ADD COLUMN edms_code CODE;
ALTER TABLE data_set_copies_history ADD COLUMN edms_label TEXT_VALUE;
ALTER TABLE data_set_copies_history ADD COLUMN edms_address TEXT_VALUE;

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

-- create content copy history entry on external dms change
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
