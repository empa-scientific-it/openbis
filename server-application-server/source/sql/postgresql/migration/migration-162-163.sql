ALTER TABLE content_copies ADD COLUMN git_repository_id TEXT_VALUE;
ALTER TABLE data_set_copies_history ADD COLUMN git_repository_id TEXT_VALUE;

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
      git_repository_id,
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
      NEW.git_repository_id,
      NEW.edms_id,
      NEW.pers_id_registerer,
      NEW.registration_timestamp);
  );

CREATE OR REPLACE FUNCTION content_copies_uniqueness_check()
  RETURNS trigger AS
$BODY$
BEGIN
  NEW.location_unique_check = NEW.data_id || ',' || 
                              NEW.edms_id || ',' ||
                              coalesce(NEW.path, '') || ',' || 
                              coalesce(NEW.git_commit_hash, '') || ',' || 
                              coalesce(NEW.git_repository_id, '') || ',' || 
                              coalesce(NEW.external_code, '');
  RETURN NEW;
END;
$BODY$
  LANGUAGE 'plpgsql';