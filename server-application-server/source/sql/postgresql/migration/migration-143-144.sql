--
-- Link Data Set Experiment no longer mandatory. Data Sets link to an Experiment or a Sample with Space 
--

ALTER TABLE DATA_ALL ALTER COLUMN EXPE_ID DROP NOT NULL;
ALTER TABLE DATA_ALL ADD CONSTRAINT DATA_CK CHECK (EXPE_ID IS NOT NULL OR SAMP_ID IS NOT NULL);

CREATE OR REPLACE FUNCTION check_created_or_modified_data_set_owner_is_alive() RETURNS trigger AS $$
DECLARE
  owner_code  CODE;
  owner_del_id  TECH_ID;
BEGIN
  IF (NEW.del_id IS NOT NULL) THEN
    RETURN NEW;
  END IF;

  -- check sample
  IF (NEW.samp_id IS NOT NULL) THEN
    SELECT del_id, code INTO owner_del_id, owner_code
      FROM samples 
      WHERE id = NEW.samp_id;
    IF (owner_del_id IS NOT NULL) THEN 
      RAISE EXCEPTION 'Data Set (Code: %) cannot be connected to a Sample (Code: %) %.', 
                      NEW.code, owner_code, deletion_description(owner_del_id);
    END IF;
  END IF;
  -- check experiment
  IF (NEW.expe_id IS NOT NULL) THEN
    SELECT del_id, code INTO owner_del_id, owner_code
      FROM experiments 
      WHERE id = NEW.expe_id;
    IF (owner_del_id IS NOT NULL) THEN 
      RAISE EXCEPTION 'Data Set (Code: %) cannot be connected to an Experiment (Code: %) %.', 
                      NEW.code, owner_code, deletion_description(owner_del_id);
    END IF; 
  END IF; 
  RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

---------------------------------------------------------------------------------------
--  Purpose: trigger for data sets: They should be linked to an experiment or a sample with space
---------------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION data_exp_or_sample_link_check() RETURNS trigger AS $$
DECLARE
  space_id CODE;
  sample_code CODE;
BEGIN
  if NEW.expe_id IS NOT NULL then
    RETURN NEW;
  end if;
  if NEW.samp_id IS NULL then
    RAISE EXCEPTION 'Neither experiment nor sample is specified for data set %', NEW.code;
  end if;
  select s.id, s.code into space_id, sample_code from samples_all s where s.id = NEW.samp_id;
  if space_id is NULL then
    RAISE EXCEPTION 'Sample % is a shared sample.', sample_code;
  end if;
  RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER data_exp_or_sample_link_check BEFORE INSERT OR UPDATE ON data_all
FOR EACH ROW EXECUTE PROCEDURE data_exp_or_sample_link_check();

