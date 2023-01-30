CREATE DOMAIN LOCATION_TYPE AS TEXT CHECK (VALUE IN ('OPENBIS', 'URL', 'FILE_SYSTEM_PLAIN', 'FILE_SYSTEM_GIT'));

ALTER TABLE link_data RENAME TO content_copies;

ALTER TABLE content_copies 
  DROP CONSTRAINT lnda_pk,
  DROP CONSTRAINT lnda_data_fk;


CREATE TABLE link_data (
    data_id tech_id NOT NULL
);

ALTER TABLE link_data
  ADD CONSTRAINT lnda_pk PRIMARY KEY(data_id),
  ADD CONSTRAINT lnda_data_fk FOREIGN KEY (data_id) REFERENCES data_all(id) ON DELETE CASCADE;

INSERT INTO link_data SELECT DISTINCT(data_id) FROM content_copies;

CREATE SEQUENCE content_copies_id_seq;

ALTER TABLE content_copies 
  ADD COLUMN id TECH_ID NOT NULL DEFAULT nextval('content_copies_id_seq'),
  ADD COLUMN location_type LOCATION_TYPE,
  ADD COLUMN path TEXT_VALUE,
  ADD COLUMN git_commit_hash TEXT_VALUE,
  ADD COLUMN location_unique_check TEXT_VALUE,
  ALTER COLUMN external_code DROP NOT NULL,
  ADD CONSTRAINT coco_pk PRIMARY KEY(ID),
  ADD CONSTRAINT coco_data_fk FOREIGN KEY (data_id) REFERENCES link_data(data_id);
  
ALTER TABLE content_copies 
  RENAME CONSTRAINT lnda_edms_fk TO coco_edms_fk;
  
UPDATE content_copies SET location_type = 'OPENBIS' where edms_id IN
  (SELECT id FROM external_data_management_systems WHERE address_type = 'OPENBIS');

UPDATE content_copies SET location_type = 'URL' where edms_id IN
  (SELECT id FROM external_data_management_systems WHERE address_type = 'URL');
  
UPDATE content_copies SET location_unique_check = 
  data_id || ',' || 
  edms_id || ',' || 
  coalesce(path, '') || ',' || 
  coalesce(git_commit_hash, '') || ',' || 
  coalesce(external_code, '');
  
ALTER TABLE content_copies
  ALTER COLUMN id DROP DEFAULT,
  ALTER COLUMN location_type SET NOT NULL,
  ALTER COLUMN location_unique_check SET NOT NULL,
  ADD CONSTRAINT content_copies_unique_check_uk UNIQUE(location_unique_check);

        
CREATE OR REPLACE FUNCTION content_copies_uniqueness_check()
  RETURNS trigger AS
$BODY$
BEGIN
  NEW.location_unique_check = NEW.data_id || ',' ||
                              NEW.edms_id || ',' ||
                              coalesce(NEW.path, '') || ',' || 
                              coalesce(NEW.git_commit_hash, '') || ',' || 
                              coalesce(NEW.external_code, '');
  RETURN NEW;
END;
$BODY$
  LANGUAGE 'plpgsql';

CREATE TRIGGER content_copies_uniqueness_check
  BEFORE INSERT OR UPDATE
  ON content_copies
  FOR EACH ROW
  EXECUTE PROCEDURE content_copies_uniqueness_check();

  
CREATE OR REPLACE FUNCTION content_copies_location_type_check() RETURNS trigger AS $$
DECLARE
   edms_address_type EDMS_ADDRESS_TYPE;
   index integer;
BEGIN

   select position(address_type in NEW.location_type), address_type into index, edms_address_type from external_data_management_systems
      where id = NEW.edms_id;

   if index != 1 then
      RAISE EXCEPTION 'Insert/Update to content_copies failed. Location type %, but edms.address_type %', NEW.location_type, edms_address_type;
   end if;

   RETURN NEW;

END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER content_copies_location_type_check 
  BEFORE INSERT OR UPDATE 
  ON content_copies
  FOR EACH ROW 
  EXECUTE PROCEDURE content_copies_location_type_check();
  