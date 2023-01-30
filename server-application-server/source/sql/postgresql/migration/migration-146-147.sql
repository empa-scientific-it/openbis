ALTER TABLE samples_all ADD COLUMN proj_id TECH_ID;
DROP VIEW samples;
CREATE VIEW samples AS
    SELECT id, perm_id, code, proj_id, expe_id, saty_id, registration_timestamp, modification_timestamp, pers_id_registerer, pers_id_modifier, del_id, orig_del, space_id, samp_id_part_of, version 
    FROM samples_all 
    WHERE del_id IS NULL;


CREATE OR REPLACE FUNCTION sample_fill_code_unique_check()
  RETURNS trigger AS
$BODY$
BEGIN
  NEW.code_unique_check = NEW.code || ',' || coalesce(NEW.samp_id_part_of, -1) || ',' || coalesce(NEW.proj_id, -1) || ',' || coalesce(NEW.space_id, -1);
  RETURN NEW;
END;
$BODY$
  LANGUAGE 'plpgsql';

  
  
CREATE OR REPLACE FUNCTION sample_fill_subcode_unique_check()
  RETURNS trigger AS
$BODY$
DECLARE
    unique_subcode  BOOLEAN_CHAR;
BEGIN
    SELECT is_subcode_unique into unique_subcode FROM sample_types WHERE id = NEW.saty_id;
    
    IF (unique_subcode) THEN
    NEW.subcode_unique_check = NEW.code || ',' || coalesce(NEW.saty_id, -1) || ',' || coalesce(NEW.proj_id, -1) || ',' || coalesce(NEW.space_id, -1);
    ELSE
    NEW.subcode_unique_check = NULL;
  END IF;
  
  RETURN NEW;
END;
$BODY$
  LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION disable_project_level_samples()
  RETURNS trigger AS
$BODY$
BEGIN
    IF (NEW.proj_id IS NOT NULL) THEN
    RAISE EXCEPTION 'Project level samples are disabled';
  END IF;
  
  RETURN NEW;
END;
$BODY$
  LANGUAGE 'plpgsql';
  
CREATE TRIGGER disable_project_level_samples
  BEFORE INSERT OR UPDATE
  ON samples_all
  FOR EACH ROW
  EXECUTE PROCEDURE disable_project_level_samples();
    
