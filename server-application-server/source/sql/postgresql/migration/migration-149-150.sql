ALTER TABLE SAMPLES_ALL ADD CONSTRAINT SAMP_PROJ_FK FOREIGN KEY (PROJ_ID) REFERENCES PROJECTS(ID);
CREATE INDEX SAMP_PROJ_FK_I ON SAMPLES_ALL (PROJ_ID);

CREATE OR REPLACE RULE sample_insert AS
    ON INSERT TO samples DO INSTEAD 
       INSERT INTO samples_all (
         id, 
         code, 
         del_id,
         orig_del,
         expe_id,
         proj_id,
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
         NEW.proj_id,
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
              proj_id = NEW.proj_id,
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
     
