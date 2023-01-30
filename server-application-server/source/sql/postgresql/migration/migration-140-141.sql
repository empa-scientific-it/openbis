-- Migration from 140-141

ALTER TABLE PERSONS DROP CONSTRAINT PERS_DBIN_FK;
ALTER TABLE PERSONS DROP CONSTRAINT PERS_BK_UK;
ALTER TABLE PERSONS ADD CONSTRAINT PERS_BK_UK UNIQUE(USER_ID);
ALTER TABLE PERSONS DROP COLUMN DBIN_ID;

ALTER TABLE AUTHORIZATION_GROUPS DROP CONSTRAINT AG_DBIN_FK;
ALTER TABLE AUTHORIZATION_GROUPS DROP CONSTRAINT AG_BK_UK;
ALTER TABLE AUTHORIZATION_GROUPS ADD CONSTRAINT AG_BK_UK UNIQUE(CODE);
ALTER TABLE AUTHORIZATION_GROUPS DROP COLUMN DBIN_ID;

ALTER TABLE MATERIALS DROP CONSTRAINT MATE_DBIN_FK;
ALTER TABLE MATERIALS DROP CONSTRAINT MATE_BK_UK;
ALTER TABLE MATERIALS ADD CONSTRAINT MATE_BK_UK UNIQUE(CODE,MATY_ID);
ALTER TABLE MATERIALS DROP COLUMN DBIN_ID;

DROP VIEW SAMPLES_DELETED;
DROP VIEW SAMPLES;
ALTER TABLE SAMPLES_ALL DROP CONSTRAINT SAMP_DBIN_FK;
ALTER TABLE SAMPLES_ALL DROP COLUMN DBIN_ID;
CREATE VIEW samples AS
     SELECT id, perm_id, code, expe_id, saty_id, registration_timestamp, modification_timestamp, pers_id_registerer, pers_id_modifier, del_id, orig_del, space_id, samp_id_part_of, version 
       FROM samples_all 
      WHERE del_id IS NULL;

CREATE VIEW samples_deleted AS
     SELECT id, perm_id, code, expe_id, saty_id, registration_timestamp, modification_timestamp, pers_id_registerer, pers_id_modifier, del_id, orig_del, space_id, samp_id_part_of, version 
       FROM samples_all 
      WHERE del_id IS NOT NULL;

CREATE OR REPLACE FUNCTION sample_fill_code_unique_check()
  RETURNS trigger AS
$BODY$
BEGIN
  NEW.code_unique_check = NEW.code || ',' || coalesce(NEW.samp_id_part_of, -1) || ',' || coalesce(NEW.space_id, -1);
  RETURN NEW;
END;
$BODY$
  LANGUAGE 'plpgsql';      
      
CREATE OR REPLACE FUNCTION sample_fill_subcode_unique_check()
  RETURNS trigger AS
$BODY$
DECLARE
    unique_subcode  BOOLEAN_CHAR;
BEGIN
    SELECT is_subcode_unique into unique_subcode FROM sample_types WHERE id = NEW.saty_id;
    
    IF (unique_subcode) THEN
    NEW.subcode_unique_check = NEW.code || ',' || coalesce(NEW.saty_id, -1) || ',' || coalesce(NEW.space_id, -1);
    ELSE
    NEW.subcode_unique_check = NULL;
  END IF;
  
  RETURN NEW;
END;
$BODY$
  LANGUAGE 'plpgsql';
  
CREATE OR REPLACE RULE sample_insert AS
    ON INSERT TO samples DO INSTEAD 
       INSERT INTO samples_all (
         id, 
         code, 
         del_id,
         orig_del,
         expe_id,
         modification_timestamp,
         perm_id,
         pers_id_registerer, 
         pers_id_modifier, 
         registration_timestamp, 
         samp_id_part_of,
         saty_id, 
         space_id,
         version
       ) VALUES (
         NEW.id, 
         NEW.code, 
         NEW.del_id,
         NEW.orig_del,
         NEW.expe_id,
         NEW.modification_timestamp,
         NEW.perm_id,
         NEW.pers_id_registerer, 
         NEW.pers_id_modifier, 
         NEW.registration_timestamp, 
         NEW.samp_id_part_of,
         NEW.saty_id, 
         NEW.space_id,
         NEW.version
       );
     
CREATE OR REPLACE RULE sample_update AS
    ON UPDATE TO samples DO INSTEAD 
       UPDATE samples_all
          SET code = NEW.code,
              del_id = NEW.del_id,
              orig_del = NEW.orig_del,
              expe_id = NEW.expe_id,
              modification_timestamp = NEW.modification_timestamp,
              perm_id = NEW.perm_id,
              pers_id_registerer = NEW.pers_id_registerer,
              pers_id_modifier = NEW.pers_id_modifier,
              registration_timestamp = NEW.registration_timestamp,
              samp_id_part_of = NEW.samp_id_part_of,
              saty_id = NEW.saty_id,
              space_id = NEW.space_id,
              version = NEW.version
          WHERE id = NEW.id;
      