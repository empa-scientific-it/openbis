
CREATE OR REPLACE FUNCTION sample_code_uniqueness_check() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
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
$$;


CREATE OR REPLACE FUNCTION sample_subcode_uniqueness_check() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
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
$$;

ALTER TABLE groups RENAME TO spaces;

ALTER SEQUENCE group_id_seq RENAME TO space_id_seq;

ALTER TABLE persons	RENAME grou_id TO space_id;
ALTER TABLE projects RENAME grou_id TO space_id;
ALTER TABLE role_assignments RENAME grou_id TO space_id;
ALTER TABLE samples	RENAME grou_id TO space_id;

-- migrate EVENTS table

ALTER TABLE events
	DROP CONSTRAINT evnt_et_enum_ck;

update events set entity_type = 'SPACE' where entity_type = 'GROUP';

ALTER TABLE events ADD CONSTRAINT evnt_et_enum_ck CHECK 
	(entity_type IN ('ATTACHMENT', 'DATASET', 'EXPERIMENT', 'SPACE', 'MATERIAL', 'PROJECT', 'PROPERTY_TYPE', 'SAMPLE', 'VOCABULARY', 'AUTHORIZATION_GROUP')); 

ALTER TABLE persons
	DROP CONSTRAINT pers_grou_fk;

ALTER TABLE persons
	ADD CONSTRAINT pers_space_fk FOREIGN KEY (space_id) REFERENCES spaces(id);

ALTER TABLE projects
	DROP CONSTRAINT proj_bk_uk;

ALTER TABLE projects
	DROP CONSTRAINT proj_grou_fk;

ALTER TABLE projects
	ADD CONSTRAINT proj_bk_uk UNIQUE (code, space_id);

ALTER TABLE projects
	ADD CONSTRAINT proj_space_fk FOREIGN KEY (space_id) REFERENCES spaces(id);

ALTER TABLE role_assignments
	DROP CONSTRAINT roas_dbin_grou_arc_ck;

ALTER TABLE role_assignments
	DROP CONSTRAINT roas_ag_group_bk_uk;

ALTER TABLE role_assignments
	DROP CONSTRAINT roas_pe_group_bk_uk;

ALTER TABLE role_assignments
	DROP CONSTRAINT roas_grou_fk;

ALTER TABLE role_assignments
	ADD CONSTRAINT roas_dbin_space_arc_ck CHECK ((((dbin_id IS NOT NULL) AND (space_id IS NULL)) OR ((dbin_id IS NULL) AND (space_id IS NOT NULL))));

ALTER TABLE role_assignments
	ADD CONSTRAINT roas_ag_space_bk_uk UNIQUE (ag_id_grantee, role_code, space_id);

ALTER TABLE role_assignments
	ADD CONSTRAINT roas_pe_space_bk_uk UNIQUE (pers_id_grantee, role_code, space_id);

ALTER TABLE role_assignments
	ADD CONSTRAINT roas_space_fk FOREIGN KEY (space_id) REFERENCES spaces(id);

ALTER TABLE samples
	DROP CONSTRAINT samp_dbin_grou_arc_ck;

ALTER TABLE samples
	DROP CONSTRAINT samp_grou_fk;

ALTER TABLE samples
	ADD CONSTRAINT samp_dbin_space_arc_ck CHECK ((((dbin_id IS NOT NULL) AND (space_id IS NULL)) OR ((dbin_id IS NULL) AND (space_id IS NOT NULL))));

ALTER TABLE samples
	ADD CONSTRAINT samp_space_fk FOREIGN KEY (space_id) REFERENCES spaces(id);

ALTER TABLE spaces
  DROP CONSTRAINT grou_bk_uk,
  ADD CONSTRAINT space_bk_uk UNIQUE (code, dbin_id);

ALTER TABLE spaces
  DROP CONSTRAINT grou_dbin_fk,
  ADD CONSTRAINT space_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);

ALTER TABLE spaces
  DROP CONSTRAINT grou_pers_fk_registerer,
  ADD CONSTRAINT space_pers_fk_registerer FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);

ALTER INDEX pers_grou_fk_i RENAME TO pers_space_fk_i;

ALTER INDEX proj_grou_fk_i RENAME TO proj_space_fk_i;

ALTER INDEX roas_grou_fk_i RENAME TO roas_space_fk_i;

ALTER INDEX grou_pk RENAME TO space_pk;

ALTER INDEX grou_dbin_fk_i RENAME TO space_dbin_fk_i;

ALTER INDEX grou_pers_registered_by_fk_i RENAME TO space_pers_registered_by_fk_i;

