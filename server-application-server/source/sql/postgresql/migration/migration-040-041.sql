-- Create FILTERS table

CREATE TABLE FILTERS (ID TECH_ID NOT NULL, DBIN_ID TECH_ID NOT NULL, NAME VARCHAR(200) NOT NULL, DESCRIPTION DESCRIPTION_1000,REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP, PERS_ID_REGISTERER TECH_ID NOT NULL, MODIFICATION_TIMESTAMP TIME_STAMP DEFAULT CURRENT_TIMESTAMP, EXPRESSION VARCHAR(2000) NOT NULL, IS_PUBLIC BOOLEAN NOT NULL, GRID_ID VARCHAR(200) NOT NULL);
CREATE SEQUENCE FILTER_ID_SEQ;
ALTER TABLE FILTERS ADD CONSTRAINT FILT_PK PRIMARY KEY(ID);
ALTER TABLE FILTERS ADD CONSTRAINT FILT_BK_UK UNIQUE(NAME, DBIN_ID, GRID_ID);
ALTER TABLE FILTERS ADD CONSTRAINT FILT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);
ALTER TABLE FILTERS ADD CONSTRAINT FILT_DBIN_FK FOREIGN KEY (DBIN_ID) REFERENCES DATABASE_INSTANCES(ID);
CREATE INDEX FILT_PERS_FK_I ON FILTERS (PERS_ID_REGISTERER);
CREATE INDEX FILT_DBIN_FK_I ON FILTERS (DBIN_ID);
GRANT SELECT ON SEQUENCE filter_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE filters TO GROUP OPENBIS_READONLY;

-- Add missing foreign key to role assignments authorization group
ALTER TABLE ROLE_ASSIGNMENTS ADD CONSTRAINT ROAS_AG_FK_GRANTEE FOREIGN KEY (AG_ID_GRANTEE) REFERENCES AUTHORIZATION_GROUPS(ID);

--------------------------------------------------------------------------------------------------
-- Modify dataset connections:
-- 1. connection with sample shouldn't be mandatory any more
-- 2. introduce an arc condition that dataset cannot be connected with a sample and a parent dataset at the same time
--------------------------------------------------------------------------------------------------

-- Weaken data-sample connection constraint - allow dataset to have no connection with sample
ALTER TABLE data ALTER COLUMN samp_id DROP NOT NULL;

-- Remove data-sample connection for datasets that have a parent

UPDATE data 
   SET samp_id = NULL 
 WHERE id IN (SELECT DISTINCT data_id_child FROM data_set_relationships);

-- With PostgreSQL 8.4 one can check that before migration there were no cycles in dataset relationships:
--
-- WITH RECURSIVE data_set_parents(id, parent_ids, cycle) AS (
--         SELECT  r.data_id_child AS id, 
--                 ARRAY[CAST (r.data_id_parent AS bigint)] AS parent_ids, 
--                 false AS cycle 
--         FROM data_set_relationships r
--     UNION ALL
--         SELECT  r.data_id_child,
--                 CAST (r.data_id_parent AS bigint) || p.parent_ids,
--                 r.data_id_child = ANY(p.parent_ids)
--         FROM data_set_relationships r, data_set_parents p
--         WHERE r.data_id_parent = p.id AND NOT cycle
-- )
-- SELECT count(*) AS cycles FROM data_set_parents WHERE cycle = true;

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