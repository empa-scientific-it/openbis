-- -------
-- Change attachment table names, they should no longer mention the experiment
-- -------

ALTER TABLE experiment_attachment_contents RENAME TO attachment_contents;
ALTER TABLE experiment_attachments RENAME TO attachments;

ALTER TABLE EXPERIMENT_ATTACHMENT_ID_SEQ RENAME TO ATTACHMENT_ID_SEQ;
ALTER TABLE EXPERIMENT_ATTACHMENT_CONTENT_ID_SEQ RENAME TO ATTACHMENT_CONTENT_ID_SEQ;

ALTER INDEX EXAT_EXPE_FK_I RENAME TO ATTA_EXPE_FK_I;
ALTER INDEX EXAT_PERS_FK_I RENAME TO ATTA_PERS_FK_I;
-- Index couldn't be renamed because it might not exist
DROP INDEX IF EXISTS EXAT_EXAC_FK_I; 
CREATE INDEX ATTA_EXAC_FK_I ON ATTACHMENTS (EXAC_ID);

-- it's not possible to rename constraints, we drop and create them with different names

ALTER TABLE attachments
	DROP CONSTRAINT exat_cont_fk;
ALTER TABLE attachments
	DROP CONSTRAINT exat_expe_fk;
ALTER TABLE attachments
	DROP CONSTRAINT exat_pers_fk;
ALTER TABLE attachments
	DROP CONSTRAINT exat_pk;

ALTER TABLE attachments
	ADD CONSTRAINT atta_pk PRIMARY KEY (id);

ALTER TABLE attachments
	ADD CONSTRAINT atta_cont_fk FOREIGN KEY (exac_id) REFERENCES attachment_contents(id);

ALTER TABLE attachments
	ADD CONSTRAINT atta_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments(id);

ALTER TABLE attachments
	ADD CONSTRAINT atta_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);

-- -------
-- Add an arc connection from attachment to project and sample tables
-- -------

ALTER TABLE attachments
	ADD COLUMN samp_id tech_id,
	ADD COLUMN proj_id tech_id,
	ALTER COLUMN expe_id DROP NOT NULL;
	
ALTER TABLE attachments
	DROP CONSTRAINT exat_bk_uk;
	
ALTER TABLE attachments
	ADD CONSTRAINT atta_arc_ck CHECK ((((((expe_id IS NOT NULL) AND (proj_id IS NULL)) AND (samp_id IS NULL)) OR (((expe_id IS NULL) AND (proj_id IS NOT NULL)) AND (samp_id IS NULL))) OR (((expe_id IS NULL) AND (proj_id IS NULL)) AND (samp_id IS NOT NULL))));

ALTER TABLE attachments
	ADD CONSTRAINT atta_expe_bk_uk UNIQUE (expe_id, file_name, version);

ALTER TABLE attachments
	ADD CONSTRAINT atta_proj_bk_uk UNIQUE (proj_id, file_name, version);

ALTER TABLE attachments
	ADD CONSTRAINT atta_samp_bk_uk UNIQUE (samp_id, file_name, version);

ALTER TABLE attachments
	ADD CONSTRAINT atta_proj_fk FOREIGN KEY (proj_id) REFERENCES projects(id);

ALTER TABLE attachments
	ADD CONSTRAINT atta_samp_fk FOREIGN KEY (samp_id) REFERENCES samples(id);

CREATE INDEX ATTA_SAMP_FK_I ON ATTACHMENTS (SAMP_ID);
CREATE INDEX ATTA_PROJ_FK_I ON ATTACHMENTS (PROJ_ID);

-- -------
-- Add modification timestamp to project table to allow edition
-- -------

ALTER TABLE projects
	ADD COLUMN modification_timestamp TIME_STAMP DEFAULT CURRENT_TIMESTAMP;
	
-- -------
-- Add new datatypes
-- -------

insert into data_types
(id
 ,code
 ,description)
 values 
 (nextval('DATA_TYPE_ID_SEQ')
 ,'HYPERLINK'
 ,'Address of a web page'
);

insert into data_types
(id
 ,code
 ,description)
 values 
 (nextval('DATA_TYPE_ID_SEQ')
 ,'MULTILINE_VARCHAR'
 ,'Long text'
);

-- -------
-- Added explicit lock Samples table in exclusive mode to prevent race condition
-- -------

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
