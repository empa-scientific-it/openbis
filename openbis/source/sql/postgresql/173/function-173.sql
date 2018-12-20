-- Creating Functions

------------------------------------------------------------------------------------
--  Purpose:  Create function RENAME_SEQUENCE() that is required for renaming the sequences belonging to tables
------------------------------------------------------------------------------------
CREATE FUNCTION RENAME_SEQUENCE(OLD_NAME VARCHAR, NEW_NAME VARCHAR) RETURNS INTEGER AS $$
DECLARE
  CURR_SEQ_VAL   INTEGER;
BEGIN
  SELECT INTO CURR_SEQ_VAL NEXTVAL(OLD_NAME);
  EXECUTE 'CREATE SEQUENCE ' || NEW_NAME || ' START WITH ' || CURR_SEQ_VAL;
  EXECUTE 'DROP SEQUENCE ' || OLD_NAME;
  RETURN CURR_SEQ_VAL;
END;
$$ LANGUAGE 'plpgsql';


------------------------------------------------------------------------------------
--  Purpose:  Create trigger CONTROLLED_VOCABULARY_CHECK 
------------------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION CONTROLLED_VOCABULARY_CHECK() RETURNS trigger AS $$
DECLARE
   v_code  CODE;
BEGIN

   select code into v_code from data_types where id = NEW.daty_id;

   -- Check if the data is of type "CONTROLLEDVOCABULARY"
   if v_code = 'CONTROLLEDVOCABULARY' then
      if NEW.covo_id IS NULL then
         RAISE EXCEPTION 'Insert/Update of Property Type (Code: %) failed, as its Data Type is CONTROLLEDVOCABULARY, but it is not linked to a Controlled Vocabulary.', NEW.code;
      end if;
   end if;

   RETURN NEW;

END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER CONTROLLED_VOCABULARY_CHECK BEFORE INSERT OR UPDATE ON PROPERTY_TYPES
    FOR EACH ROW EXECUTE PROCEDURE CONTROLLED_VOCABULARY_CHECK();


------------------------------------------------------------------------------------
--  Purpose:  Create trigger EXTERNAL_DATA_STORAGE_FORMAT_CHECK 
------------------------------------------------------------------------------------

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
      select code into data_code from data_all where id = NEW.id; 
      RAISE EXCEPTION 'Insert/Update of Data (Code: %) failed, as its Storage Format is %, but is required to be STORAGE_FORMAT.', data_code, v_covo_code;
   end if;

   RETURN NEW;

END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER EXTERNAL_DATA_STORAGE_FORMAT_CHECK BEFORE INSERT OR UPDATE ON EXTERNAL_DATA
    FOR EACH ROW EXECUTE PROCEDURE EXTERNAL_DATA_STORAGE_FORMAT_CHECK();

   
------------------------------------------------------------------------------------
--  Purpose:  Create triggers for checking sample code uniqueness 
------------------------------------------------------------------------------------

    
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
  
  
CREATE OR REPLACE FUNCTION sample_type_fill_subcode_unique_check()
  RETURNS trigger AS
$BODY$
BEGIN
    IF (NEW.is_subcode_unique::boolean <> OLD.is_subcode_unique::boolean) THEN
      UPDATE samples_all SET subcode_unique_check = subcode_unique_check WHERE saty_id = NEW.id;
  END IF;
    RETURN NEW;
END;
$BODY$
  LANGUAGE 'plpgsql';

  
CREATE TRIGGER sample_fill_code_unique_check
  BEFORE INSERT OR UPDATE
  ON samples_all
  FOR EACH ROW
  EXECUTE PROCEDURE sample_fill_code_unique_check();

CREATE TRIGGER disable_project_level_samples
  BEFORE INSERT OR UPDATE
  ON samples_all
  FOR EACH ROW
  EXECUTE PROCEDURE disable_project_level_samples();


CREATE TRIGGER sample_fill_subcode_unique_check
  BEFORE INSERT OR UPDATE
  ON samples_all
  FOR EACH ROW
  EXECUTE PROCEDURE sample_fill_subcode_unique_check();
  
CREATE TRIGGER sample_type_fill_subcode_unique_check
  AFTER UPDATE
  ON sample_types
  FOR EACH ROW
  EXECUTE PROCEDURE sample_type_fill_subcode_unique_check();
    
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
  
------------------------------------------------------------------------------------
--  Purpose:  Create trigger MATERIAL/SAMPLE/EXPERIMENT/DATA_SET _PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK
--            It checks that if material property value is assigned to the entity,
--						then the material type is equal to the one described by property type.
------------------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION MATERIAL_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK() RETURNS trigger AS $$
DECLARE
   v_type_id  CODE;
   v_type_id_prop  CODE;
BEGIN
   if NEW.mate_prop_id IS NOT NULL then
			-- find material type id of the property type 
			select pt.maty_prop_id into v_type_id_prop 
			  from material_type_property_types etpt, property_types pt 
			 where NEW.mtpt_id = etpt.id AND etpt.prty_id = pt.id;
		
			if v_type_id_prop IS NOT NULL then
				-- find material type id of the material which consists the entity's property value
				select entity.maty_id into v_type_id 
				  from materials entity
				 where NEW.mate_prop_id = entity.id;
				if v_type_id != v_type_id_prop then
					RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', 
							 NEW.mate_prop_id, v_type_id, v_type_id_prop;
				end if;
			end if;
   end if;
   RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER MATERIAL_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK BEFORE INSERT OR UPDATE ON material_properties
    FOR EACH ROW EXECUTE PROCEDURE MATERIAL_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK();
    
CREATE OR REPLACE FUNCTION SAMPLE_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK() RETURNS trigger AS $$
DECLARE
   v_type_id  CODE;
   v_type_id_prop  CODE;
BEGIN
   if NEW.mate_prop_id IS NOT NULL then
			-- find material type id of the property type 
			select pt.maty_prop_id into v_type_id_prop 
			  from sample_type_property_types etpt, property_types pt 
			 where NEW.stpt_id = etpt.id AND etpt.prty_id = pt.id;
		
			if v_type_id_prop IS NOT NULL then
				-- find material type id of the material which consists the entity's property value
				select entity.maty_id into v_type_id 
				  from materials entity
				 where NEW.mate_prop_id = entity.id;
				if v_type_id != v_type_id_prop then
					RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', 
												 NEW.mate_prop_id, v_type_id, v_type_id_prop;
				end if;
			end if;
   end if;
   RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER SAMPLE_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK BEFORE INSERT OR UPDATE ON sample_properties
    FOR EACH ROW EXECUTE PROCEDURE SAMPLE_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK();
    
CREATE OR REPLACE FUNCTION EXPERIMENT_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK() RETURNS trigger AS $$
DECLARE
   v_type_id  CODE;
   v_type_id_prop  CODE;
BEGIN
   if NEW.mate_prop_id IS NOT NULL then
			-- find material type id of the property type 
			select pt.maty_prop_id into v_type_id_prop 
			  from experiment_type_property_types etpt, property_types pt 
			 where NEW.etpt_id = etpt.id AND etpt.prty_id = pt.id;
		
			if v_type_id_prop IS NOT NULL then
				-- find material type id of the material which consists the entity's property value
				select entity.maty_id into v_type_id 
				  from materials entity
				 where NEW.mate_prop_id = entity.id;
				if v_type_id != v_type_id_prop then
					RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', 
												 NEW.mate_prop_id, v_type_id, v_type_id_prop;
				end if;
			end if;
   end if;
   RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER EXPERIMENT_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK BEFORE INSERT OR UPDATE ON experiment_properties
    FOR EACH ROW EXECUTE PROCEDURE EXPERIMENT_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK();
 
 -- data set
CREATE OR REPLACE FUNCTION DATA_SET_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK() RETURNS trigger AS $$
DECLARE
   v_type_id  CODE;
   v_type_id_prop  CODE;
BEGIN
   if NEW.mate_prop_id IS NOT NULL then
			-- find material type id of the property type 
			select pt.maty_prop_id into v_type_id_prop 
			  from data_set_type_property_types dstpt, property_types pt 
			 where NEW.dstpt_id = dstpt.id AND dstpt.prty_id = pt.id;
		
			if v_type_id_prop IS NOT NULL then
				-- find material type id of the material which consists the entity's property value
				select entity.maty_id into v_type_id 
				  from materials entity
				 where NEW.mate_prop_id = entity.id;
				if v_type_id != v_type_id_prop then
					RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', 
												 NEW.mate_prop_id, v_type_id, v_type_id_prop;
				end if;
			end if;
   end if;
   RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER DATA_SET_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK BEFORE INSERT OR UPDATE ON data_set_properties
    FOR EACH ROW EXECUTE PROCEDURE DATA_SET_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK();
    
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
--- on insert/update - deleted experiment or sample can't be connected
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
	RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';
  
CREATE CONSTRAINT TRIGGER check_deletion_consistency_on_sample_deletion 
  AFTER UPDATE ON samples_all
	DEFERRABLE INITIALLY DEFERRED
	FOR EACH ROW 
	EXECUTE PROCEDURE check_deletion_consistency_on_sample_deletion();	

-----------------------------------------
-- update sample relationships on revert 
-----------------------------------------

CREATE OR REPLACE FUNCTION preserve_deletion_consistency_on_sample_relationships() RETURNS trigger AS $$
DECLARE
  delid  TECH_ID;
BEGIN
	IF (NEW.del_id IS NOT NULL OR OLD.del_id IS NULL) THEN
		RETURN NEW;
	END IF;
	SELECT del_id INTO delid
		FROM SAMPLES_ALL where id = NEW.sample_id_parent;
	IF (delid IS NOT NULL) THEN
		NEW.del_id = delid;
	END IF;
	SELECT del_id INTO delid
		FROM SAMPLES_ALL where id = NEW.sample_id_child;
	IF (delid IS NOT NULL) THEN
		NEW.del_id = delid;
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER preserve_deletion_consistency_on_sample_relationships 
  BEFORE UPDATE ON sample_relationships_all
	FOR EACH ROW 
	EXECUTE PROCEDURE preserve_deletion_consistency_on_sample_relationships();
	
-----------------------------------------
-- update dataset relationships on revert 
-----------------------------------------
CREATE OR REPLACE FUNCTION preserve_deletion_consistency_on_data_set_relationships() RETURNS trigger AS $$
DECLARE
  delid  TECH_ID;
BEGIN
	IF (NEW.del_id IS NOT NULL OR OLD.del_id IS NULL) THEN
		RETURN NEW;
	END IF;
	SELECT del_id INTO delid
		FROM DATA_ALL where id = NEW.data_id_parent;
	IF (delid IS NOT NULL) THEN
		NEW.del_id = delid;
	END IF;
	SELECT del_id INTO delid
		FROM DATA_ALL where id = NEW.data_id_child;
	IF (delid IS NOT NULL) THEN
		NEW.del_id = delid;
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER preserve_deletion_consistency_on_data_set_relationships 
  BEFORE UPDATE ON data_set_relationships_all
	FOR EACH ROW 
	EXECUTE PROCEDURE preserve_deletion_consistency_on_data_set_relationships();
	
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


------------------------------------------------------------------------------------
--  Purpose: Create functions/triggers to validate data in table content_copies.
------------------------------------------------------------------------------------
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

CREATE TRIGGER content_copies_uniqueness_check
  BEFORE INSERT OR UPDATE
  ON content_copies
  FOR EACH ROW
  EXECUTE PROCEDURE content_copies_uniqueness_check();

  
CREATE OR REPLACE FUNCTION content_copies_location_type_check() RETURNS trigger AS $$
DECLARE
   edms_address_type EDMS_ADDRESS_TYPE;
   index integer;
BEGIN

   select position(address_type in NEW.location_type), address_type into index, edms_address_type from external_data_management_systems
      where id = NEW.edms_id;

   if index != 1 then
      RAISE EXCEPTION 'Insert/Update to content_copies failed. Location type %, but edms.address_type %', NEW.location_type, edms_address_type;
   end if;

   RETURN NEW;

END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER content_copies_location_type_check 
  BEFORE INSERT OR UPDATE 
  ON content_copies
  FOR EACH ROW 
  EXECUTE PROCEDURE content_copies_location_type_check();

----------------------------------------------------------------------------------------------------
-- Rules for views
----------------------------------------------------------------------------------------------------
CREATE OR REPLACE RULE sample_insert AS
    ON INSERT TO samples DO INSTEAD 
       INSERT INTO samples_all (
         id,
         frozen,
         code, 
         del_id,
         orig_del,
         expe_id,
         expe_frozen,
         proj_id,
         proj_frozen,
         modification_timestamp,
         perm_id,
         pers_id_registerer, 
         pers_id_modifier, 
         registration_timestamp, 
         samp_id_part_of,
         cont_frozen,
         saty_id, 
         space_id,
         space_frozen,
         version
       ) VALUES (
         NEW.id,
         NEW.frozen,
         NEW.code, 
         NEW.del_id,
         NEW.orig_del,
         NEW.expe_id,
         NEW.expe_frozen,
         NEW.proj_id,
         NEW.proj_frozen,
         NEW.modification_timestamp,
         NEW.perm_id,
         NEW.pers_id_registerer, 
         NEW.pers_id_modifier, 
         NEW.registration_timestamp, 
         NEW.samp_id_part_of,
         NEW.cont_frozen,
         NEW.saty_id, 
         NEW.space_id,
         NEW.space_frozen,
         NEW.version
       );
     
CREATE OR REPLACE RULE sample_update AS
    ON UPDATE TO samples DO INSTEAD 
       UPDATE samples_all
          SET code = NEW.code,
              frozen = NEW.frozen,
              del_id = NEW.del_id,
              orig_del = NEW.orig_del,
              expe_id = NEW.expe_id,
              expe_frozen = NEW.expe_frozen,
              proj_id = NEW.proj_id,
              proj_frozen = NEW.proj_frozen,
              modification_timestamp = NEW.modification_timestamp,
              perm_id = NEW.perm_id,
              pers_id_registerer = NEW.pers_id_registerer,
              pers_id_modifier = NEW.pers_id_modifier,
              registration_timestamp = NEW.registration_timestamp,
              samp_id_part_of = NEW.samp_id_part_of,
              cont_frozen = NEW.cont_frozen,
              saty_id = NEW.saty_id,
              space_id = NEW.space_id,
              space_frozen = NEW.space_frozen,
              version = NEW.version
          WHERE id = NEW.id;
     
CREATE OR REPLACE RULE sample_delete AS
    ON DELETE TO samples DO INSTEAD
       DELETE FROM samples_all
              WHERE id = OLD.id;
              
CREATE OR REPLACE RULE sample_deleted_update AS
    ON UPDATE TO samples_deleted DO INSTEAD
       UPDATE samples_all
          SET del_id = NEW.del_id,
              orig_del = NEW.orig_del,
              modification_timestamp = NEW.modification_timestamp,
              version = NEW.version
          WHERE id = NEW.id;
     
CREATE OR REPLACE RULE sample_deleted_delete AS
    ON DELETE TO samples_deleted DO INSTEAD
       DELETE FROM samples_all
              WHERE id = OLD.id;
              
----------------
-- experiment --
----------------

CREATE OR REPLACE RULE experiment_insert AS
  ON INSERT TO experiments DO INSTEAD 
     INSERT INTO experiments_all (
       id,
       frozen,
       code, 
       del_id,
       orig_del,
       exty_id, 
       is_public,
       modification_timestamp,
       perm_id,
       pers_id_registerer, 
       pers_id_modifier, 
       proj_id,
       proj_frozen,
       registration_timestamp,
       version
     ) VALUES (
       NEW.id,
       NEW.frozen,
       NEW.code, 
       NEW.del_id,
       NEW.orig_del,
       NEW.exty_id, 
       NEW.is_public,
       NEW.modification_timestamp,
       NEW.perm_id,
       NEW.pers_id_registerer, 
       NEW.pers_id_modifier, 
       NEW.proj_id,
       NEW.proj_frozen,
       NEW.registration_timestamp,
       NEW.version
     );
     
CREATE OR REPLACE RULE experiment_update AS
    ON UPDATE TO experiments DO INSTEAD 
       UPDATE experiments_all
          SET code = NEW.code,
              frozen = NEW.frozen,
              del_id = NEW.del_id,
              orig_del = NEW.orig_del,
              exty_id = NEW.exty_id,
              is_public = NEW.is_public,
              modification_timestamp = NEW.modification_timestamp,
              perm_id = NEW.perm_id,
              pers_id_registerer = NEW.pers_id_registerer,
              pers_id_modifier = NEW.pers_id_modifier,
              proj_id = NEW.proj_id,
              proj_frozen = NEW.proj_frozen,
              registration_timestamp = NEW.registration_timestamp,
              version = NEW.version
          WHERE id = NEW.id;
     
CREATE OR REPLACE RULE experiment_delete AS
    ON DELETE TO experiments DO INSTEAD
       DELETE FROM experiments_all
              WHERE id = OLD.id;
      
CREATE OR REPLACE RULE experiments_deleted_update AS
    ON UPDATE TO experiments_deleted DO INSTEAD 
       UPDATE experiments_all
          SET del_id = NEW.del_id,
              orig_del = NEW.orig_del,
              modification_timestamp = NEW.modification_timestamp,
              version = NEW.version
          WHERE id = NEW.id;
     
CREATE OR REPLACE RULE experiments_deleted_delete AS
    ON DELETE TO experiments_deleted DO INSTEAD
       DELETE FROM experiments_all
              WHERE id = OLD.id;
                           
                           
----------
-- data --
----------
      
CREATE OR REPLACE RULE data_insert AS
  ON INSERT TO data DO INSTEAD 
     INSERT INTO data_all (
       id,
       frozen,
       code, 
       del_id,
       orig_del,
       expe_id,
       expe_frozen,
       dast_id,
       data_producer_code,
       dsty_id,
       is_derived,
       is_valid,
       modification_timestamp,
       access_timestamp,
       pers_id_registerer,
       pers_id_modifier,
       production_timestamp,
       registration_timestamp,
       samp_id,
       samp_frozen,
       version,
       data_set_kind
     ) VALUES (
       NEW.id,
       NEW.frozen,
       NEW.code, 
       NEW.del_id, 
       NEW.orig_del,
       NEW.expe_id,
       NEW.expe_frozen,
       NEW.dast_id,
       NEW.data_producer_code,
       NEW.dsty_id,
       NEW.is_derived, 
       NEW.is_valid,
       NEW.modification_timestamp,
       NEW.access_timestamp,
       NEW.pers_id_registerer,
       NEW.pers_id_modifier,
       NEW.production_timestamp,
       NEW.registration_timestamp,
       NEW.samp_id,
       NEW.samp_frozen,
       NEW.version,
       NEW.data_set_kind
     );
     
CREATE OR REPLACE RULE data_update AS
    ON UPDATE TO data DO INSTEAD 
       UPDATE data_all
          SET code = NEW.code,
              frozen = NEW.frozen,
              del_id = NEW.del_id,
              orig_del = NEW.orig_del,
              expe_id = NEW.expe_id,
              expe_frozen = NEW.expe_frozen,
              dast_id = NEW.dast_id,
              data_producer_code = NEW.data_producer_code,
              dsty_id = NEW.dsty_id,
              is_derived = NEW.is_derived,
              is_valid = NEW.is_valid,
              modification_timestamp = NEW.modification_timestamp,
              access_timestamp = NEW.access_timestamp,
              pers_id_registerer = NEW.pers_id_registerer,
              pers_id_modifier = NEW.pers_id_modifier,
              production_timestamp = NEW.production_timestamp,
              registration_timestamp = NEW.registration_timestamp,
              samp_id = NEW.samp_id,
              samp_frozen = NEW.samp_frozen,
              version = NEW.version,
              data_set_kind = NEW.data_set_kind
       WHERE id = NEW.id;

              
CREATE OR REPLACE RULE data_all AS
    ON DELETE TO data DO INSTEAD
       DELETE FROM data_all
              WHERE id = OLD.id;
              
CREATE OR REPLACE RULE data_deleted_update AS
    ON UPDATE TO data_deleted DO INSTEAD 
       UPDATE data_all
          SET del_id = NEW.del_id,
              orig_del = NEW.orig_del,
              modification_timestamp = NEW.modification_timestamp,
              version = NEW.version
          WHERE id = NEW.id;
     
CREATE OR REPLACE RULE data_deleted_delete AS
    ON DELETE TO data_deleted DO INSTEAD
       DELETE FROM data_all
              WHERE id = OLD.id;               

-- link_data must refer to a data set of kind LINK
CREATE OR REPLACE FUNCTION check_data_set_kind_link() RETURNS trigger AS $$
DECLARE
    kind DATA_SET_KIND;
BEGIN
    SELECT data_set_kind INTO kind
        FROM data_all 
        WHERE id = NEW.id;
        IF (kind <> 'LINK') THEN 
            RAISE EXCEPTION 'Link data (Data Set Code: %) must reference a data set of kind LINK (is %).', 
                            NEW.id, kind;
        END IF;
    RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE CONSTRAINT TRIGGER check_data_set_kind_link 
    AFTER INSERT OR UPDATE ON link_data
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW
    EXECUTE PROCEDURE check_data_set_kind_link();

-- external_data must refer to a data set of kind PHYSICAL
CREATE OR REPLACE FUNCTION check_data_set_kind_physical() RETURNS trigger AS $$
DECLARE
    kind DATA_SET_KIND;
BEGIN
    SELECT data_set_kind INTO kind
        FROM data_all 
        WHERE id = NEW.id;
        IF (kind <> 'PHYSICAL') THEN 
            RAISE EXCEPTION 'External data (Data Set Code: %) must reference a data set of kind PHYSICAL (is %).', 
                            NEW.id, kind;
        END IF;
    RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE CONSTRAINT TRIGGER check_data_set_kind_physical 
    AFTER INSERT OR UPDATE ON external_data
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW
    EXECUTE PROCEDURE check_data_set_kind_physical();

----------------------------------------------------------------------------------------------------
-- Rules for properties history
----------------------------------------------------------------------------------------------------

-- Material Properties --

CREATE OR REPLACE RULE material_properties_update AS
    ON UPDATE TO material_properties 
    WHERE (OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd' AND OLD.VALUE != NEW.VALUE) 
        OR (OLD.CVTE_ID IS NOT NULL AND OLD.CVTE_ID != NEW.CVTE_ID) 
        OR (OLD.MATE_PROP_ID IS NOT NULL AND OLD.MATE_PROP_ID != NEW.MATE_PROP_ID)
    DO ALSO 
       INSERT INTO material_properties_history (
         ID, 
         MATE_ID, 
         MTPT_ID, 
         VALUE, 
         VOCABULARY_TERM,
         MATERIAL, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('MATERIAL_PROPERTY_ID_SEQ'), 
         OLD.MATE_ID, 
         OLD.MTPT_ID, 
         OLD.VALUE, 
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         NEW.MODIFICATION_TIMESTAMP
       );
       
CREATE OR REPLACE RULE material_properties_delete AS
    ON DELETE TO material_properties 
    WHERE (OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd')
        OR OLD.CVTE_ID IS NOT NULL 
        OR OLD.MATE_PROP_ID IS NOT NULL
    DO ALSO 
       INSERT INTO material_properties_history (
         ID, 
         MATE_ID, 
         MTPT_ID, 
         VALUE, 
         VOCABULARY_TERM,
         MATERIAL, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('MATERIAL_PROPERTY_ID_SEQ'), 
         OLD.MATE_ID, 
         OLD.MTPT_ID, 
         OLD.VALUE, 
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         current_timestamp
       );
       
-- Experiment Properties --

CREATE OR REPLACE RULE experiment_properties_update AS
    ON UPDATE TO experiment_properties 
    WHERE (OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd' AND OLD.VALUE != NEW.VALUE) 
        OR (OLD.CVTE_ID IS NOT NULL AND OLD.CVTE_ID != NEW.CVTE_ID) 
        OR (OLD.MATE_PROP_ID IS NOT NULL AND OLD.MATE_PROP_ID != NEW.MATE_PROP_ID)
    DO ALSO 
       INSERT INTO experiment_properties_history (
         ID, 
         EXPE_ID,
         ETPT_ID, 
         VALUE, 
         VOCABULARY_TERM,
         MATERIAL, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('EXPERIMENT_PROPERTY_ID_SEQ'), 
         OLD.EXPE_ID, 
         OLD.ETPT_ID, 
         OLD.VALUE, 
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         NEW.MODIFICATION_TIMESTAMP
       );
       
CREATE OR REPLACE RULE experiment_properties_delete AS
    ON DELETE TO experiment_properties 
    WHERE (OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd')
        OR OLD.CVTE_ID IS NOT NULL 
        OR OLD.MATE_PROP_ID IS NOT NULL
    DO ALSO 
       INSERT INTO experiment_properties_history (
         ID, 
         EXPE_ID,
         ETPT_ID, 
         VALUE, 
         VOCABULARY_TERM,
         MATERIAL, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('EXPERIMENT_PROPERTY_ID_SEQ'), 
         OLD.EXPE_ID, 
         OLD.ETPT_ID, 
         OLD.VALUE, 
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         current_timestamp
       );
       
-- Sample Properties --

CREATE OR REPLACE RULE sample_properties_update AS
    ON UPDATE TO sample_properties
    WHERE (OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd' AND OLD.VALUE != NEW.VALUE) 
        OR (OLD.CVTE_ID IS NOT NULL AND OLD.CVTE_ID != NEW.CVTE_ID) 
        OR (OLD.MATE_PROP_ID IS NOT NULL AND OLD.MATE_PROP_ID != NEW.MATE_PROP_ID)
    DO ALSO
       INSERT INTO sample_properties_history (
         ID, 
         SAMP_ID,
         STPT_ID, 
         VALUE, 
         VOCABULARY_TERM,
         MATERIAL, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('SAMPLE_PROPERTY_ID_SEQ'), 
         OLD.SAMP_ID, 
         OLD.STPT_ID, 
         OLD.VALUE, 
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         NEW.MODIFICATION_TIMESTAMP
       );
              
CREATE OR REPLACE RULE sample_properties_delete AS
    ON DELETE TO sample_properties 
    WHERE ((OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd')
        OR OLD.CVTE_ID IS NOT NULL 
        OR OLD.MATE_PROP_ID IS NOT NULL)
	   AND (SELECT DEL_ID FROM SAMPLES_ALL WHERE ID = OLD.SAMP_ID) IS NULL
     DO ALSO
       INSERT INTO sample_properties_history (
         ID, 
         SAMP_ID,
         STPT_ID, 
         VALUE, 
         VOCABULARY_TERM,
         MATERIAL, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('SAMPLE_PROPERTY_ID_SEQ'), 
         OLD.SAMP_ID, 
         OLD.STPT_ID, 
         OLD.VALUE, 
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         current_timestamp
       );
       
-- Data Set Properties --

CREATE OR REPLACE RULE data_set_properties_update AS
    ON UPDATE TO data_set_properties 
    WHERE (OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd' AND OLD.VALUE != NEW.VALUE) 
        OR (OLD.CVTE_ID IS NOT NULL AND OLD.CVTE_ID != NEW.CVTE_ID) 
        OR (OLD.MATE_PROP_ID IS NOT NULL AND OLD.MATE_PROP_ID != NEW.MATE_PROP_ID)
    DO ALSO
       INSERT INTO data_set_properties_history (
         ID, 
         DS_ID,
         DSTPT_ID, 
         VALUE, 
         VOCABULARY_TERM,
         MATERIAL, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('DATA_SET_PROPERTY_ID_SEQ'), 
         OLD.DS_ID, 
         OLD.DSTPT_ID, 
         OLD.VALUE, 
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         NEW.MODIFICATION_TIMESTAMP
       );

CREATE OR REPLACE RULE data_set_properties_delete AS
    ON DELETE TO data_set_properties 
    WHERE ((OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd')
        OR OLD.CVTE_ID IS NOT NULL 
        OR OLD.MATE_PROP_ID IS NOT NULL)
	   AND (SELECT DEL_ID FROM DATA_ALL WHERE ID = OLD.DS_ID) IS NULL
    DO ALSO
       INSERT INTO data_set_properties_history (
         ID, 
         DS_ID,
         DSTPT_ID, 
         VALUE, 
         VOCABULARY_TERM,
         MATERIAL, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('DATA_SET_PROPERTY_ID_SEQ'), 
         OLD.DS_ID, 
         OLD.DSTPT_ID, 
         OLD.VALUE, 
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         current_timestamp
       );
       
-- End of rules for properties history

CREATE OR REPLACE RULE data_set_relationships_insert AS
    ON INSERT TO data_set_relationships DO INSTEAD 
       INSERT INTO data_set_relationships_all (
         data_id_parent,
         parent_frozen,
         data_id_child,
         child_frozen,
         pers_id_author,
         relationship_id,
         ordinal,
         registration_timestamp,
         modification_timestamp
       ) VALUES (
         NEW.data_id_parent,
         NEW.parent_frozen,
         NEW.data_id_child,
         NEW.child_frozen,
         NEW.pers_id_author,
         NEW.relationship_id,
         NEW.ordinal,
         NEW.registration_timestamp,
         NEW.modification_timestamp
       );

CREATE OR REPLACE RULE data_set_relationships_update AS
    ON UPDATE TO data_set_relationships DO INSTEAD 
       UPDATE data_set_relationships_all
          SET 
            data_id_parent = NEW.data_id_parent,
            parent_frozen = NEW.parent_frozen,
            data_id_child = NEW.data_id_child,
            child_frozen = NEW.child_frozen,
            del_id = NEW.del_id,
            relationship_id = NEW.relationship_id,
            ordinal = NEW.ordinal,
            pers_id_author = NEW.pers_id_author,
            registration_timestamp = NEW.registration_timestamp,
            modification_timestamp = NEW.modification_timestamp
          WHERE data_id_parent = NEW.data_id_parent and data_id_child = NEW.data_id_child 
                and relationship_id = NEW.relationship_id;
          
CREATE OR REPLACE RULE data_set_relationships_delete AS
    ON DELETE TO data_set_relationships DO INSTEAD
       DELETE FROM data_set_relationships_all
              WHERE data_id_parent = OLD.data_id_parent and data_id_child = OLD.data_id_child
                    and relationship_id = OLD.relationship_id;

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
         modification_timestamp
       ) VALUES (
         NEW.id, 
         NEW.sample_id_parent,
         NEW.parent_frozen,
         NEW.relationship_id,
         NEW.sample_id_child,
         NEW.child_frozen,
         NEW.pers_id_author,
         NEW.registration_timestamp,
         NEW.modification_timestamp
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
             modification_timestamp = NEW.modification_timestamp
          WHERE id = NEW.id;
     
CREATE OR REPLACE RULE sample_relationships_delete AS
    ON DELETE TO sample_relationships DO INSTEAD
       DELETE FROM sample_relationships_all
              WHERE id = OLD.id;

CREATE OR REPLACE RULE METAPROJECT_ASSIGNMENTS_INSERT AS
    ON INSERT TO METAPROJECT_ASSIGNMENTS DO INSTEAD 
       INSERT INTO METAPROJECT_ASSIGNMENTS_ALL (
         ID, 
         MEPR_ID,
         EXPE_ID,
			   SAMP_ID,
			   DATA_ID,
			   MATE_ID,
			   DEL_ID,
			   CREATION_DATE
       ) VALUES (
         NEW.ID, 
         NEW.MEPR_ID,
         NEW.EXPE_ID,
			   NEW.SAMP_ID,
			   NEW.DATA_ID,
			   NEW.MATE_ID,
			   NEW.DEL_ID,
			   NEW.CREATION_DATE
       );

CREATE OR REPLACE RULE METAPROJECT_ASSIGNMENTS_UPDATE AS
    ON UPDATE TO METAPROJECT_ASSIGNMENTS DO INSTEAD 
       UPDATE METAPROJECT_ASSIGNMENTS_ALL
          SET 
			      ID = NEW.ID, 
         		MEPR_ID = NEW.MEPR_ID,
         		EXPE_ID = NEW.EXPE_ID,
			   		SAMP_ID = NEW.SAMP_ID,
			   		DATA_ID = NEW.DATA_ID,
			   		MATE_ID = NEW.MATE_ID,
			   		DEL_ID = NEW.DEL_ID,
			   		CREATION_DATE = NEW.CREATION_DATE
          WHERE ID = NEW.ID;
          
CREATE OR REPLACE RULE METAPROJECT_ASSIGNMENTS_DELETE AS
    ON DELETE TO METAPROJECT_ASSIGNMENTS DO INSTEAD
       DELETE FROM METAPROJECT_ASSIGNMENTS_ALL
          WHERE ID = OLD.ID;
              
----------------------------------------------------------------------------------------------------
-- Rules for relationships history
----------------------------------------------------------------------------------------------------

-- sample -> experiment

CREATE OR REPLACE RULE sample_experiment_update AS
    ON UPDATE TO samples_all 
    WHERE (OLD.EXPE_ID != NEW.EXPE_ID OR OLD.EXPE_ID IS NULL) AND NEW.EXPE_ID IS NOT NULL
    DO ALSO (
       UPDATE EXPERIMENT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_EXPE_ID = OLD.EXPE_ID AND SAMP_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       INSERT INTO EXPERIMENT_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_EXPE_ID,
         RELATION_TYPE, 
         SAMP_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('EXPERIMENT_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.EXPE_ID, 
         'OWNER', 
         NEW.ID, 
         NEW.PERM_ID,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE MAIN_SAMP_ID = OLD.ID AND EXPE_ID = OLD.EXPE_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         EXPE_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.EXPE_ID, 
         (SELECT PERM_ID FROM EXPERIMENTS_ALL WHERE ID = NEW.EXPE_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
    );
    
CREATE OR REPLACE RULE sample_experiment_remove_update AS
    ON UPDATE TO samples_all 
    WHERE OLD.EXPE_ID IS NOT NULL AND NEW.EXPE_ID IS NULL
    DO ALSO (
       UPDATE EXPERIMENT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_EXPE_ID = OLD.EXPE_ID AND SAMP_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE MAIN_SAMP_ID = OLD.ID AND EXPE_ID = OLD.EXPE_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
    );
    
CREATE OR REPLACE RULE sample_experiment_insert AS
    ON INSERT TO samples_all 
    WHERE NEW.EXPE_ID IS NOT NULL
       DO ALSO (
       INSERT INTO EXPERIMENT_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_EXPE_ID,
         RELATION_TYPE, 
         SAMP_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('EXPERIMENT_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.EXPE_ID, 
         'OWNER', 
         NEW.ID, 
         NEW.PERM_ID,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         EXPE_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.EXPE_ID, 
         (SELECT PERM_ID FROM EXPERIMENTS_ALL WHERE ID = NEW.EXPE_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
   );
   
CREATE OR REPLACE RULE sample_experiment_delete AS
    ON DELETE TO samples_all 
    WHERE OLD.EXPE_ID IS NOT NULL
       DO ALSO 
       UPDATE EXPERIMENT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = current_timestamp 
         WHERE MAIN_EXPE_ID = OLD.EXPE_ID AND SAMP_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;
     
-- container samples
       
CREATE OR REPLACE RULE sample_container_update AS
    ON UPDATE TO samples_all 
    WHERE (OLD.SAMP_ID_PART_OF != NEW.SAMP_ID_PART_OF OR OLD.SAMP_ID_PART_OF IS NULL) AND NEW.SAMP_ID_PART_OF IS NOT NULL
    DO ALSO (
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE (MAIN_SAMP_ID = OLD.SAMP_ID_PART_OF AND SAMP_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL AND RELATION_TYPE = 'CONTAINER')
           OR (MAIN_SAMP_ID = OLD.ID AND SAMP_ID = OLD.SAMP_ID_PART_OF AND VALID_UNTIL_TIMESTAMP IS NULL AND RELATION_TYPE = 'CONTAINED');
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         SAMP_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.SAMP_ID_PART_OF, 
         'CONTAINER', 
         NEW.ID, 
         NEW.PERM_ID,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         SAMP_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'CONTAINED', 
         NEW.SAMP_ID_PART_OF, 
         (SELECT PERM_ID FROM SAMPLES_ALL WHERE ID = NEW.SAMP_ID_PART_OF),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
    );
    
CREATE OR REPLACE RULE sample_container_remove_update AS
    ON UPDATE TO samples_all 
    WHERE OLD.SAMP_ID_PART_OF IS NOT NULL AND NEW.SAMP_ID_PART_OF IS NULL
    DO ALSO (
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE (MAIN_SAMP_ID = OLD.SAMP_ID_PART_OF AND SAMP_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL AND RELATION_TYPE = 'CONTAINER')
           OR (MAIN_SAMP_ID = OLD.ID AND SAMP_ID = OLD.SAMP_ID_PART_OF AND VALID_UNTIL_TIMESTAMP IS NULL AND RELATION_TYPE = 'CONTAINED');
    );
    
CREATE OR REPLACE RULE sample_container_insert AS
    ON INSERT TO samples_all 
    WHERE NEW.SAMP_ID_PART_OF IS NOT NULL
       DO ALSO (
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         SAMP_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.SAMP_ID_PART_OF, 
         'CONTAINER', 
         NEW.ID, 
         NEW.PERM_ID,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         SAMP_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'CONTAINED', 
         NEW.SAMP_ID_PART_OF, 
         (SELECT PERM_ID FROM SAMPLES_ALL WHERE ID = NEW.SAMP_ID_PART_OF),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
   );
   
CREATE OR REPLACE RULE sample_container_delete AS
    ON DELETE TO samples_all 
    WHERE OLD.SAMP_ID_PART_OF IS NOT NULL
       DO ALSO 
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = current_timestamp 
         WHERE MAIN_SAMP_ID = OLD.SAMP_ID_PART_OF AND SAMP_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL AND RELATION_TYPE = 'CONTAINER';

-- dataset -> eperiment

CREATE OR REPLACE RULE dataset_experiment_update AS
    ON UPDATE TO data_all 
    WHERE (OLD.EXPE_ID != NEW.EXPE_ID OR OLD.SAMP_ID IS NOT NULL) AND NEW.SAMP_ID IS NULL
    DO ALSO (
       UPDATE EXPERIMENT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_EXPE_ID = OLD.EXPE_ID AND DATA_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       INSERT INTO EXPERIMENT_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_EXPE_ID,
         RELATION_TYPE, 
         DATA_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('EXPERIMENT_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.EXPE_ID, 
         'OWNER', 
         NEW.ID, 
         NEW.CODE,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       UPDATE DATA_SET_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE (MAIN_DATA_ID = OLD.ID AND EXPE_ID = OLD.EXPE_ID AND VALID_UNTIL_TIMESTAMP IS NULL);
       INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_DATA_ID,
         RELATION_TYPE, 
         EXPE_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.EXPE_ID, 
         (SELECT PERM_ID FROM EXPERIMENTS_ALL WHERE ID = NEW.EXPE_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
    );
    
CREATE OR REPLACE RULE dataset_experiment_remove_update AS
    ON UPDATE TO data_all 
    WHERE OLD.SAMP_ID IS NULL AND NEW.SAMP_ID IS NOT NULL
    DO ALSO (
       UPDATE EXPERIMENT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_EXPE_ID = OLD.EXPE_ID AND DATA_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       UPDATE DATA_SET_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE MAIN_DATA_ID = OLD.ID AND EXPE_ID = OLD.EXPE_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
    );
    
CREATE OR REPLACE RULE dataset_experiment_insert AS
    ON INSERT TO data_all 
    WHERE NEW.EXPE_ID IS NOT NULL AND NEW.SAMP_ID IS NULL
       DO ALSO (
       INSERT INTO EXPERIMENT_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_EXPE_ID,
         RELATION_TYPE, 
         DATA_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('EXPERIMENT_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.EXPE_ID, 
         'OWNER', 
         NEW.ID, 
         NEW.CODE,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_DATA_ID,
         RELATION_TYPE, 
         EXPE_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.EXPE_ID, 
         (SELECT PERM_ID FROM EXPERIMENTS_ALL WHERE ID = NEW.EXPE_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
   );
   
CREATE OR REPLACE RULE dataset_experiment_delete AS
    ON DELETE TO data_all 
    WHERE OLD.EXPE_ID IS NOT NULL AND OLD.SAMP_ID IS NULL
       DO ALSO 
       UPDATE EXPERIMENT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = current_timestamp 
         WHERE MAIN_EXPE_ID = OLD.EXPE_ID AND DATA_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;

-- dataset -> sample

CREATE OR REPLACE RULE dataset_sample_update AS
    ON UPDATE TO data_all 
    WHERE (OLD.SAMP_ID != NEW.SAMP_ID OR OLD.SAMP_ID IS NULL) AND NEW.SAMP_ID IS NOT NULL
    DO ALSO (
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_SAMP_ID = OLD.SAMP_ID AND DATA_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         DATA_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.SAMP_ID, 
         'OWNER', 
         NEW.ID, 
         NEW.CODE,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       UPDATE DATA_SET_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE (MAIN_DATA_ID = OLD.ID AND SAMP_ID = OLD.SAMP_ID AND VALID_UNTIL_TIMESTAMP IS NULL);
       INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_DATA_ID,
         RELATION_TYPE, 
         SAMP_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.SAMP_ID, 
         (SELECT PERM_ID FROM SAMPLES_ALL WHERE ID = NEW.SAMP_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
    );
    
CREATE OR REPLACE RULE dataset_sample_remove_update AS
    ON UPDATE TO data_all 
    WHERE OLD.SAMP_ID IS NOT NULL AND NEW.SAMP_ID IS NULL
    DO ALSO (
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_SAMP_ID = OLD.SAMP_ID AND DATA_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       UPDATE DATA_SET_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE MAIN_DATA_ID = OLD.ID AND SAMP_ID = OLD.SAMP_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
    );
    
CREATE OR REPLACE RULE dataset_sample_insert AS
    ON INSERT TO data_all 
    WHERE NEW.SAMP_ID IS NOT NULL
       DO ALSO (
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         DATA_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.SAMP_ID, 
         'OWNER', 
         NEW.ID, 
         NEW.CODE,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_DATA_ID,
         RELATION_TYPE, 
         SAMP_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.SAMP_ID, 
         (SELECT PERM_ID FROM SAMPLES_ALL WHERE ID = NEW.SAMP_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
   );
   
CREATE OR REPLACE RULE dataset_sample_delete AS
    ON DELETE TO data_all 
    WHERE OLD.SAMP_ID IS NOT NULL
       DO ALSO 
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = current_timestamp 
         WHERE MAIN_SAMP_ID = OLD.SAMP_ID AND DATA_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;

-- data set relationship

CREATE OR REPLACE RULE data_relationship_insert AS
    ON INSERT TO data_set_relationships_all
    WHERE NEW.DEL_ID IS NULL
       DO ALSO (
         INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
           ID, 
           MAIN_DATA_ID,
           RELATION_TYPE, 
           DATA_ID, 
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP,
           ORDINAL
         ) VALUES (
           nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.DATA_ID_PARENT, 
           (SELECT UPPER(PARENT_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = NEW.RELATIONSHIP_ID), 
           NEW.DATA_ID_CHILD, 
           (SELECT CODE FROM data_all WHERE ID = NEW.DATA_ID_CHILD),
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP,
           NEW.ORDINAL
         );
         INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
           ID, 
           MAIN_DATA_ID,
           RELATION_TYPE, 
           DATA_ID, 
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP,
           ORDINAL
         ) VALUES (
           nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.DATA_ID_CHILD, 
           (SELECT UPPER(CHILD_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = NEW.RELATIONSHIP_ID), 
           NEW.DATA_ID_PARENT, 
           (SELECT CODE FROM data_all WHERE ID = NEW.DATA_ID_PARENT),
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP,
           NEW.ORDINAL
         );
       );

CREATE OR REPLACE RULE data_relationship_delete AS
    ON DELETE TO data_set_relationships_all
    WHERE OLD.DEL_ID IS NULL
       DO ALSO (
         UPDATE DATA_SET_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = current_timestamp
           WHERE (MAIN_DATA_ID = OLD.DATA_ID_PARENT 
                  AND DATA_ID = OLD.DATA_ID_CHILD
                  AND RELATION_TYPE = (SELECT UPPER(PARENT_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = OLD.RELATIONSHIP_ID)
                  AND VALID_UNTIL_TIMESTAMP IS NULL)
             OR (MAIN_DATA_ID = OLD.DATA_ID_CHILD 
                 AND DATA_ID = OLD.DATA_ID_PARENT 
                 AND RELATION_TYPE = (SELECT UPPER(CHILD_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = OLD.RELATIONSHIP_ID)
                 AND VALID_UNTIL_TIMESTAMP IS NULL);
       );
       
CREATE OR REPLACE RULE data_relationship_update AS
    ON UPDATE TO data_set_relationships_all
    WHERE NEW.DEL_ID IS NULL AND OLD.DEL_ID IS NULL
       DO ALSO (
         UPDATE DATA_SET_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = current_timestamp
           WHERE (MAIN_DATA_ID = OLD.DATA_ID_PARENT 
                  AND DATA_ID = OLD.DATA_ID_CHILD
                  AND RELATION_TYPE = (SELECT UPPER(PARENT_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = OLD.RELATIONSHIP_ID)
                  AND VALID_UNTIL_TIMESTAMP IS NULL)
             OR (MAIN_DATA_ID = OLD.DATA_ID_CHILD 
                 AND DATA_ID = OLD.DATA_ID_PARENT 
                 AND RELATION_TYPE = (SELECT UPPER(CHILD_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = OLD.RELATIONSHIP_ID)
                 AND VALID_UNTIL_TIMESTAMP IS NULL);
         INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
           ID, 
           MAIN_DATA_ID,
           RELATION_TYPE, 
           DATA_ID, 
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP,
           ORDINAL
         ) VALUES (
           nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.DATA_ID_PARENT, 
           (SELECT UPPER(PARENT_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = NEW.RELATIONSHIP_ID), 
           NEW.DATA_ID_CHILD, 
           (SELECT CODE FROM data_all WHERE ID = NEW.DATA_ID_CHILD),
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP,
           NEW.ORDINAL
         );
         INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
           ID, 
           MAIN_DATA_ID,
           RELATION_TYPE, 
           DATA_ID, 
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP,
           ORDINAL
         ) VALUES (
           nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.DATA_ID_CHILD, 
           (SELECT UPPER(CHILD_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = NEW.RELATIONSHIP_ID), 
           NEW.DATA_ID_PARENT, 
           (SELECT CODE FROM data_all WHERE ID = NEW.DATA_ID_PARENT),
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP,
           NEW.ORDINAL
         );
       );
       
CREATE OR REPLACE RULE data_relationship_trash_update AS
    ON UPDATE TO data_set_relationships_all
    WHERE NEW.DEL_ID IS NOT NULL AND OLD.DEL_ID IS NULL
       DO ALSO (
         UPDATE DATA_SET_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = current_timestamp
           WHERE (MAIN_DATA_ID = OLD.DATA_ID_PARENT 
                  AND DATA_ID = OLD.DATA_ID_CHILD
                  AND RELATION_TYPE = (SELECT UPPER(PARENT_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = OLD.RELATIONSHIP_ID)
                  AND VALID_UNTIL_TIMESTAMP IS NULL)
             OR (MAIN_DATA_ID = OLD.DATA_ID_CHILD 
                 AND DATA_ID = OLD.DATA_ID_PARENT 
                 AND RELATION_TYPE = (SELECT UPPER(CHILD_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = OLD.RELATIONSHIP_ID)
                 AND VALID_UNTIL_TIMESTAMP IS NULL);
       );
       
CREATE OR REPLACE RULE data_relationship_trash_revert_update AS
    ON UPDATE TO data_set_relationships_all
    WHERE OLD.DEL_ID IS NOT NULL AND NEW.DEL_ID IS NULL
       DO ALSO (
         INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
           ID, 
           MAIN_DATA_ID,
           RELATION_TYPE, 
           DATA_ID, 
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP,
           ORDINAL
         ) VALUES (
           nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.DATA_ID_PARENT, 
           (SELECT UPPER(PARENT_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = NEW.RELATIONSHIP_ID), 
           NEW.DATA_ID_CHILD, 
           (SELECT CODE FROM data_all WHERE ID = NEW.DATA_ID_CHILD),
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP,
           NEW.ORDINAL
         );
         INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
           ID, 
           MAIN_DATA_ID,
           RELATION_TYPE, 
           DATA_ID, 
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP,
           ORDINAL
         ) VALUES (
           nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.DATA_ID_CHILD, 
           (SELECT UPPER(CHILD_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = NEW.RELATIONSHIP_ID), 
           NEW.DATA_ID_PARENT, 
           (SELECT CODE FROM data_all WHERE ID = NEW.DATA_ID_PARENT),
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP,
           NEW.ORDINAL
         );
       );


-- data set content copies relationships

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
    
CREATE OR REPLACE RULE content_copies_history_delete AS
  ON DELETE TO content_copies
  DO ALSO (
    UPDATE data_set_copies_history SET valid_until_timestamp = CURRENT_TIMESTAMP
    WHERE cc_id = OLD.id;
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


-- samples parent-child relationship

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
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP
         ) VALUES (
           nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.SAMPLE_ID_PARENT, 
           'PARENT', 
           NEW.SAMPLE_ID_CHILD, 
           (SELECT PERM_ID FROM samples_all WHERE ID = NEW.SAMPLE_ID_CHILD),
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP
         );
         INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
           ID, 
           MAIN_SAMP_ID,
           RELATION_TYPE, 
           SAMP_ID, 
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP
         ) VALUES (
           nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.SAMPLE_ID_CHILD, 
           'CHILD', 
           NEW.SAMPLE_ID_PARENT, 
           (SELECT PERM_ID FROM samples_all WHERE ID = NEW.SAMPLE_ID_PARENT),
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP
         );
       );
       
CREATE OR REPLACE RULE sample_parent_child_delete AS
    ON DELETE TO sample_relationships_all
    WHERE OLD.DEL_ID IS NULL
       DO ALSO (
         UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = current_timestamp
           WHERE (MAIN_SAMP_ID = OLD.SAMPLE_ID_PARENT AND SAMP_ID = OLD.SAMPLE_ID_CHILD AND VALID_UNTIL_TIMESTAMP IS NULL)
             OR (MAIN_SAMP_ID = OLD.SAMPLE_ID_CHILD AND SAMP_ID = OLD.SAMPLE_ID_PARENT AND VALID_UNTIL_TIMESTAMP IS NULL);
       );

CREATE OR REPLACE RULE sample_parent_child_update AS
    ON UPDATE TO sample_relationships_all
    WHERE NEW.DEL_ID IS NOT NULL AND OLD.DEL_ID IS NULL
       DO ALSO (
         UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = current_timestamp
           WHERE (MAIN_SAMP_ID = OLD.SAMPLE_ID_PARENT AND SAMP_ID = OLD.SAMPLE_ID_CHILD AND VALID_UNTIL_TIMESTAMP IS NULL)
             OR (MAIN_SAMP_ID = OLD.SAMPLE_ID_CHILD AND SAMP_ID = OLD.SAMPLE_ID_PARENT AND VALID_UNTIL_TIMESTAMP IS NULL);
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
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP
         ) VALUES (
           nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.SAMPLE_ID_PARENT, 
           'PARENT', 
           NEW.SAMPLE_ID_CHILD, 
           (SELECT PERM_ID FROM samples_all WHERE ID = NEW.SAMPLE_ID_CHILD),
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP
         );
         INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
           ID, 
           MAIN_SAMP_ID,
           RELATION_TYPE, 
           SAMP_ID, 
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP
         ) VALUES (
           nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.SAMPLE_ID_CHILD, 
           'CHILD', 
           NEW.SAMPLE_ID_PARENT, 
           (SELECT PERM_ID FROM samples_all WHERE ID = NEW.SAMPLE_ID_PARENT),
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP
         );
       );

-- experiment -> project

CREATE OR REPLACE RULE experiment_project_update AS
    ON UPDATE TO experiments_all 
    WHERE (OLD.PROJ_ID != NEW.PROJ_ID OR OLD.PROJ_ID IS NULL) AND NEW.PROJ_ID IS NOT NULL
    DO ALSO (
       UPDATE PROJECT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_PROJ_ID = OLD.PROJ_ID AND EXPE_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       INSERT INTO PROJECT_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_PROJ_ID,
         RELATION_TYPE, 
         EXPE_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('PROJECT_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.PROJ_ID, 
         'OWNER', 
         NEW.ID, 
         NEW.PERM_ID,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       UPDATE EXPERIMENT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE MAIN_EXPE_ID = OLD.ID AND PROJ_ID = OLD.PROJ_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       INSERT INTO EXPERIMENT_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_EXPE_ID,
         RELATION_TYPE, 
         PROJ_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('EXPERIMENT_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.PROJ_ID, 
         (SELECT perm_id FROM PROJECTS WHERE ID = NEW.PROJ_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
    );
    
CREATE OR REPLACE RULE experiment_project_remove_update AS
    ON UPDATE TO experiments_all 
    WHERE OLD.PROJ_ID IS NOT NULL AND NEW.PROJ_ID IS NULL
    DO ALSO (
       UPDATE PROJECT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_PROJ_ID = OLD.PROJ_ID AND EXPE_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       UPDATE EXPERIMENT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE MAIN_EXPE_ID = OLD.ID AND PROJ_ID = OLD.PROJ_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
    );
    
CREATE OR REPLACE RULE experiment_project_insert AS
    ON INSERT TO experiments_all 
    WHERE NEW.PROJ_ID IS NOT NULL
       DO ALSO (
       INSERT INTO PROJECT_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_PROJ_ID,
         RELATION_TYPE, 
         EXPE_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('PROJECT_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.PROJ_ID, 
         'OWNER', 
         NEW.ID, 
         NEW.PERM_ID,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       INSERT INTO EXPERIMENT_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_EXPE_ID,
         RELATION_TYPE, 
         PROJ_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('EXPERIMENT_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.PROJ_ID, 
         (SELECT perm_id FROM PROJECTS WHERE ID = NEW.PROJ_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
   );
   
CREATE OR REPLACE RULE experiment_project_delete AS
    ON DELETE TO experiments_all 
    WHERE OLD.PROJ_ID IS NOT NULL
       DO ALSO 
       UPDATE PROJECT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = current_timestamp 
         WHERE MAIN_PROJ_ID = OLD.PROJ_ID AND EXPE_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;

-- project -> space

CREATE OR REPLACE RULE project_space_update AS
    ON UPDATE TO projects 
    WHERE (OLD.SPACE_ID != NEW.SPACE_ID OR OLD.SPACE_ID IS NULL) AND NEW.SPACE_ID IS NOT NULL
    DO ALSO (
       UPDATE PROJECT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_PROJ_ID = OLD.ID AND SPACE_ID = OLD.SPACE_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       INSERT INTO PROJECT_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_PROJ_ID,
         RELATION_TYPE, 
         SPACE_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('PROJECT_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.SPACE_ID, 
         (SELECT CODE FROM SPACES WHERE ID = NEW.SPACE_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
    );
    
CREATE OR REPLACE RULE project_space_remove_update AS
    ON UPDATE TO projects 
    WHERE OLD.SPACE_ID IS NOT NULL AND NEW.SPACE_ID IS NULL
    DO ALSO (
       UPDATE PROJECT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_PROJ_ID = OLD.ID AND SPACE_ID = OLD.SPACE_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
    );
    
CREATE OR REPLACE RULE project_space_insert AS
    ON INSERT TO projects 
    WHERE NEW.SPACE_ID IS NOT NULL
       DO ALSO (
       INSERT INTO PROJECT_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_PROJ_ID,
         RELATION_TYPE, 
         SPACE_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('PROJECT_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.SPACE_ID, 
         (SELECT CODE FROM SPACES WHERE ID = NEW.SPACE_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
   );

-- sample -> project

CREATE OR REPLACE RULE sample_project_update AS
    ON UPDATE TO samples_all 
    WHERE (OLD.PROJ_ID != NEW.PROJ_ID OR OLD.PROJ_ID IS NULL OR OLD.EXPE_ID IS NOT NULL) AND NEW.PROJ_ID IS NOT NULL AND NEW.EXPE_ID IS NULL
    DO ALSO (
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE MAIN_SAMP_ID = OLD.ID AND PROJ_ID = OLD.PROJ_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         PROJ_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.PROJ_ID, 
         (SELECT PERM_ID FROM PROJECTS WHERE ID = NEW.PROJ_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
    );

CREATE OR REPLACE RULE sample_project_remove_update AS
    ON UPDATE TO samples_all 
    WHERE OLD.PROJ_ID IS NOT NULL AND (NEW.PROJ_ID IS NULL OR (OLD.EXPE_ID IS NULL AND NEW.EXPE_ID IS NOT NULL))
    DO ALSO (
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE MAIN_SAMP_ID = OLD.ID AND PROJ_ID = OLD.PROJ_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
    );

CREATE OR REPLACE RULE sample_project_insert AS
    ON INSERT TO samples_all 
    WHERE NEW.EXPE_ID IS NULL AND NEW.PROJ_ID IS NOT NULL
    DO ALSO (
      INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         PROJ_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.PROJ_ID, 
         (SELECT PERM_ID FROM PROJECTS WHERE ID = NEW.PROJ_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
   );

-- sample -> project

CREATE OR REPLACE RULE sample_space_update AS
    ON UPDATE TO samples_all 
    WHERE (OLD.SPACE_ID != NEW.SPACE_ID OR OLD.SPACE_ID IS NULL OR OLD.EXPE_ID IS NOT NULL OR OLD.PROJ_ID IS NOT NULL) AND NEW.SPACE_ID IS NOT NULL AND NEW.EXPE_ID IS NULL AND NEW.PROJ_ID IS NULL
    DO ALSO (
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE MAIN_SAMP_ID = OLD.ID AND SPACE_ID = OLD.SPACE_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         SPACE_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.SPACE_ID, 
         (SELECT CODE FROM SPACES WHERE ID = NEW.SPACE_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
    );

CREATE OR REPLACE RULE sample_space_remove_update AS
    ON UPDATE TO samples_all 
    WHERE OLD.SPACE_ID IS NOT NULL AND (NEW.SPACE_ID IS NULL OR (OLD.EXPE_ID IS NULL AND NEW.EXPE_ID IS NOT NULL) OR (OLD.PROJ_ID IS NULL AND NEW.PROJ_ID IS NOT NULL))
    DO ALSO (
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE MAIN_SAMP_ID = OLD.ID AND SPACE_ID = OLD.SPACE_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
    );

CREATE OR REPLACE RULE sample_space_insert AS
    ON INSERT TO samples_all 
    WHERE NEW.EXPE_ID IS NULL AND NEW.SPACE_ID IS NOT NULL AND NEW.PROJ_ID IS NULL
    DO ALSO (
      INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         SPACE_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.SPACE_ID, 
         (SELECT CODE FROM SPACES WHERE ID = NEW.SPACE_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
   );
-- end of rules for relationships history

---------------------------
-- Triggers for freezing
---------------------------

CREATE OR REPLACE FUNCTION RAISE_EXCEPTION_FROZEN_ENTITY_BY_CODE() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'Operation % is not allowed because % % is frozen.', TG_ARGV[0], TG_ARGV[1], OLD.code;
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION RAISE_EXCEPTION_FROZEN_SPACE_RELATIONSHIP() RETURNS trigger AS $$
DECLARE
    space_id   TECH_ID;
    operation  TEXT;
BEGIN
    IF (NEW.space_id IS NOT NULL AND NEW.space_frozen) THEN
        space_id = NEW.space_id;
        operation = 'SET SPACE';
    ELSEIF (OLD.space_id IS NOT NULL AND OLD.space_frozen) THEN
        space_id = OLD.space_id;
        operation = 'REMOVE SPACE';
    END IF;

    RAISE EXCEPTION 'Operation % is not allowed because % % and space % are frozen.', operation, TG_ARGV[0], NEW.code,
        (select code from spaces where id = space_id);
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION RAISE_EXCEPTION_FROZEN_PROJECT_RELATIONSHIP() RETURNS trigger AS $$
DECLARE
    project_id   TECH_ID;
    operation    TEXT;
BEGIN
    IF (NEW.proj_id IS NOT NULL AND NEW.proj_frozen) THEN
        project_id = NEW.proj_id;
        operation = 'SET PROJECT';
    ELSEIF (OLD.proj_id IS NOT NULL AND OLD.proj_frozen) THEN
        project_id = OLD.proj_id;
        operation = 'REMOVE PROJECT';
    END IF;

    RAISE EXCEPTION 'Operation % is not allowed because % % and project % are frozen.', operation, TG_ARGV[0], NEW.code,
        (select code from projects where id = project_id);
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION RAISE_EXCEPTION_FROZEN_EXPERIMENT_RELATIONSHIP() RETURNS trigger AS $$
DECLARE
    experiment_id   TECH_ID;
    operation       TEXT;
BEGIN
    IF (NEW.expe_id IS NOT NULL AND NEW.expe_frozen) THEN
        experiment_id = NEW.expe_id;
        operation = 'SET EXPERIMENT';
    ELSEIF (OLD.expe_id IS NOT NULL AND OLD.expe_frozen) THEN
        experiment_id = OLD.expe_id;
        operation = 'REMOVE EXPERIMENT';
    END IF;

    RAISE EXCEPTION 'Operation % is not allowed because % % and experiment % are frozen.', operation, TG_ARGV[0], NEW.code,
        (select code from experiments_all where id = experiment_id);
END;
$$ LANGUAGE 'plpgsql';

---------------------------
-- Triggers for freezing
---------------------------

CREATE OR REPLACE FUNCTION RAISE_EXCEPTION_FROZEN_ENTITY_BY_CODE() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'Operation % is not allowed because % % is frozen.', TG_ARGV[0], TG_ARGV[1], OLD.code;
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION RAISE_EXCEPTION_FROZEN_SPACE_RELATIONSHIP() RETURNS trigger AS $$
DECLARE
    space_id   TECH_ID;
    operation  TEXT;
BEGIN
    IF (NEW.space_id IS NOT NULL AND NEW.space_frozen) THEN
        space_id = NEW.space_id;
        operation = 'SET SPACE';
    ELSEIF (OLD.space_id IS NOT NULL AND OLD.space_frozen) THEN
        space_id = OLD.space_id;
        operation = 'REMOVE SPACE';
    END IF;

    RAISE EXCEPTION 'Operation % is not allowed because % % and space % are frozen.', operation, TG_ARGV[0], NEW.code,
        (select code from spaces where id = space_id);
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION RAISE_EXCEPTION_FROZEN_PROJECT_RELATIONSHIP() RETURNS trigger AS $$
DECLARE
    project_id   TECH_ID;
    operation    TEXT;
BEGIN
    IF (NEW.proj_id IS NOT NULL AND NEW.proj_frozen) THEN
        project_id = NEW.proj_id;
        operation = 'SET PROJECT';
    ELSEIF (OLD.proj_id IS NOT NULL AND OLD.proj_frozen) THEN
        project_id = OLD.proj_id;
        operation = 'REMOVE PROJECT';
    END IF;

    RAISE EXCEPTION 'Operation % is not allowed because % % and project % are frozen.', operation, TG_ARGV[0], NEW.code,
        (select code from projects where id = project_id);
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION RAISE_EXCEPTION_FROZEN_EXPERIMENT_RELATIONSHIP() RETURNS trigger AS $$
DECLARE
    experiment_id   TECH_ID;
    operation       TEXT;
BEGIN
    IF (NEW.expe_id IS NOT NULL AND NEW.expe_frozen) THEN
        experiment_id = NEW.expe_id;
        operation = 'SET EXPERIMENT';
    ELSEIF (OLD.expe_id IS NOT NULL AND OLD.expe_frozen) THEN
        experiment_id = OLD.expe_id;
        operation = 'REMOVE EXPERIMENT';
    END IF;

    RAISE EXCEPTION 'Operation % is not allowed because % % and experiment % are frozen.', operation, TG_ARGV[0], NEW.code,
        (select code from experiments_all where id = experiment_id);
END;
$$ LANGUAGE 'plpgsql';

-- Projects --------------------
-- Project deleting

DROP TRIGGER IF EXISTS PROJECT_FROZEN_CHECK_ON_DELETE ON PROJECTS;
CREATE TRIGGER PROJECT_FROZEN_CHECK_ON_DELETE BEFORE DELETE ON PROJECTS
    FOR EACH ROW WHEN (OLD.frozen)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_ENTITY_BY_CODE('DELETE', 'project');

-- Project update
DROP TRIGGER IF EXISTS PROJECT_FROZEN_CHECK_ON_UPDATE ON PROJECTS;
CREATE TRIGGER PROJECT_FROZEN_CHECK_ON_UPDATE BEFORE UPDATE ON PROJECTS
    FOR EACH ROW WHEN (OLD.frozen AND NEW.frozen)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_ENTITY_BY_CODE('UPDATE', 'project');

-- Project attachment inserting, updating and deleting
CREATE OR REPLACE FUNCTION RAISE_EXCEPTION_FROZEN_PROJECT() RETURNS trigger AS $$
DECLARE
    project_id   TECH_ID;
BEGIN
    IF (TG_OP = 'DELETE') THEN
        project_id = OLD.proj_id;
    ELSEIF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN
        project_id = NEW.proj_id;
    END IF;

    RAISE EXCEPTION 'Operation % % is not allowed because project % is frozen.', TG_OP, TG_ARGV[0],
        (select code from projects where id = project_id);
END;
$$ LANGUAGE 'plpgsql';
DROP TRIGGER IF EXISTS PROJECT_FROZEN_CHECK_ON_INSERT_ATTACHMENT ON ATTACHMENTS;
CREATE TRIGGER PROJECT_FROZEN_CHECK_ON_INSERT_ATTACHMENT BEFORE INSERT ON ATTACHMENTS
    FOR EACH ROW WHEN (NEW.PROJ_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_PROJECT('ATTACHMENT');

DROP TRIGGER IF EXISTS PROJECT_FROZEN_CHECK_ON_UPDATE_ATTACHMENT ON ATTACHMENTS;
CREATE TRIGGER PROJECT_FROZEN_CHECK_ON_UPDATE_ATTACHMENT BEFORE UPDATE ON ATTACHMENTS
    FOR EACH ROW WHEN (OLD.PROJ_FROZEN AND NEW.PROJ_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_PROJECT('ATTACHMENT');

DROP TRIGGER IF EXISTS PROJECT_FROZEN_CHECK_ON_DELETE_ATTACHMENT ON ATTACHMENTS;
CREATE TRIGGER PROJECT_FROZEN_CHECK_ON_DELETE_ATTACHMENT BEFORE DELETE ON ATTACHMENTS
    FOR EACH ROW WHEN (OLD.PROJ_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_PROJECT('ATTACHMENT');

-- Project space relationship
DROP TRIGGER IF EXISTS PROJECT_SPACE_RELATIONSHIP_FROZEN_CHECK ON PROJECTS;
CREATE TRIGGER PROJECT_SPACE_RELATIONSHIP_FROZEN_CHECK BEFORE UPDATE ON PROJECTS
    FOR EACH ROW WHEN (NEW.space_id <> OLD.space_id AND (NEW.SPACE_FROZEN OR OLD.SPACE_FROZEN) AND NEW.FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_SPACE_RELATIONSHIP('project');

-- Experiments --------------------
-- Experiment trashing and deleting

DROP TRIGGER IF EXISTS EXPERIMENT_FROZEN_CHECK_ON_TRASH ON EXPERIMENTS_ALL;
CREATE TRIGGER EXPERIMENT_FROZEN_CHECK_ON_TRASH BEFORE UPDATE ON EXPERIMENTS_ALL
    FOR EACH ROW WHEN (NEW.del_id IS NOT NULL AND OLD.del_id IS NULL AND OLD.frozen) 
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_ENTITY_BY_CODE('TRASH', 'experiment');
    
DROP TRIGGER IF EXISTS EXPERIMENT_FROZEN_CHECK_ON_DELETE ON EXPERIMENTS_ALL;
CREATE TRIGGER EXPERIMENT_FROZEN_CHECK_ON_DELETE BEFORE DELETE ON EXPERIMENTS_ALL
    FOR EACH ROW WHEN (OLD.frozen)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_ENTITY_BY_CODE('DELETE', 'experiment');

-- Experiment property inserting, updating and deleting
CREATE OR REPLACE FUNCTION RAISE_EXCEPTION_FROZEN_EXPERIMENT() RETURNS trigger AS $$
DECLARE
    experiment_id   TECH_ID;
BEGIN
    IF (TG_OP = 'DELETE') THEN
        experiment_id = OLD.expe_id;
    ELSEIF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN
        experiment_id = NEW.expe_id;
    END IF;

    RAISE EXCEPTION 'Operation % % is not allowed because experiment % is frozen.', TG_OP, TG_ARGV[0],
        (select code from experiments_all where id = experiment_id);
END;
$$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS EXPERIMENT_FROZEN_CHECK_ON_INSERT_PROPERTY ON EXPERIMENT_PROPERTIES;
CREATE TRIGGER EXPERIMENT_FROZEN_CHECK_ON_INSERT_PROPERTY BEFORE INSERT ON EXPERIMENT_PROPERTIES
    FOR EACH ROW WHEN (NEW.EXPE_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_EXPERIMENT('PROPERTY');

DROP TRIGGER IF EXISTS EXPERIMENT_FROZEN_CHECK_ON_CHANGE_PROPERTY ON EXPERIMENT_PROPERTIES;
CREATE TRIGGER EXPERIMENT_FROZEN_CHECK_ON_CHANGE_PROPERTY BEFORE UPDATE ON EXPERIMENT_PROPERTIES
    FOR EACH ROW WHEN (OLD.EXPE_FROZEN AND NEW.EXPE_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_EXPERIMENT('PROPERTY');

DROP TRIGGER IF EXISTS EXPERIMENT_FROZEN_CHECK_ON_DELETE_PROPERTY ON EXPERIMENT_PROPERTIES;
CREATE TRIGGER EXPERIMENT_FROZEN_CHECK_ON_DELETE_PROPERTY BEFORE DELETE ON EXPERIMENT_PROPERTIES
    FOR EACH ROW WHEN (OLD.EXPE_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_EXPERIMENT('PROPERTY');

-- Experiment attachment inserting, updating and deleting
DROP TRIGGER IF EXISTS EXPERIMENT_FROZEN_CHECK_ON_INSERT_ATTACHMENT ON ATTACHMENTS;
CREATE TRIGGER EXPERIMENT_FROZEN_CHECK_ON_INSERT_ATTACHMENT BEFORE INSERT ON ATTACHMENTS
    FOR EACH ROW WHEN (NEW.EXPE_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_EXPERIMENT('ATTACHMENT');

DROP TRIGGER IF EXISTS EXPERIMENT_FROZEN_CHECK_ON_UPDATE_ATTACHMENT ON ATTACHMENTS;
CREATE TRIGGER EXPERIMENT_FROZEN_CHECK_ON_UPDATE_ATTACHMENT BEFORE UPDATE ON ATTACHMENTS
    FOR EACH ROW WHEN (OLD.EXPE_FROZEN AND NEW.EXPE_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_EXPERIMENT('ATTACHMENT');

DROP TRIGGER IF EXISTS EXPERIMENT_FROZEN_CHECK_ON_DELETE_ATTACHMENT ON ATTACHMENTS;
CREATE TRIGGER EXPERIMENT_FROZEN_CHECK_ON_DELETE_ATTACHMENT BEFORE DELETE ON ATTACHMENTS
    FOR EACH ROW WHEN (OLD.EXPE_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_EXPERIMENT('ATTACHMENT');

-- Experiment project relationship
DROP TRIGGER IF EXISTS EXPERIMENT_PROJECT_RELATIONSHIP_FROZEN_CHECK ON EXPERIMENTS_ALL;
CREATE TRIGGER EXPERIMENT_PROJECT_RELATIONSHIP_FROZEN_CHECK BEFORE UPDATE ON EXPERIMENTS_ALL
    FOR EACH ROW WHEN (NEW.proj_id <> OLD.proj_id AND (NEW.PROJ_FROZEN OR OLD.PROJ_FROZEN) AND NEW.FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_PROJECT_RELATIONSHIP('experiment');

-- Samples --------------------
-- Sample trashing and deleting

DROP TRIGGER IF EXISTS SAMPLE_FROZEN_CHECK_ON_TRASH ON SAMPLES_ALL;
CREATE TRIGGER SAMPLE_FROZEN_CHECK_ON_TRASH BEFORE UPDATE ON SAMPLES_ALL
    FOR EACH ROW WHEN (NEW.del_id IS NOT NULL AND OLD.del_id IS NULL AND OLD.frozen) 
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_ENTITY_BY_CODE('TRASH', 'sample');
    
DROP TRIGGER IF EXISTS SAMPLE_FROZEN_CHECK_ON_DELETE ON SAMPLES_ALL;
CREATE TRIGGER SAMPLE_FROZEN_CHECK_ON_DELETE BEFORE DELETE ON SAMPLES_ALL
    FOR EACH ROW WHEN (OLD.frozen)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_ENTITY_BY_CODE('DELETE', 'sample');

-- Sample property inserting, updating and deleting
CREATE OR REPLACE FUNCTION RAISE_EXCEPTION_FROZEN_SAMPLE() RETURNS trigger AS $$
DECLARE
    sample_id   TECH_ID;
BEGIN
    IF (TG_OP = 'DELETE') THEN
        sample_id = OLD.samp_id;
    ELSEIF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN
        sample_id = NEW.samp_id;
    END IF;

    RAISE EXCEPTION 'Operation % % is not allowed because sample % is frozen.', TG_OP, TG_ARGV[0],
        (select code from samples_all where id = sample_id);
END;
$$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS SAMPLE_FROZEN_CHECK_ON_INSERT_PROPERTY ON SAMPLE_PROPERTIES;
CREATE TRIGGER SAMPLE_FROZEN_CHECK_ON_INSERT_PROPERTY BEFORE INSERT ON SAMPLE_PROPERTIES
    FOR EACH ROW WHEN (NEW.SAMP_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_SAMPLE('PROPERTY');

DROP TRIGGER IF EXISTS SAMPLE_FROZEN_CHECK_ON_CHANGE_PROPERTY ON SAMPLE_PROPERTIES;
CREATE TRIGGER SAMPLE_FROZEN_CHECK_ON_CHANGE_PROPERTY BEFORE UPDATE ON SAMPLE_PROPERTIES
    FOR EACH ROW WHEN (OLD.SAMP_FROZEN AND NEW.SAMP_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_SAMPLE('PROPERTY');

DROP TRIGGER IF EXISTS SAMPLE_FROZEN_CHECK_ON_DELETE_PROPERTY ON SAMPLE_PROPERTIES;
CREATE TRIGGER SAMPLE_FROZEN_CHECK_ON_DELETE_PROPERTY BEFORE DELETE ON SAMPLE_PROPERTIES
    FOR EACH ROW WHEN (OLD.SAMP_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_SAMPLE('PROPERTY');

-- Sample attachment inserting, updating and deleting
DROP TRIGGER IF EXISTS SAMPLE_FROZEN_CHECK_ON_INSERT_ATTACHMENT ON ATTACHMENTS;
CREATE TRIGGER SAMPLE_FROZEN_CHECK_ON_INSERT_ATTACHMENT BEFORE INSERT ON ATTACHMENTS
    FOR EACH ROW WHEN (NEW.SAMP_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_SAMPLE('ATTACHMENT');

DROP TRIGGER IF EXISTS SAMPLE_FROZEN_CHECK_ON_UPDATE_ATTACHMENT ON ATTACHMENTS;
CREATE TRIGGER SAMPLE_FROZEN_CHECK_ON_UPDATE_ATTACHMENT BEFORE UPDATE ON ATTACHMENTS
    FOR EACH ROW WHEN (OLD.SAMP_FROZEN AND NEW.SAMP_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_SAMPLE('ATTACHMENT');

DROP TRIGGER IF EXISTS SAMPLE_FROZEN_CHECK_ON_DELETE_ATTACHMENT ON ATTACHMENTS;
CREATE TRIGGER SAMPLE_FROZEN_CHECK_ON_DELETE_ATTACHMENT BEFORE DELETE ON ATTACHMENTS
    FOR EACH ROW WHEN (OLD.SAMP_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_SAMPLE('ATTACHMENT');

-- Sample container setting and removing
CREATE OR REPLACE FUNCTION RAISE_EXCEPTION_FROZEN_SAMPLE_CONTAINER_RELATIONSHIP() RETURNS trigger AS $$
DECLARE
    sample_id   TECH_ID;
    operation   TEXT;
BEGIN
    IF (NEW.samp_id_part_of IS NOT NULL AND NEW.CONT_FROZEN) THEN
        sample_id = NEW.samp_id_part_of;
        operation = 'SET CONTAINER';
    ELSEIF (OLD.samp_id_part_of IS NOT NULL AND OLD.CONT_FROZEN) THEN
        sample_id = OLD.samp_id_part_of;
        operation = 'REMOVE CONTAINER';
    END IF;

    RAISE EXCEPTION 'Operation % is not allowed because samples % and % are frozen.', operation, NEW.code,
        (select code from samples_all where id = sample_id);
END;
$$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS SAMPLE_FROZEN_CHECK_ON_SET_CONTAINER ON SAMPLES_ALL;
CREATE TRIGGER SAMPLE_FROZEN_CHECK_ON_SET_CONTAINER BEFORE UPDATE ON SAMPLES_ALL
    FOR EACH ROW WHEN (
        (NEW.samp_id_part_of <> OLD.samp_id_part_of
         OR (NEW.samp_id_part_of IS NOT NULL AND OLD.samp_id_part_of IS NULL)
         OR (NEW.samp_id_part_of IS NULL AND OLD.samp_id_part_of IS NOT NULL))
        AND (NEW.CONT_FROZEN OR OLD.CONT_FROZEN) AND NEW.FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_SAMPLE_CONTAINER_RELATIONSHIP();

-- Sample parent-child relationship inserting and deleting
CREATE OR REPLACE FUNCTION RAISE_EXCEPTION_FROZEN_SAMPLE_RELATIONSHIP() RETURNS trigger AS $$
DECLARE
    parent_id   TECH_ID;
    child_id    TECH_ID;
BEGIN
    IF (TG_OP = 'DELETE') THEN
        parent_id = OLD.sample_id_parent;
        child_id = OLD.sample_id_child;
    ELSEIF (TG_OP = 'INSERT') THEN
        parent_id = NEW.sample_id_parent;
        child_id = NEW.sample_id_child;
    END IF;
    RAISE EXCEPTION 'Operation % is not allowed because samples % and % are frozen.', TG_OP, 
        (select code from samples_all where id = parent_id),
        (select code from samples_all where id = child_id);
END;
$$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS SAMPLE_RELATIONSHIP_FROZEN_CHECK_ON_INSERT ON SAMPLE_RELATIONSHIPS_ALL;
CREATE TRIGGER SAMPLE_RELATIONSHIP_FROZEN_CHECK_ON_INSERT BEFORE INSERT ON SAMPLE_RELATIONSHIPS_ALL
    FOR EACH ROW WHEN (NEW.PARENT_FROZEN AND NEW.CHILD_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_SAMPLE_RELATIONSHIP();

DROP TRIGGER IF EXISTS SAMPLE_RELATIONSHIP_FROZEN_CHECK_ON_DELETE ON SAMPLE_RELATIONSHIPS_ALL;
CREATE TRIGGER SAMPLE_RELATIONSHIP_FROZEN_CHECK_ON_DELETE BEFORE DELETE ON SAMPLE_RELATIONSHIPS_ALL
    FOR EACH ROW WHEN (OLD.PARENT_FROZEN AND OLD.CHILD_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_SAMPLE_RELATIONSHIP();

-- Sample experiment relationship
DROP TRIGGER IF EXISTS SAMPLE_EXPERIMENT_RELATIONSHIP_FROZEN_CHECK ON SAMPLES_ALL;
CREATE TRIGGER SAMPLE_EXPERIMENT_RELATIONSHIP_FROZEN_CHECK BEFORE UPDATE ON SAMPLES_ALL
    FOR EACH ROW WHEN (
        (NEW.expe_id <> OLD.expe_id
         OR (NEW.expe_id IS NOT NULL AND OLD.expe_id IS NULL)
         OR (NEW.expe_id IS NULL AND OLD.expe_id IS NOT NULL))
        AND (NEW.EXPE_FROZEN OR OLD.EXPE_FROZEN) AND NEW.FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_EXPERIMENT_RELATIONSHIP('sample');

-- Sample project relationship
DROP TRIGGER IF EXISTS SAMPLE_PROJECT_RELATIONSHIP_FROZEN_CHECK ON SAMPLES_ALL;
CREATE TRIGGER SAMPLE_PROJECT_RELATIONSHIP_FROZEN_CHECK BEFORE UPDATE ON SAMPLES_ALL
    FOR EACH ROW WHEN (
        (NEW.proj_id <> OLD.proj_id
         OR (NEW.proj_id IS NOT NULL AND OLD.proj_id IS NULL)
         OR (NEW.proj_id IS NULL AND OLD.proj_id IS NOT NULL))
        AND (NEW.PROJ_FROZEN OR OLD.PROJ_FROZEN) AND NEW.FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_PROJECT_RELATIONSHIP('sample');

-- Sample space relationship
DROP TRIGGER IF EXISTS SAMPLE_SPACE_RELATIONSHIP_FROZEN_CHECK ON SAMPLES_ALL;
CREATE TRIGGER SAMPLE_SPACE_RELATIONSHIP_FROZEN_CHECK BEFORE UPDATE ON SAMPLES_ALL
    FOR EACH ROW WHEN (
        (NEW.space_id <> OLD.space_id
         OR (NEW.space_id IS NOT NULL AND OLD.space_id IS NULL)
         OR (NEW.space_id IS NULL AND OLD.space_id IS NOT NULL))
        AND (NEW.SPACE_FROZEN OR OLD.SPACE_FROZEN) AND NEW.FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_SPACE_RELATIONSHIP('sample');

-- Data Set --------------------
-- Data set trashing and deleting

DROP TRIGGER IF EXISTS DATA_SET_FROZEN_CHECK_ON_TRASH ON DATA_ALL;
CREATE TRIGGER DATA_SET_FROZEN_CHECK_ON_TRASH BEFORE UPDATE ON DATA_ALL
    FOR EACH ROW WHEN (NEW.del_id IS NOT NULL AND OLD.del_id IS NULL AND OLD.frozen) 
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_ENTITY_BY_CODE('TRASH', 'data set');
    
DROP TRIGGER IF EXISTS DATA_SET_FROZEN_CHECK_ON_DELETE ON DATA_ALL;
CREATE TRIGGER DATA_SET_FROZEN_CHECK_ON_DELETE BEFORE DELETE ON DATA_ALL
    FOR EACH ROW WHEN (OLD.frozen)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_ENTITY_BY_CODE('DELETE', 'data set');

-- Data set property inserting, updating and deleting
CREATE OR REPLACE FUNCTION RAISE_EXCEPTION_FROZEN_DATA_SET() RETURNS trigger AS $$
DECLARE
    ds_id   TECH_ID;
BEGIN
    IF (TG_OP = 'DELETE') THEN
        ds_id = OLD.ds_id;
    ELSEIF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN
        ds_id = NEW.ds_id;
    END IF;

    RAISE EXCEPTION 'Operation % % is not allowed because data set % is frozen.', TG_OP, TG_ARGV[0],
        (select code from data_all where id = ds_id);
END;
$$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS DATA_SET_FROZEN_CHECK_ON_INSERT_PROPERTY ON DATA_SET_PROPERTIES;
CREATE TRIGGER DATA_SET_FROZEN_CHECK_ON_INSERT_PROPERTY BEFORE INSERT ON DATA_SET_PROPERTIES
    FOR EACH ROW WHEN (NEW.DASE_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_DATA_SET('PROPERTY');

DROP TRIGGER IF EXISTS DATA_SET_FROZEN_CHECK_ON_CHANGE_PROPERTY ON DATA_SET_PROPERTIES;
CREATE TRIGGER DATA_SET_FROZEN_CHECK_ON_CHANGE_PROPERTY BEFORE UPDATE ON DATA_SET_PROPERTIES
    FOR EACH ROW WHEN (OLD.DASE_FROZEN AND NEW.DASE_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_DATA_SET('PROPERTY');

DROP TRIGGER IF EXISTS DATA_SET_FROZEN_CHECK_ON_DELETE_PROPERTY ON DATA_SET_PROPERTIES;
CREATE TRIGGER DATA_SET_FROZEN_CHECK_ON_DELETE_PROPERTY BEFORE DELETE ON DATA_SET_PROPERTIES
    FOR EACH ROW WHEN (OLD.DASE_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_DATA_SET('PROPERTY');

-- Data set parent-child relationship inserting and deleting
CREATE OR REPLACE FUNCTION RAISE_EXCEPTION_FROZEN_DATA_SET_RELATIONSHIP() RETURNS trigger AS $$
DECLARE
    parent_id   TECH_ID;
    child_id    TECH_ID;
BEGIN
    IF (TG_OP = 'DELETE') THEN
        parent_id = OLD.data_id_parent;
        child_id = OLD.data_id_child;
    ELSEIF (TG_OP = 'INSERT') THEN
        parent_id = NEW.data_id_parent;
        child_id = NEW.data_id_child;
    END IF;
    RAISE EXCEPTION 'Operation % is not allowed because data sets % and % are frozen.', TG_OP, 
        (select code from data_all where id = parent_id),
        (select code from data_all where id = child_id);
END;
$$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS DATA_SET_RELATIONSHIP_FROZEN_CHECK_ON_INSERT ON DATA_SET_RELATIONSHIPS_ALL;
CREATE TRIGGER DATA_SET_RELATIONSHIP_FROZEN_CHECK_ON_INSERT BEFORE INSERT ON DATA_SET_RELATIONSHIPS_ALL
    FOR EACH ROW WHEN (NEW.PARENT_FROZEN AND NEW.CHILD_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_DATA_SET_RELATIONSHIP();

DROP TRIGGER IF EXISTS DATA_SET_RELATIONSHIP_FROZEN_CHECK_ON_DELETE ON DATA_SET_RELATIONSHIPS_ALL;
CREATE TRIGGER DATA_SET_RELATIONSHIP_FROZEN_CHECK_ON_DELETE BEFORE DELETE ON DATA_SET_RELATIONSHIPS_ALL
    FOR EACH ROW WHEN (OLD.PARENT_FROZEN AND OLD.CHILD_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_DATA_SET_RELATIONSHIP();

-- Data set experiment relationship
DROP TRIGGER IF EXISTS DATA_SET_EXPERIMENT_RELATIONSHIP_FROZEN_CHECK_ON_UPDATE ON DATA_ALL;
CREATE TRIGGER DATA_SET_EXPERIMENT_RELATIONSHIP_FROZEN_CHECK_ON_UPDATE BEFORE UPDATE ON DATA_ALL
    FOR EACH ROW WHEN (
        (NEW.EXPE_ID <> OLD.EXPE_ID
         OR (NEW.EXPE_ID IS NOT NULL AND OLD.EXPE_ID IS NULL)
         OR (NEW.EXPE_ID IS NULL AND OLD.EXPE_ID IS NOT NULL))
        AND (NEW.EXPE_FROZEN OR OLD.EXPE_FROZEN) AND NEW.FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_EXPERIMENT_RELATIONSHIP('data set');

-- Data set sample relationship
CREATE OR REPLACE FUNCTION RAISE_EXCEPTION_FROZEN_DATA_SET_SAMPLE_RELATIONSHIP() RETURNS trigger AS $$
DECLARE
    sample_id   TECH_ID;
    operation   TEXT;
BEGIN
    IF (NEW.samp_id IS NOT NULL AND NEW.samp_frozen) THEN
        sample_id = NEW.samp_id;
        operation = 'SET SAMPLE';
    ELSEIF (OLD.samp_id IS NOT NULL AND OLD.samp_frozen) THEN
        sample_id = OLD.samp_id;
        operation = 'REMOVE SAMPLE';
    END IF;

    RAISE EXCEPTION 'Operation % is not allowed because data set % and sample % are frozen.', operation, NEW.code,
        (select code from samples_all where id = sample_id);
END;
$$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS DATA_SET_SAMPLE_RELATIONSHIP_FROZEN_CHECK_ON_UPDATE ON DATA_ALL;
CREATE TRIGGER DATA_SET_SAMPLE_RELATIONSHIP_FROZEN_CHECK_ON_UPDATE BEFORE UPDATE ON DATA_ALL
    FOR EACH ROW WHEN (
        (NEW.SAMP_ID <> OLD.SAMP_ID
         OR (NEW.SAMP_ID IS NOT NULL AND OLD.SAMP_ID IS NULL)
         OR (NEW.SAMP_ID IS NULL AND OLD.SAMP_ID IS NOT NULL))
        AND (NEW.SAMP_FROZEN OR OLD.SAMP_FROZEN) AND NEW.FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_DATA_SET_SAMPLE_RELATIONSHIP();

-- end of triggers for freezing