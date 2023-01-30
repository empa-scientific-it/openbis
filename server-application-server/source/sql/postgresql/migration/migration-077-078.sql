-- Migration from 077 to 078

----------------------------------------------------------------------------------------------------
-- Purpose: Introduce views for samples/experiments/data that hold only non-deleted entities. 
--          We want the new views to have names of the old tables so the old tables need to be 
--          renamed. All trigger functions should work on the old tables, not the views.
-- NOTES: 
-- - views need to have explicitly specified list of columns because of PgDiffViews limitations
----------------------------------------------------------------------------------------------------

-------------
-- samples --
-------------

ALTER TABLE samples RENAME TO samples_all;

CREATE VIEW samples AS
     SELECT id, perm_id, code, expe_id, saty_id, registration_timestamp, modification_timestamp, pers_id_registerer, del_id, dbin_id, space_id, samp_id_part_of 
       FROM samples_all 
      WHERE del_id IS NULL;
GRANT SELECT ON samples TO GROUP openbis_readonly;

CREATE OR REPLACE RULE sample_insert AS
    ON INSERT TO samples DO INSTEAD 
       INSERT INTO samples_all (
         id, 
         code, 
         dbin_id,
         del_id,
         expe_id,
         modification_timestamp,
         perm_id,
         pers_id_registerer, 
         registration_timestamp, 
         samp_id_part_of,
         saty_id, 
         space_id
       ) VALUES (
         NEW.id, 
         NEW.code, 
         NEW.dbin_id,
         NEW.del_id,
         NEW.expe_id,
         NEW.modification_timestamp,
         NEW.perm_id,
         NEW.pers_id_registerer, 
         NEW.registration_timestamp, 
         NEW.samp_id_part_of,
         NEW.saty_id, 
         NEW.space_id
       );
     
CREATE OR REPLACE RULE sample_update AS
    ON UPDATE TO samples DO INSTEAD 
       UPDATE samples_all
          SET code = NEW.code,
              dbin_id = NEW.dbin_id,
              del_id = NEW.del_id,
              expe_id = NEW.expe_id,
              modification_timestamp = NEW.modification_timestamp,
              perm_id = NEW.perm_id,
              pers_id_registerer = NEW.pers_id_registerer,
              registration_timestamp = NEW.registration_timestamp,
              samp_id_part_of = NEW.samp_id_part_of,
              saty_id = NEW.saty_id,
              space_id = NEW.space_id
          WHERE id = NEW.id;
     
CREATE OR REPLACE RULE sample_delete AS
    ON DELETE TO samples DO INSTEAD
       DELETE FROM samples_all
              WHERE id = OLD.id;

CREATE VIEW samples_deleted AS
     SELECT id, perm_id, code, expe_id, saty_id, registration_timestamp, modification_timestamp, pers_id_registerer, del_id, dbin_id, space_id, samp_id_part_of 
       FROM samples_all 
      WHERE del_id IS NOT NULL;
GRANT SELECT ON samples_deleted TO GROUP openbis_readonly;

CREATE OR REPLACE RULE sample_deleted_update AS
    ON UPDATE TO samples_deleted DO INSTEAD 
       UPDATE samples_all
          SET del_id = NEW.del_id,
              modification_timestamp = NEW.modification_timestamp
          WHERE id = NEW.id;
     
CREATE OR REPLACE RULE sample_deleted_delete AS
    ON DELETE TO samples_deleted DO INSTEAD
       DELETE FROM samples_all
              WHERE id = OLD.id;

-----------------
-- experiments --
-----------------

ALTER TABLE experiments RENAME TO experiments_all;

CREATE VIEW experiments AS
     SELECT id, perm_id, code, exty_id, mate_id_study_object, pers_id_registerer, registration_timestamp, modification_timestamp, proj_id, del_id, is_public 
       FROM experiments_all 
      WHERE del_id IS NULL;
GRANT SELECT ON experiments TO GROUP openbis_readonly;

CREATE OR REPLACE RULE experiment_insert AS
  ON INSERT TO experiments DO INSTEAD 
     INSERT INTO experiments_all (
       id, 
       code, 
       del_id,
       exty_id, 
       is_public,
       mate_id_study_object,
       modification_timestamp,
       perm_id,
       pers_id_registerer, 
       proj_id,
       registration_timestamp
     ) VALUES (
       NEW.id, 
       NEW.code, 
       NEW.del_id,
       NEW.exty_id, 
       NEW.is_public,
       NEW.mate_id_study_object,
       NEW.modification_timestamp,
       NEW.perm_id,
       NEW.pers_id_registerer, 
       NEW.proj_id,
       NEW.registration_timestamp
     );
     
CREATE OR REPLACE RULE experiment_update AS
    ON UPDATE TO experiments DO INSTEAD 
       UPDATE experiments_all
          SET code = NEW.code,
              del_id = NEW.del_id,
              exty_id = NEW.exty_id,
              is_public = NEW.is_public,
              mate_id_study_object = NEW.mate_id_study_object,
              modification_timestamp = NEW.modification_timestamp,
              perm_id = NEW.perm_id,
              pers_id_registerer = NEW.pers_id_registerer,
              proj_id = NEW.proj_id,
              registration_timestamp = NEW.registration_timestamp
          WHERE id = NEW.id;
     
CREATE OR REPLACE RULE experiment_delete AS
    ON DELETE TO experiments DO INSTEAD
       DELETE FROM experiments_all
              WHERE id = OLD.id;
              
CREATE VIEW experiments_deleted AS
     SELECT id, perm_id, code, exty_id, mate_id_study_object, pers_id_registerer, registration_timestamp, modification_timestamp, proj_id, del_id, is_public 
       FROM experiments_all 
      WHERE del_id IS NOT NULL;
GRANT SELECT ON experiments_deleted TO GROUP openbis_readonly;              
              
CREATE OR REPLACE RULE experiments_deleted_update AS
    ON UPDATE TO experiments_deleted DO INSTEAD 
       UPDATE experiments_all
          SET del_id = NEW.del_id,
              modification_timestamp = NEW.modification_timestamp
          WHERE id = NEW.id;
     
CREATE OR REPLACE RULE experiments_deleted_delete AS
    ON DELETE TO experiments_deleted DO INSTEAD
       DELETE FROM experiments_all
              WHERE id = OLD.id;
              
----------
-- data --
----------

ALTER TABLE data RENAME TO data_all;

CREATE VIEW data AS
     SELECT id, code, dsty_id, dast_id, expe_id, data_producer_code, production_timestamp, samp_id, registration_timestamp, pers_id_registerer, is_placeholder, is_valid, modification_timestamp, is_derived, ctnr_order, ctnr_id, del_id 
       FROM data_all 
      WHERE del_id IS NULL;
GRANT SELECT ON data TO GROUP openbis_readonly;      
      
CREATE OR REPLACE RULE data_insert AS
  ON INSERT TO data DO INSTEAD 
     INSERT INTO data_all (
       id, 
       code, 
       ctnr_id,
       ctnr_order,
       del_id,
       expe_id,
       dast_id,
       data_producer_code,
       dsty_id,
       is_derived,
       is_placeholder,
       is_valid,
       modification_timestamp,
       pers_id_registerer,
       production_timestamp,
       registration_timestamp,
       samp_id
     ) VALUES (
       NEW.id, 
       NEW.code, 
       NEW.ctnr_id,
       NEW.ctnr_order,
       NEW.del_id, 
       NEW.expe_id,
       NEW.dast_id,
       NEW.data_producer_code,
       NEW.dsty_id,
       NEW.is_derived, 
       NEW.is_placeholder,
       NEW.is_valid,
       NEW.modification_timestamp,
       NEW.pers_id_registerer,
       NEW.production_timestamp,
       NEW.registration_timestamp,
       NEW.samp_id
     );
     
CREATE OR REPLACE RULE data_update AS
    ON UPDATE TO data DO INSTEAD 
       UPDATE data_all
          SET code = NEW.code,
              ctnr_id = NEW.ctnr_id,
              ctnr_order = NEW.ctnr_order,
              del_id = NEW.del_id,
              expe_id = NEW.expe_id,
              dast_id = NEW.dast_id,
              data_producer_code = NEW.data_producer_code,
              dsty_id = NEW.dsty_id,
              is_derived = NEW.is_derived,
              is_placeholder = NEW.is_placeholder,
              is_valid = NEW.is_valid,
              modification_timestamp = NEW.modification_timestamp,
              pers_id_registerer = NEW.pers_id_registerer,
              production_timestamp = NEW.production_timestamp,
              registration_timestamp = NEW.registration_timestamp,
              samp_id = NEW.samp_id
       WHERE id = NEW.id;
              
CREATE OR REPLACE RULE data_all AS
    ON DELETE TO data DO INSTEAD
       DELETE FROM data_all
              WHERE id = OLD.id;
              
CREATE VIEW data_deleted AS
     SELECT id, code, dsty_id, dast_id, expe_id, data_producer_code, production_timestamp, samp_id, registration_timestamp, pers_id_registerer, is_placeholder, is_valid, modification_timestamp, is_derived, ctnr_order, ctnr_id, del_id 
       FROM data_all 
      WHERE del_id IS NOT NULL;
GRANT SELECT ON data_deleted TO GROUP openbis_readonly;     

CREATE OR REPLACE RULE data_deleted_update AS
    ON UPDATE TO data_deleted DO INSTEAD 
       UPDATE data_all
          SET del_id = NEW.del_id,
              modification_timestamp = NEW.modification_timestamp
          WHERE id = NEW.id;
     
CREATE OR REPLACE RULE data_deleted_delete AS
    ON DELETE TO data_deleted DO INSTEAD
       DELETE FROM data_all
              WHERE id = OLD.id;               
              
-- update table names in old trigger functions 

CREATE OR REPLACE FUNCTION EXTERNAL_DATA_STORAGE_FORMAT_CHECK() RETURNS trigger AS $$
DECLARE
   v_covo_code  CODE;
   data_code CODE;
BEGIN

   select code into v_covo_code from controlled_vocabularies
      where is_internal_namespace = true and 
         id = (select covo_id from controlled_vocabulary_terms where id = NEW.cvte_id_stor_fmt);
   -- Check if the data storage format is a term of the controlled vocabulary "STORAGE_FORMAT"
   if v_covo_code != 'STORAGE_FORMAT' then
      select code into data_code from data_all where id = NEW.data_id; 
      RAISE EXCEPTION 'Insert/Update of Data (Code: %) failed, as its Storage Format is %, but is required to be STORAGE_FORMAT.', data_code, v_covo_code;
   end if;

   RETURN NEW;

END;
$$ LANGUAGE 'plpgsql';


CREATE OR REPLACE FUNCTION SAMPLE_CODE_UNIQUENESS_CHECK() RETURNS trigger AS $$
DECLARE
   counter  INTEGER;
BEGIN
  LOCK TABLE samples_all IN EXCLUSIVE MODE;
  
	  IF (NEW.samp_id_part_of is NULL) THEN
		  IF (NEW.dbin_id is not NULL) THEN
			  SELECT count(*) into counter FROM samples_all 
		      where id != NEW.id and code = NEW.code and samp_id_part_of is NULL and dbin_id = NEW.dbin_id;
        IF (counter > 0) THEN
				  RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because database instance sample with the same code already exists.', NEW.code;
        END IF;
		  ELSIF (NEW.space_id is not NULL) THEN
			  SELECT count(*) into counter FROM samples_all 
				  where id != NEW.id and code = NEW.code and samp_id_part_of is NULL and space_id = NEW.space_id;
			  IF (counter > 0) THEN
				  RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because space sample with the same code already exists.', NEW.code;
			  END IF;
      END IF;
    ELSE
		  IF (NEW.dbin_id is not NULL) THEN
			  SELECT count(*) into counter FROM samples_all 
				  where id != NEW.id and code = NEW.code and samp_id_part_of = NEW.samp_id_part_of and dbin_id = NEW.dbin_id;
			  IF (counter > 0) THEN
				  RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because database instance sample with the same code and being the part of the same container already exists.', NEW.code;
			  END IF;
		  ELSIF (NEW.space_id is not NULL) THEN
			  SELECT count(*) into counter FROM samples_all 
				  where id != NEW.id and code = NEW.code and samp_id_part_of = NEW.samp_id_part_of and space_id = NEW.space_id;
			  IF (counter > 0) THEN
				  RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because space sample with the same code and being the part of the same container already exists.', NEW.code;
			  END IF;
		  END IF;
     END IF;   
  
  RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';


CREATE OR REPLACE FUNCTION SAMPLE_SUBCODE_UNIQUENESS_CHECK() RETURNS trigger AS $$
DECLARE
   counter  INTEGER;
   unique_subcode  BOOLEAN_CHAR;
BEGIN
  LOCK TABLE samples_all IN EXCLUSIVE MODE;
  
  SELECT is_subcode_unique into unique_subcode FROM sample_types WHERE id = NEW.saty_id;
  
  IF (unique_subcode) THEN
    IF (NEW.dbin_id is not NULL) THEN
			SELECT count(*) into counter FROM samples_all 
				where id != NEW.id and code = NEW.code and saty_id = NEW.saty_id and dbin_id = NEW.dbin_id;
			IF (counter > 0) THEN
				RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because database instance sample of the same type with the same subcode already exists.', NEW.code;
			END IF;
		ELSIF (NEW.space_id is not NULL) THEN
			SELECT count(*) into counter FROM samples_all 
				where id != NEW.id and code = NEW.code and saty_id = NEW.saty_id and space_id = NEW.space_id;
			IF (counter > 0) THEN
				RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because space sample of the same type with the same subcode already exists.', NEW.code;
			END IF;
		END IF;
  END IF;
  
  RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';


CREATE OR REPLACE FUNCTION check_created_or_modified_data_set_owner_is_alive() RETURNS trigger AS $$
DECLARE
	owner_code	CODE;
	owner_del_id	TECH_ID;
BEGIN
  -- check sample
  IF (NEW.samp_id IS NOT NULL) THEN
  	SELECT del_id, code INTO owner_del_id, owner_code
  	  FROM samples_all 
  	  WHERE id = NEW.samp_id;
  	IF (owner_del_id IS NOT NULL) THEN 
			RAISE EXCEPTION 'Data Set (Code: %) cannot be connected to a Sample (Code: %) %.', 
			                NEW.code, owner_code, deletion_description(owner_del_id);
		END IF;
	END IF;
	-- check experiment
	SELECT del_id, code INTO owner_del_id, owner_code
    FROM experiments_all 
    WHERE id = NEW.expe_id;
  IF (owner_del_id IS NOT NULL) THEN 
		RAISE EXCEPTION 'Data Set (Code: %) cannot be connected to an Experiment (Code: %) %.', 
		                NEW.code, owner_code, deletion_description(owner_del_id);
	END IF;	
	RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';


CREATE OR REPLACE FUNCTION check_created_or_modified_sample_owner_is_alive() RETURNS trigger AS $$
DECLARE
	owner_code	CODE;
	owner_del_id	TECH_ID;
BEGIN
  -- check experiment (can't be deleted)
  IF (NEW.expe_id IS NOT NULL) THEN
  	SELECT del_id, code INTO owner_del_id, owner_code
  	  FROM experiments_all 
  	  WHERE id = NEW.expe_id;
  	IF (owner_del_id IS NOT NULL) THEN 
			RAISE EXCEPTION 'Sample (Code: %) cannot be connected to an Experiment (Code: %) %.', 
   		                NEW.code, owner_code, deletion_description(owner_del_id);
		END IF;
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';


CREATE OR REPLACE FUNCTION check_deletion_consistency_on_sample_deletion() RETURNS trigger AS $$
DECLARE
  counter  INTEGER;
BEGIN
  -- all directly connected data sets need to be deleted
  -- check datasets
	SELECT count(*) INTO counter 
	  FROM data_all
	  WHERE data_all.samp_id = NEW.id AND data_all.del_id IS NULL;
	IF (counter > 0) THEN
	  RAISE EXCEPTION 'Sample (Code: %) deletion failed because at least one of its data sets was not deleted.', NEW.code;
	END IF;
  -- all components need to be deleted
	SELECT count(*) INTO counter 
	  FROM samples_all 
	  WHERE samples_all.samp_id_part_of = NEW.id AND samples_all.del_id IS NULL;
	IF (counter > 0) THEN
	  RAISE EXCEPTION 'Sample (Code: %) deletion failed because at least one of its component samples was not deleted.', NEW.code;
	END IF;
	-- all children need to be deleted
	SELECT count(*) INTO counter 
		FROM sample_relationships sr, samples_all sc
		WHERE sample_id_parent = NEW.id AND sc.id = sr.sample_id_child AND sc.del_id IS NULL;
	IF (counter > 0) THEN
		RAISE EXCEPTION 'Sample (Code: %) deletion failed because at least one of its child samples was not deleted.', NEW.code;
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';


CREATE OR REPLACE FUNCTION check_deletion_consistency_on_experiment_deletion() RETURNS trigger AS $$
DECLARE
  counter  INTEGER;
BEGIN
  -- check datasets
	SELECT count(*) INTO counter 
	  FROM data_all
	  WHERE data_all.expe_id = NEW.id AND data_all.del_id IS NULL;
	IF (counter > 0) THEN
	  RAISE EXCEPTION 'Experiment (Code: %) deletion failed because at least one of its data sets was not deleted.', NEW.code;
	END IF;
	-- check samples
	SELECT count(*) INTO counter 
	  FROM samples_all 
	  WHERE samples_all.expe_id = NEW.id AND samples_all.del_id IS NULL;
	IF (counter > 0) THEN
	  RAISE EXCEPTION 'Experiment (Code: %) deletion failed because at least one of its samples was not deleted.', NEW.code;
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';