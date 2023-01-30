-- Migration from 137 to 138

-- This code creates the column only if it doesn't exist.
-- This is to ensure that the migration runs fine even when 
-- if the column was previously created by sql patch

DO $$ 
    BEGIN
        BEGIN
            ALTER TABLE data_all ADD COLUMN access_timestamp TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP;
            CREATE INDEX DATA_ACCT_I ON DATA_ALL (ACCESS_TIMESTAMP);
        EXCEPTION
            WHEN duplicate_column THEN RAISE NOTICE 'column access_timestamp already exists in data_all.';
        END;
    END;
$$;

DROP VIEW data;

CREATE VIEW data AS
     SELECT id, code, dsty_id, dast_id, expe_id, data_producer_code, production_timestamp, samp_id, registration_timestamp, access_timestamp, pers_id_registerer, pers_id_modifier, is_placeholder, is_valid, modification_timestamp, is_derived, ctnr_order, ctnr_id, del_id, orig_del, version 
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
       access_timestamp,
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
       NEW.access_timestamp,
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
              access_timestamp = NEW.access_timestamp,
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
     SELECT id, code, dsty_id, dast_id, expe_id, data_producer_code, production_timestamp, samp_id, registration_timestamp, access_timestamp, pers_id_registerer, pers_id_modifier, is_placeholder, is_valid, modification_timestamp, is_derived, ctnr_order, ctnr_id, del_id, orig_del, version 
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
              

