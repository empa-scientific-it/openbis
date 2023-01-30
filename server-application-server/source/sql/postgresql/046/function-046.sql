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
--  Purpose:  Create trigger SAMPLE_CODE_UNIQUENESS_CHECK 
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
		ELSIF (NEW.grou_id is not NULL) THEN
			SELECT count(*) into counter FROM samples 
				where id != NEW.id and code = NEW.code and samp_id_part_of is NULL and grou_id = NEW.grou_id;
			IF (counter > 0) THEN
				RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because group sample with the same code already exists.', NEW.code;
			END IF;
		END IF;
        ELSE
		IF (NEW.dbin_id is not NULL) THEN
			SELECT count(*) into counter FROM samples 
				where id != NEW.id and code = NEW.code and samp_id_part_of = NEW.samp_id_part_of and dbin_id = NEW.dbin_id;
			IF (counter > 0) THEN
				RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because database instance sample with the same code and being the part of the same parent already exists.', NEW.code;
			END IF;
		ELSIF (NEW.grou_id is not NULL) THEN
			SELECT count(*) into counter FROM samples 
				where id != NEW.id and code = NEW.code and samp_id_part_of = NEW.samp_id_part_of and grou_id = NEW.grou_id;
			IF (counter > 0) THEN
				RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because group sample with the same code and being the part of the same parent already exists.', NEW.code;
			END IF;
		END IF;
        END IF;   
   RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER SAMPLE_CODE_UNIQUENESS_CHECK BEFORE INSERT OR UPDATE ON SAMPLES
    FOR EACH ROW EXECUTE PROCEDURE SAMPLE_CODE_UNIQUENESS_CHECK();
    
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
    
---------------------------------------------------------------------------------------------------
--  Purpose:  Create DEFERRED triggers:
--            * check_dataset_relationships_on_data_table_modification,
--            * check_dataset_relationships_on_relationships_table_modification.
--            They check that after all modifications of database (just before commit) 
--            if 'data'/'data_set_relationships' tables are among modified tables 
--            dataset is not connected with a sample and a parent dataset at the same time.
--            This connections are held in two different tables so simple immediate trigger 
--            with arc check cannot be used and we need two deferred triggers.
----------------------------------------------------------------------------------------------------

-- trigger for 'data' table

CREATE OR REPLACE FUNCTION check_dataset_relationships_on_data_table_modification() RETURNS trigger AS $$
DECLARE
	counter	INTEGER;
BEGIN
	-- if there is a connection with a Sample there should not be any connection with a parent Data Set
	IF (NEW.samp_id IS NOT NULL) THEN
		-- count number of parents
		SELECT count(*) INTO counter 
			FROM data_set_relationships 
			WHERE data_id_child = NEW.id;
		IF (counter > 0) THEN
			RAISE EXCEPTION 'Insert/Update of Data Set (Code: %) failed because it cannot be connected with a Sample and a parent Data Set at the same time.', NEW.code;
		END IF;
	END IF;
  RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE CONSTRAINT TRIGGER check_dataset_relationships_on_data_table_modification 
  AFTER INSERT OR UPDATE ON data
	DEFERRABLE INITIALLY DEFERRED
	FOR EACH ROW 
	EXECUTE PROCEDURE check_dataset_relationships_on_data_table_modification();

-- trigger for 'data_set_relationships'

CREATE OR REPLACE FUNCTION check_dataset_relationships_on_relationships_table_modification() RETURNS trigger AS $$
DECLARE
	counter	INTEGER;
	sample_id	TECH_ID;
	data_code	CODE;
BEGIN
	-- child will have a parent added so it should not be connected with any sample
	SELECT samp_id, code INTO sample_id, data_code 
		FROM data 
		WHERE id = NEW.data_id_child;
	IF (sample_id IS NOT NULL) THEN
		RAISE EXCEPTION 'Insert/Update of Data Set (Code: %) failed because it cannot be connected to a Sample and to a parent Data Set at the same time.', data_code;
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';
  
CREATE CONSTRAINT TRIGGER check_dataset_relationships_on_relationships_table_modification 
  AFTER INSERT OR UPDATE ON data_set_relationships
	DEFERRABLE INITIALLY DEFERRED
	FOR EACH ROW 
	EXECUTE PROCEDURE check_dataset_relationships_on_relationships_table_modification();