-- JAVA ch.systemsx.cisd.openbis.generic.server.dataaccess.migration.MigrationStepFrom022To023

-- Add column GLOBAL_CODE to the DATABASE_INSTANCES table.
-- Set it's value from the code, it will be updated later from java.

ALTER TABLE DATABASE_INSTANCES ADD COLUMN GLOBAL_CODE CODE;
update DATABASE_INSTANCES set GLOBAL_CODE = CODE;
ALTER TABLE DATABASE_INSTANCES ADD CONSTRAINT DBIN_GLOBAL_CODE_UK UNIQUE(GLOBAL_CODE);
ALTER TABLE DATABASE_INSTANCES alter column GLOBAL_CODE set NOT NULL;

-- Add column the new FK column SAMP_ID_PART_OF of the SAMPLES table

ALTER TABLE SAMPLES ADD COLUMN SAMP_ID_PART_OF TECH_ID;

-- Drop constraint SAMP_BK_DBIN_UK of SAMPLES table

ALTER TABLE SAMPLES DROP CONSTRAINT SAMP_BK_DBIN_UK;

-- Drop constraint SAMP_BK_GROU_UK of SAMPLES table

ALTER TABLE SAMPLES DROP CONSTRAINT SAMP_BK_GROU_UK;

-- Add FK PART_OF to the SAMPLES table

ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_SAMP_FK_PART_OF FOREIGN KEY (SAMP_ID_PART_OF) REFERENCES SAMPLES(ID);

-- Add Index for the FK Part Of of the SAMPLES table
CREATE INDEX SAMP_SAMP_FK_I_PART_OF ON SAMPLES (SAMP_ID_PART_OF);

-- Create the SAMPLE_MATERIAL_BATCHES table

CREATE TABLE SAMPLE_MATERIAL_BATCHES (ID TECH_ID NOT NULL,SAMP_ID TECH_ID NOT NULL,MABA_ID TECH_ID NOT NULL);
ALTER TABLE SAMPLE_MATERIAL_BATCHES ADD CONSTRAINT SAMB_PK PRIMARY KEY(ID);

-- Create the sequence for the SAMPLE_MATERIAL_BATCHES table

CREATE SEQUENCE SAMPLE_MATERIAL_BATCH_ID_SEQ;


--  Create WELL as a new SAMPLE_TYPE

insert into sample_types
(id
,code
,description
,dbin_id)
values 
(nextval('SAMPLE_TYPE_ID_SEQ')
,'WELL'
,'Plate Well'
,(select id from database_instances where is_original_source = true)
);


-- Transfer data from the SAMPLE_COMPONENTS table to the SAMPLES table

INSERT INTO samples(
              id
            , code
            , samp_id_top
            , samp_id_generated_from
            , saty_id
            , pers_id_registerer
            , inva_id
            , samp_id_control_layout
            , dbin_id
            , grou_id
            , samp_id_part_of)
SELECT        nextval('sample_id_seq')
            , saco.code
            , null
            , null
            , (select id from sample_types where code = 'WELL')
            , (select id from persons where user_id ='system')
            , samp.inva_id
            , null
            , samp.dbin_id
            , samp.grou_id
            , saco.samp_id
FROM samples samp INNER JOIN sample_components saco
     ON   samp.id = saco.samp_id;


-- Fill the SAMPLE_MATERIAL_BATCHES table via SAMPLE_C0MPONENT_MATERIALS, SAMPLE_C0MPONENTS, SAMPLES and MATERIAL_BATCHES tables

INSERT INTO sample_material_batches(
              id
            , samp_id
            , maba_id)
SELECT        nextval('sample_material_batch_id_seq')
            , well.id
            , scma.maba_id
FROM sample_component_materials scma 
   INNER JOIN material_batches maba
     ON scma.maba_id = maba.id
        INNER JOIN sample_components saco
           ON scma.saco_id = saco.id
              INNER JOIN samples well
                 ON saco.code = well.code and saco.samp_id = well.samp_id_part_of
;


-- Drop the retired Database Objects

DROP TABLE SAMPLE_COMPONENT_MATERIALS;
DROP TABLE SAMPLE_COMPONENTS;

DROP SEQUENCE SCMA_ID_SEQ;
DROP SEQUENCE SAMPLE_COMPONENT_ID_SEQ;

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
				where id != NEW.id and code = NEW.code and dbin_id = NEW.dbin_id;
			IF (counter > 0) THEN
				RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because database instance sample with the same code already exists.', NEW.code;
			END IF;
		ELSIF (NEW.grou_id is not NULL) THEN
			SELECT count(*) into counter FROM samples 
				where id != NEW.id and code = NEW.code and grou_id = NEW.grou_id;
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
