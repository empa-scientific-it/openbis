-- revert enabling of project-sample feature

CREATE TRIGGER disable_project_level_samples
  BEFORE INSERT OR UPDATE
  ON samples_all
  FOR EACH ROW
  EXECUTE PROCEDURE disable_project_level_samples();

DROP TRIGGER check_project_is_defined_for_experiment_level_samples ON samples_all;
DROP FUNCTION check_project_is_defined_for_experiment_level_samples;

DELETE FROM sample_properties WHERE samp_id in (SELECT id FROM samples_all WHERE sample_identifier = '/ELN_SETTINGS/TEMPLATES/ORDER_TEMPLATE');
DELETE FROM samples_all WHERE sample_identifier = '/ELN_SETTINGS/TEMPLATES/ORDER_TEMPLATE';
UPDATE samples_all SET proj_id = NULL;
