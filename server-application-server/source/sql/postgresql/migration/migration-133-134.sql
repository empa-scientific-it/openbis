-- Migration from 133 to 134

DROP VIEW IF EXISTS sample_history_view;
CREATE OR REPLACE VIEW sample_history_view AS (
  SELECT
    2*id as id,
    main_samp_id,
    relation_type,
    space_id,
    expe_id,
    samp_id,
    data_id,
    entity_perm_id,
    null as stpt_id,
    null as value,
    null as vocabulary_term,
    null as material,
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
    null as data_id,
    null as entity_perm_id,
    stpt_id,
    value,
    vocabulary_term,
    material,
    pers_id_author,
    valid_from_timestamp,
    valid_until_timestamp
  FROM
    SAMPLE_PROPERTIES_HISTORY;


DROP VIEW IF EXISTS data_set_history_view;
CREATE OR REPLACE VIEW data_set_history_view AS (
  SELECT
    2*id as id,
    main_data_id,
    relation_type,
    expe_id,
    samp_id,
    data_id,
    entity_perm_id,
    null as dstpt_id,
    null as value,
    null as vocabulary_term,
    null as material,
    pers_id_author,
    valid_from_timestamp,
    valid_until_timestamp
  FROM
    DATA_SET_RELATIONSHIPS_HISTORY
  WHERE
    valid_until_timestamp IS NOT NULL)
UNION
  SELECT
    2*id+1 as id,
    ds_id as main_data_id,
    null as relation_type,
    null as expe_id,
    null as samp_id,
    null as data_id,
    null as entity_perm_id,
    dstpt_id,
    value,
    vocabulary_term,
    material,
    pers_id_author,
    valid_from_timestamp,
    valid_until_timestamp
  FROM
    DATA_SET_PROPERTIES_HISTORY;


DROP VIEW IF EXISTS experiment_history_view;
CREATE OR REPLACE VIEW experiment_history_view AS (
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
    pers_id_author,
    valid_from_timestamp,
    valid_until_timestamp
  FROM
    EXPERIMENT_PROPERTIES_HISTORY;

GRANT SELECT ON sample_history_view TO GROUP OPENBIS_READONLY;
GRANT SELECT ON data_set_history_view TO GROUP OPENBIS_READONLY;
GRANT SELECT ON experiment_history_view TO GROUP OPENBIS_READONLY;
