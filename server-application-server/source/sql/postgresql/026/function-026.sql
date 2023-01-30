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