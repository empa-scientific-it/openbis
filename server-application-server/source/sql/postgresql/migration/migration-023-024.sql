-- JAVA ch.systemsx.cisd.openbis.generic.server.dataaccess.migration.MigrationStepFrom023To024

-- Drop constraint DBIN_GLOBAL_CODE_UK of DATABASE_INSTANCES table

ALTER TABLE DATABASE_INSTANCES DROP CONSTRAINT DBIN_GLOBAL_CODE_UK;

-- Name changes

ALTER TABLE DATABASE_INSTANCES RENAME COLUMN GLOBAL_CODE TO UUID;

-- Add constraint DBIN_UUID_UK to the DATABASE_INSTANCES table

ALTER TABLE DATABASE_INSTANCES ADD CONSTRAINT DBIN_UUID_UK UNIQUE(UUID);

-- Drop column in EXPERIMENTS

ALTER TABLE EXPERIMENTS DROP COLUMN DESCRIPTION;

-- Add index to SAMPLES table

CREATE INDEX SAMP_CODE_I ON SAMPLES (CODE);

-- Bug fix in trigger function SAMPLE_CODE_UNIQUENESS_CHECK()

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
