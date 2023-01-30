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
      select code into data_code from data where id = NEW.data_id; 
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

CREATE OR REPLACE FUNCTION SAMPLE_CODE_UNIQUENESS_CHECK() RETURNS trigger AS $$
DECLARE
   counter  INTEGER;
BEGIN
  LOCK TABLE samples IN EXCLUSIVE MODE;
  
	  IF (NEW.samp_id_part_of is NULL) THEN
		  IF (NEW.dbin_id is not NULL) THEN
			  SELECT count(*) into counter FROM samples 
		      where id != NEW.id and code = NEW.code and samp_id_part_of is NULL and dbin_id = NEW.dbin_id;
        IF (counter > 0) THEN
				  RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because database instance sample with the same code already exists.', NEW.code;
        END IF;
		  ELSIF (NEW.space_id is not NULL) THEN
			  SELECT count(*) into counter FROM samples 
				  where id != NEW.id and code = NEW.code and samp_id_part_of is NULL and space_id = NEW.space_id;
			  IF (counter > 0) THEN
				  RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because space sample with the same code already exists.', NEW.code;
			  END IF;
      END IF;
    ELSE
		  IF (NEW.dbin_id is not NULL) THEN
			  SELECT count(*) into counter FROM samples 
				  where id != NEW.id and code = NEW.code and samp_id_part_of = NEW.samp_id_part_of and dbin_id = NEW.dbin_id;
			  IF (counter > 0) THEN
				  RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because database instance sample with the same code and being the part of the same container already exists.', NEW.code;
			  END IF;
		  ELSIF (NEW.space_id is not NULL) THEN
			  SELECT count(*) into counter FROM samples 
				  where id != NEW.id and code = NEW.code and samp_id_part_of = NEW.samp_id_part_of and space_id = NEW.space_id;
			  IF (counter > 0) THEN
				  RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because space sample with the same code and being the part of the same container already exists.', NEW.code;
			  END IF;
		  END IF;
     END IF;   
  
  RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER SAMPLE_CODE_UNIQUENESS_CHECK BEFORE INSERT OR UPDATE ON SAMPLES
    FOR EACH ROW EXECUTE PROCEDURE SAMPLE_CODE_UNIQUENESS_CHECK();
    

CREATE OR REPLACE FUNCTION SAMPLE_SUBCODE_UNIQUENESS_CHECK() RETURNS trigger AS $$
DECLARE
   counter  INTEGER;
   unique_subcode  BOOLEAN_CHAR;
BEGIN
  LOCK TABLE samples IN EXCLUSIVE MODE;
  
  SELECT is_subcode_unique into unique_subcode FROM sample_types WHERE id = NEW.saty_id;
  
  IF (unique_subcode) THEN
    IF (NEW.dbin_id is not NULL) THEN
			SELECT count(*) into counter FROM samples 
				where id != NEW.id and code = NEW.code and saty_id = NEW.saty_id and dbin_id = NEW.dbin_id;
			IF (counter > 0) THEN
				RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because database instance sample of the same type with the same subcode already exists.', NEW.code;
			END IF;
		ELSIF (NEW.space_id is not NULL) THEN
			SELECT count(*) into counter FROM samples 
				where id != NEW.id and code = NEW.code and saty_id = NEW.saty_id and space_id = NEW.space_id;
			IF (counter > 0) THEN
				RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because space sample of the same type with the same subcode already exists.', NEW.code;
			END IF;
		END IF;
  END IF;
  
  RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER SAMPLE_SUBCODE_UNIQUENESS_CHECK BEFORE INSERT OR UPDATE ON SAMPLES
    FOR EACH ROW EXECUTE PROCEDURE SAMPLE_SUBCODE_UNIQUENESS_CHECK();
    
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
--- on insert/update - experiment, sample can't be deleted unless the data set is delete
---                  - parents/children relationship stays unchanged 

CREATE OR REPLACE FUNCTION check_created_or_modified_data_set_owner_is_alive() RETURNS trigger AS $$
DECLARE
	owner_code	CODE;
	owner_del_id	TECH_ID;
BEGIN
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
	AFTER INSERT OR UPDATE ON data
	DEFERRABLE INITIALLY DEFERRED
	FOR EACH ROW WHEN (NEW.del_id IS NULL)
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
  AFTER INSERT OR UPDATE ON samples
	DEFERRABLE INITIALLY DEFERRED
	FOR EACH ROW WHEN (NEW.del_id IS NULL)
	EXECUTE PROCEDURE check_created_or_modified_sample_owner_is_alive();
	
CREATE OR REPLACE FUNCTION check_deletion_consistency_on_sample_deletion() RETURNS trigger AS $$
DECLARE
  counter  INTEGER;
BEGIN
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
  AFTER UPDATE ON samples
	DEFERRABLE INITIALLY DEFERRED
	FOR EACH ROW 
	WHEN (OLD.del_id IS NULL AND NEW.del_id IS NOT NULL)
	EXECUTE PROCEDURE check_deletion_consistency_on_sample_deletion();	
	
----------------------------------------------------------------------------------------------------
-- 3. experiment
--- deletion -> all directly connected samples and data sets need to be deleted

CREATE OR REPLACE FUNCTION check_deletion_consistency_on_experiment_deletion() RETURNS trigger AS $$
DECLARE
  counter  INTEGER;
BEGIN
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
  AFTER UPDATE ON experiments
	DEFERRABLE INITIALLY DEFERRED
	FOR EACH ROW 
	WHEN (OLD.del_id IS NULL AND NEW.del_id IS NOT NULL)
	EXECUTE PROCEDURE check_deletion_consistency_on_experiment_deletion();