-- -------
-- Modify MATERIAL_BATCHES
-- -------

ALTER TABLE material_batches
    DROP COLUMN proc_id;

-- -------
-- Modify SAMPLES
-- -------

ALTER TABLE samples
    ADD COLUMN expe_id tech_id;

ALTER TABLE samples
    ADD CONSTRAINT samp_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments(id);

CREATE INDEX samp_expe_fk_i ON samples USING btree (expe_id);

-- relink samples directly to experiments

UPDATE samples
SET expe_id = (SELECT e.id FROM experiments e, procedures p, sample_inputs si 
               WHERE si.samp_id = samples.id AND si.proc_id = p.id AND p.expe_id = e.id AND e.inva_id IS NULL);


-- -------
-- Modify DATA
-- -------

ALTER TABLE data
    ADD COLUMN expe_id tech_id;

ALTER TABLE data
    ADD CONSTRAINT data_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments(id);

CREATE INDEX data_expe_fk_i ON data USING btree (expe_id);

UPDATE data
SET expe_id = (SELECT e.id FROM experiments e, procedures p 
               WHERE data.proc_id_produced_by = p.id AND p.expe_id = e.id);

ALTER TABLE data
    ALTER COLUMN expe_id SET NOT NULL;

ALTER TABLE data
    DROP COLUMN proc_id_produced_by;

-- -------
-- Drop PROCEDURES, PROCEDURE_TYPES, and SAMPLE_INPUTS
-- -------

DROP TABLE sample_inputs;
DROP TABLE procedures;
DROP TABLE procedure_types;

DROP SEQUENCE procedure_id_seq;
DROP SEQUENCE procedure_type_id_seq;

-- -------
-- Add CODE_SEQ
-- -------

CREATE SEQUENCE CODE_SEQ;

-- -------
-- Add new columns to DATA_STORES
-- -------

ALTER TABLE data_stores
    ADD COLUMN remote_url character varying(250),
    ADD COLUMN session_token character varying(50),
    ADD COLUMN modification_timestamp time_stamp DEFAULT now();

UPDATE data_stores
SET remote_url = '', session_token='';

ALTER TABLE data_stores
    ALTER COLUMN remote_url SET NOT NULL,
    ALTER COLUMN session_token SET NOT NULL;

-- -------
-- Drop foreign keys onto DATA_STORES
-- -------

ALTER TABLE database_instances
    DROP COLUMN dast_id;

ALTER TABLE groups
    DROP COLUMN dast_id;

ALTER TABLE projects
    DROP COLUMN dast_id;

ALTER TABLE experiments
    DROP COLUMN dast_id;

-- -------
-- Add foreign key from DATA onto DATA_STORES
-- -------

ALTER TABLE data
    ADD COLUMN dast_id tech_id;

ALTER TABLE data
    ADD CONSTRAINT data_dast_fk FOREIGN KEY (dast_id) REFERENCES data_stores(id);

UPDATE data
SET dast_id = (select id from data_stores where code = 'STANDARD');

ALTER TABLE data
    ALTER COLUMN dast_id SET NOT NULL;


------------------------------------------------------------------------------------
--  Purpose:  Replace trigger SAMPLE_CODE_UNIQUENESS_CHECK 
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

------------------------------------------------------------------------------------
--  Purpose:  allow longer user ids 
------------------------------------------------------------------------------------

alter table persons alter column user_id type varchar(50);
drop domain user_id;
create domain user_id as varchar(50);
alter table persons alter column user_id type user_id;


