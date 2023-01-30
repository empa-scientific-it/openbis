-- Migration from 131 to 132

ALTER TABLE data_all ADD COLUMN orig_del tech_id;
ALTER TABLE experiments_all ADD COLUMN orig_del tech_id;
ALTER TABLE samples_all	ADD COLUMN orig_del tech_id;

-----------
-- Views --
-----------

---------------
-- data view --
---------------

DROP VIEW data;
CREATE VIEW data AS
     SELECT id, code, dsty_id, dast_id, expe_id, data_producer_code, production_timestamp, samp_id, registration_timestamp, pers_id_registerer, pers_id_modifier, is_placeholder, is_valid, modification_timestamp, is_derived, ctnr_order, ctnr_id, del_id, orig_del, version 
       FROM data_all 
      WHERE del_id IS NULL;
      
CREATE OR REPLACE RULE data_insert AS
  ON INSERT TO data DO INSTEAD 
     INSERT INTO data_all (
       id, 
       code, 
       ctnr_id,
       ctnr_order,
       del_id,
       orig_del,
       expe_id,
       dast_id,
       data_producer_code,
       dsty_id,
       is_derived,
       is_placeholder,
       is_valid,
       modification_timestamp,
       pers_id_registerer,
       pers_id_modifier,
       production_timestamp,
       registration_timestamp,
       samp_id,
       version
     ) VALUES (
       NEW.id, 
       NEW.code, 
       NEW.ctnr_id,
       NEW.ctnr_order,
       NEW.del_id, 
       NEW.orig_del,
       NEW.expe_id,
       NEW.dast_id,
       NEW.data_producer_code,
       NEW.dsty_id,
       NEW.is_derived, 
       NEW.is_placeholder,
       NEW.is_valid,
       NEW.modification_timestamp,
       NEW.pers_id_registerer,
       NEW.pers_id_modifier,
       NEW.production_timestamp,
       NEW.registration_timestamp,
       NEW.samp_id,
       NEW.version
     );
     
CREATE OR REPLACE RULE data_update AS
    ON UPDATE TO data DO INSTEAD 
       UPDATE data_all
          SET code = NEW.code,
              ctnr_id = NEW.ctnr_id,
              ctnr_order = NEW.ctnr_order,
              del_id = NEW.del_id,
              orig_del = NEW.orig_del,
              expe_id = NEW.expe_id,
              dast_id = NEW.dast_id,
              data_producer_code = NEW.data_producer_code,
              dsty_id = NEW.dsty_id,
              is_derived = NEW.is_derived,
              is_placeholder = NEW.is_placeholder,
              is_valid = NEW.is_valid,
              modification_timestamp = NEW.modification_timestamp,
              pers_id_registerer = NEW.pers_id_registerer,
              pers_id_modifier = NEW.pers_id_modifier,
              production_timestamp = NEW.production_timestamp,
              registration_timestamp = NEW.registration_timestamp,
              samp_id = NEW.samp_id,
              version = NEW.version
       WHERE id = NEW.id;
              
CREATE OR REPLACE RULE data_all AS
    ON DELETE TO data DO INSTEAD
       DELETE FROM data_all
              WHERE id = OLD.id;
              
DROP VIEW data_deleted;
CREATE VIEW data_deleted AS
     SELECT id, code, dsty_id, dast_id, expe_id, data_producer_code, production_timestamp, samp_id, registration_timestamp, pers_id_registerer, pers_id_modifier, is_placeholder, is_valid, modification_timestamp, is_derived, ctnr_order, ctnr_id, del_id, orig_del, version 
       FROM data_all 
      WHERE del_id IS NOT NULL;

CREATE OR REPLACE RULE data_deleted_update AS
    ON UPDATE TO data_deleted DO INSTEAD 
       UPDATE data_all
          SET del_id = NEW.del_id,
              orig_del = NEW.orig_del,
              modification_timestamp = NEW.modification_timestamp,
              version = NEW.version
          WHERE id = NEW.id;
     
CREATE OR REPLACE RULE data_deleted_delete AS
    ON DELETE TO data_deleted DO INSTEAD
       DELETE FROM data_all
              WHERE id = OLD.id;               
              
---------------------
-- experiment view --
---------------------

DROP VIEW experiments;
CREATE VIEW experiments AS
     SELECT id, perm_id, code, exty_id, pers_id_registerer, pers_id_modifier, registration_timestamp, modification_timestamp, proj_id, del_id, orig_del, is_public, version 
       FROM experiments_all 
      WHERE del_id IS NULL;

CREATE OR REPLACE RULE experiment_insert AS
  ON INSERT TO experiments DO INSTEAD 
     INSERT INTO experiments_all (
       id, 
       code, 
       del_id,
       orig_del,
       exty_id, 
       is_public,
       modification_timestamp,
       perm_id,
       pers_id_registerer, 
       pers_id_modifier, 
       proj_id,
       registration_timestamp,
       version
     ) VALUES (
       NEW.id, 
       NEW.code, 
       NEW.del_id,
       NEW.orig_del,
       NEW.exty_id, 
       NEW.is_public,
       NEW.modification_timestamp,
       NEW.perm_id,
       NEW.pers_id_registerer, 
       NEW.pers_id_modifier, 
       NEW.proj_id,
       NEW.registration_timestamp,
       NEW.version
     );
     
CREATE OR REPLACE RULE experiment_update AS
    ON UPDATE TO experiments DO INSTEAD 
       UPDATE experiments_all
          SET code = NEW.code,
              del_id = NEW.del_id,
              orig_del = NEW.orig_del,
              exty_id = NEW.exty_id,
              is_public = NEW.is_public,
              modification_timestamp = NEW.modification_timestamp,
              perm_id = NEW.perm_id,
              pers_id_registerer = NEW.pers_id_registerer,
              pers_id_modifier = NEW.pers_id_modifier,
              proj_id = NEW.proj_id,
              registration_timestamp = NEW.registration_timestamp,
              version = NEW.version
          WHERE id = NEW.id;
     
CREATE OR REPLACE RULE experiment_delete AS
    ON DELETE TO experiments DO INSTEAD
       DELETE FROM experiments_all
              WHERE id = OLD.id;
 
DROP VIEW experiments_deleted;
CREATE VIEW experiments_deleted AS
     SELECT id, perm_id, code, exty_id, pers_id_registerer, pers_id_modifier, registration_timestamp, modification_timestamp, proj_id, del_id, orig_del, is_public, version 
       FROM experiments_all 
      WHERE del_id IS NOT NULL;
      
CREATE OR REPLACE RULE experiments_deleted_update AS
    ON UPDATE TO experiments_deleted DO INSTEAD 
       UPDATE experiments_all
          SET del_id = NEW.del_id,
              orig_del = NEW.orig_del,
              modification_timestamp = NEW.modification_timestamp,
              version = NEW.version
          WHERE id = NEW.id;
     
CREATE OR REPLACE RULE experiments_deleted_delete AS
    ON DELETE TO experiments_deleted DO INSTEAD
       DELETE FROM experiments_all
              WHERE id = OLD.id;
                           
-----------------
-- sample view --
-----------------

DROP VIEW samples;
CREATE VIEW samples AS
     SELECT id, perm_id, code, expe_id, saty_id, registration_timestamp, modification_timestamp, pers_id_registerer, pers_id_modifier, del_id, orig_del, dbin_id, space_id, samp_id_part_of, version 
       FROM samples_all 
      WHERE del_id IS NULL;
      
CREATE OR REPLACE RULE sample_insert AS
    ON INSERT TO samples DO INSTEAD 
       INSERT INTO samples_all (
         id, 
         code, 
         dbin_id,
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
         NEW.dbin_id,
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
              dbin_id = NEW.dbin_id,
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
     
CREATE OR REPLACE RULE sample_delete AS
    ON DELETE TO samples DO INSTEAD
       DELETE FROM samples_all
              WHERE id = OLD.id;
              
DROP VIEW samples_deleted;
CREATE VIEW samples_deleted AS
     SELECT id, perm_id, code, expe_id, saty_id, registration_timestamp, modification_timestamp, pers_id_registerer, pers_id_modifier, del_id, orig_del, dbin_id, space_id, samp_id_part_of, version 
       FROM samples_all 
      WHERE del_id IS NOT NULL;

CREATE OR REPLACE RULE sample_deleted_update AS
    ON UPDATE TO samples_deleted DO INSTEAD
       UPDATE samples_all
          SET del_id = NEW.del_id,
              orig_del = NEW.orig_del,
              modification_timestamp = NEW.modification_timestamp,
              version = NEW.version
          WHERE id = NEW.id;
     
CREATE OR REPLACE RULE sample_deleted_delete AS
    ON DELETE TO samples_deleted DO INSTEAD
       DELETE FROM samples_all
              WHERE id = OLD.id;
              
