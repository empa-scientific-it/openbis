-- Migration from 078 to 079

--
--  Allow to storing multiple entity deletions in the same event row
--
ALTER TABLE events RENAME identifier TO identifiers;
ALTER TABLE events ALTER COLUMN identifiers TYPE TEXT_VALUE;
ALTER TABLE events ALTER COLUMN description TYPE TEXT_VALUE;

DROP TRIGGER IF EXISTS check_deletion_consistency_on_experiment_deletion ON experiments_all;
DROP TRIGGER IF EXISTS check_deletion_consistency_on_sample_deletion ON samples_all;
DROP TRIGGER IF EXISTS check_created_or_modified_sample_owner_is_alive ON samples_all;
DROP TRIGGER IF EXISTS check_created_or_modified_data_set_owner_is_alive ON data_all;

----------------------------------------------------------------------------------------------------
-- Purpose: Create DEFERRED triggers for checking consistency of deletion state.
----------------------------------------------------------------------------------------------------
-- utility function describing a deletion

CREATE OR REPLACE FUNCTION deletion_description(del_id TECH_ID) RETURNS VARCHAR AS $$
DECLARE
  del_person VARCHAR;
  del_date VARCHAR;
  del_reason VARCHAR;
BEGIN
  SELECT p.last_name || ' ' || p.first_name || ' (' || p.email || ')', 
         to_char(d.registration_timestamp, 'YYYY-MM-DD HH:MM:SS'), d.reason 
    INTO del_person, del_date, del_reason FROM deletions d, persons p 
    WHERE d.pers_id_registerer = p.id AND d.id = del_id;
  RETURN 'deleted by ' || del_person || ' on ' || del_date || ' with reason: "' || del_reason || '"';
END;
$$ LANGUAGE 'plpgsql';

----------------------------------------------------------------------------------------------------
-- 1. data set
--- on insert/update - experiment, sample can't be deleted unless the data set is delete
---                  - parents/children relationship stays unchanged 

CREATE OR REPLACE FUNCTION check_created_or_modified_data_set_owner_is_alive() RETURNS trigger AS $$
DECLARE
	owner_code	CODE;
	owner_del_id	TECH_ID;
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
	SELECT del_id, code INTO owner_del_id, owner_code
    FROM experiments 
    WHERE id = NEW.expe_id;
  IF (owner_del_id IS NOT NULL) THEN 
		RAISE EXCEPTION 'Data Set (Code: %) cannot be connected to an Experiment (Code: %) %.', 
		                NEW.code, owner_code, deletion_description(owner_del_id);
	END IF;	
	RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';
  
CREATE CONSTRAINT TRIGGER check_created_or_modified_data_set_owner_is_alive 
	AFTER INSERT OR UPDATE ON data_all
	DEFERRABLE INITIALLY DEFERRED
	FOR EACH ROW
	EXECUTE PROCEDURE check_created_or_modified_data_set_owner_is_alive();
	
----------------------------------------------------------------------------------------------------
-- 2. sample
--- on insert/update -> experiment can't be deleted unless the sample is deleted
--- deletion 
----> all directly connected data sets need to be deleted
----> all components and children need to be deleted

CREATE OR REPLACE FUNCTION check_created_or_modified_sample_owner_is_alive() RETURNS trigger AS $$
DECLARE
	owner_code	CODE;
	owner_del_id	TECH_ID;
BEGIN
	IF (NEW.del_id IS NOT NULL) THEN
		RETURN NEW;
	END IF;

  -- check experiment (can't be deleted)
  IF (NEW.expe_id IS NOT NULL) THEN
  	SELECT del_id, code INTO owner_del_id, owner_code
  	  FROM experiments 
  	  WHERE id = NEW.expe_id;
  	IF (owner_del_id IS NOT NULL) THEN 
			RAISE EXCEPTION 'Sample (Code: %) cannot be connected to an Experiment (Code: %) %.', 
   		                NEW.code, owner_code, deletion_description(owner_del_id);
		END IF;
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';
  
CREATE CONSTRAINT TRIGGER check_created_or_modified_sample_owner_is_alive 
  AFTER INSERT OR UPDATE ON samples_all
	DEFERRABLE INITIALLY DEFERRED
	FOR EACH ROW
	EXECUTE PROCEDURE check_created_or_modified_sample_owner_is_alive();
	
CREATE OR REPLACE FUNCTION check_deletion_consistency_on_sample_deletion() RETURNS trigger AS $$
DECLARE
  counter  INTEGER;
BEGIN
	IF (OLD.del_id IS NOT NULL OR NEW.del_id IS NULL) THEN
		RETURN NEW;
	END IF;

  -- all directly connected data sets need to be deleted
  -- check datasets
	SELECT count(*) INTO counter 
	  FROM data
	  WHERE data.samp_id = NEW.id AND data.del_id IS NULL;
	IF (counter > 0) THEN
	  RAISE EXCEPTION 'Sample (Code: %) deletion failed because at least one of its data sets was not deleted.', NEW.code;
	END IF;
  -- all components need to be deleted
	SELECT count(*) INTO counter 
	  FROM samples 
	  WHERE samples.samp_id_part_of = NEW.id AND samples.del_id IS NULL;
	IF (counter > 0) THEN
	  RAISE EXCEPTION 'Sample (Code: %) deletion failed because at least one of its component samples was not deleted.', NEW.code;
	END IF;
	-- all children need to be deleted
	SELECT count(*) INTO counter 
		FROM sample_relationships sr, samples sc
		WHERE sample_id_parent = NEW.id AND sc.id = sr.sample_id_child AND sc.del_id IS NULL;
	IF (counter > 0) THEN
		RAISE EXCEPTION 'Sample (Code: %) deletion failed because at least one of its child samples was not deleted.', NEW.code;
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';
  
CREATE CONSTRAINT TRIGGER check_deletion_consistency_on_sample_deletion 
  AFTER UPDATE ON samples_all
	DEFERRABLE INITIALLY DEFERRED
	FOR EACH ROW 
	EXECUTE PROCEDURE check_deletion_consistency_on_sample_deletion();	
	
----------------------------------------------------------------------------------------------------
-- 3. experiment
--- deletion -> all directly connected samples and data sets need to be deleted

CREATE OR REPLACE FUNCTION check_deletion_consistency_on_experiment_deletion() RETURNS trigger AS $$
DECLARE
  counter  INTEGER;
BEGIN
	IF (OLD.del_id IS NOT NULL OR NEW.del_id IS NULL) THEN
		RETURN NEW;
	END IF;
	
  -- check datasets
	SELECT count(*) INTO counter 
	  FROM data
	  WHERE data.expe_id = NEW.id AND data.del_id IS NULL;
	IF (counter > 0) THEN
	  RAISE EXCEPTION 'Experiment (Code: %) deletion failed because at least one of its data sets was not deleted.', NEW.code;
	END IF;
	-- check samples
	SELECT count(*) INTO counter 
	  FROM samples 
	  WHERE samples.expe_id = NEW.id AND samples.del_id IS NULL;
	IF (counter > 0) THEN
	  RAISE EXCEPTION 'Experiment (Code: %) deletion failed because at least one of its samples was not deleted.', NEW.code;
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE CONSTRAINT TRIGGER check_deletion_consistency_on_experiment_deletion 
  AFTER UPDATE ON experiments_all
	DEFERRABLE INITIALLY DEFERRED
	FOR EACH ROW 
	EXECUTE PROCEDURE check_deletion_consistency_on_experiment_deletion();