-- Migration from 099 to 100

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