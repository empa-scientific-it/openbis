-- Migration from 100 to 101

DROP VIEW experiments;
CREATE VIEW experiments AS
     SELECT id, perm_id, code, exty_id, pers_id_registerer, registration_timestamp, modification_timestamp, proj_id, del_id, is_public 
       FROM experiments_all 
      WHERE del_id IS NULL;
      
DROP VIEW experiments_deleted;
CREATE VIEW experiments_deleted AS
     SELECT id, perm_id, code, exty_id, pers_id_registerer, registration_timestamp, modification_timestamp, proj_id, del_id, is_public 
       FROM experiments_all 
      WHERE del_id IS NOT NULL;
----------------
-- experiment --
----------------

CREATE OR REPLACE RULE experiment_insert AS
  ON INSERT TO experiments DO INSTEAD 
     INSERT INTO experiments_all (
       id, 
       code, 
       del_id,
       exty_id, 
       is_public,
       modification_timestamp,
       perm_id,
       pers_id_registerer, 
       proj_id,
       registration_timestamp
     ) VALUES (
       NEW.id, 
       NEW.code, 
       NEW.del_id,
       NEW.exty_id, 
       NEW.is_public,
       NEW.modification_timestamp,
       NEW.perm_id,
       NEW.pers_id_registerer, 
       NEW.proj_id,
       NEW.registration_timestamp
     );
     
CREATE OR REPLACE RULE experiment_update AS
    ON UPDATE TO experiments DO INSTEAD 
       UPDATE experiments_all
          SET code = NEW.code,
              del_id = NEW.del_id,
              exty_id = NEW.exty_id,
              is_public = NEW.is_public,
              modification_timestamp = NEW.modification_timestamp,
              perm_id = NEW.perm_id,
              pers_id_registerer = NEW.pers_id_registerer,
              proj_id = NEW.proj_id,
              registration_timestamp = NEW.registration_timestamp
          WHERE id = NEW.id;
     
CREATE OR REPLACE RULE experiment_delete AS
    ON DELETE TO experiments DO INSTEAD
       DELETE FROM experiments_all
              WHERE id = OLD.id;
 
CREATE OR REPLACE RULE experiments_deleted_update AS
    ON UPDATE TO experiments_deleted DO INSTEAD 
       UPDATE experiments_all
          SET del_id = NEW.del_id,
              modification_timestamp = NEW.modification_timestamp
          WHERE id = NEW.id;
     
CREATE OR REPLACE RULE experiments_deleted_delete AS
    ON DELETE TO experiments_deleted DO INSTEAD
       DELETE FROM experiments_all
              WHERE id = OLD.id;
     
DROP INDEX EXPE_MATE_FK_I;



ALTER TABLE EXPERIMENTS_ALL DROP CONSTRAINT EXPE_MATE_FK;

ALTER TABLE EXPERIMENTS_ALL drop column mate_id_study_object;