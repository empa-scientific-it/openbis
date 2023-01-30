-- Migration from 119 to 120

-- drop old functions and triggers that checked sample code and subcode uniqueness
DROP TRIGGER sample_code_uniqueness_check ON samples_all;
DROP TRIGGER sample_subcode_uniqueness_check ON samples_all;

DROP FUNCTION sample_code_uniqueness_check();
DROP FUNCTION sample_subcode_uniqueness_check();


-- add columns that will store information necessary for efficient sample code and subcode uniqueness checks
ALTER TABLE samples_All ADD COLUMN code_unique_check character varying(300);
ALTER TABLE samples_All ADD COLUMN subcode_unique_check character varying(300);


-- add unique constraints on the new columns
ALTER TABLE samples_all
  ADD CONSTRAINT samp_code_unique_check_uk UNIQUE(code_unique_check);
ALTER TABLE samples_all
  ADD CONSTRAINT samp_subcode_unique_check_uk UNIQUE(subcode_unique_check);

  
-- function that fills in samples_all.code_unique_check column
CREATE OR REPLACE FUNCTION sample_fill_code_unique_check()
  RETURNS trigger AS
$BODY$
BEGIN
  NEW.code_unique_check = NEW.code || ',' || coalesce(NEW.samp_id_part_of, -1) || ',' || coalesce(NEW.space_id, -1) || ',' || coalesce(NEW.dbin_id, -1);
  RETURN NEW;
END;
$BODY$
  LANGUAGE 'plpgsql';

  
-- function that fills in samples_all.subcode_unique_check column
CREATE OR REPLACE FUNCTION sample_fill_subcode_unique_check()
  RETURNS trigger AS
$BODY$
DECLARE
    unique_subcode  BOOLEAN_CHAR;
BEGIN
    SELECT is_subcode_unique into unique_subcode FROM sample_types WHERE id = NEW.saty_id;
    
    IF (unique_subcode) THEN
    NEW.subcode_unique_check = NEW.code || ',' || coalesce(NEW.saty_id, -1) || ',' || coalesce(NEW.space_id, -1) || ',' || coalesce(NEW.dbin_id, -1);
    ELSE
    NEW.subcode_unique_check = NULL;
  END IF;
  
  RETURN NEW;
END;
$BODY$
  LANGUAGE 'plpgsql';

  
-- function that triggers recalculation of samples_all.subcode_unique_check column on sample_types.is_subcode_unique change
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

  
-- triggers for the above functions  
CREATE TRIGGER sample_fill_code_unique_check
  BEFORE INSERT OR UPDATE
  ON samples_all
  FOR EACH ROW
  EXECUTE PROCEDURE sample_fill_code_unique_check();

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

  
-- automatically switch sample_type.is_subcode_unique flag to false when there are samples with duplicated codes for that type already 
-- (it was possible before by creating samples with duplicated codes first and then switching sample_type.is_subcode_unique to true)

CREATE OR REPLACE FUNCTION FIX_IS_SUBCODE_UNIQUE_CONSISTENCY() RETURNS void AS $BODY$
DECLARE
  sample_type_record RECORD;
  sample_count INTEGER; 
BEGIN
  
    FOR sample_type_record IN SELECT * FROM sample_types WHERE is_subcode_unique = true LOOP
        select count(*) as c into sample_count from samples_all where saty_id = sample_type_record.id group by code, saty_id, space_id, dbin_id order by c desc limit 1;
        IF (sample_count > 1) THEN
          update sample_types set is_subcode_unique = false where id = sample_type_record.id;     
        END IF;
    END LOOP;
  
END;
$BODY$ LANGUAGE 'plpgsql';

SELECT FIX_IS_SUBCODE_UNIQUE_CONSISTENCY();
DROP function FIX_IS_SUBCODE_UNIQUE_CONSISTENCY();


-- fill in samples_all.code_unique_check and samples_all.subcode_unique_check columns for existing samples
UPDATE samples_all SET code_unique_check = code_unique_check;
