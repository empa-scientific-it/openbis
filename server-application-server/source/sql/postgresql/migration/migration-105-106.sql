-- Migration from 105 to 106

ALTER TABLE DATA_ALL ADD COLUMN PERS_ID_MODIFIER TECH_ID;
ALTER TABLE DATA_ALL ADD CONSTRAINT DATA_PERS_FK_MOD FOREIGN KEY (PERS_ID_MODIFIER) REFERENCES PERSONS(ID);

ALTER TABLE EXPERIMENTS_ALL ADD COLUMN PERS_ID_MODIFIER TECH_ID;
ALTER TABLE EXPERIMENTS_ALL ADD CONSTRAINT EXPE_PERS_FK_MOD FOREIGN KEY (PERS_ID_MODIFIER) REFERENCES PERSONS(ID);

ALTER TABLE SAMPLES_ALL ADD COLUMN PERS_ID_MODIFIER TECH_ID;
ALTER TABLE SAMPLES_ALL ADD CONSTRAINT SAMP_PERS_FK_MOD FOREIGN KEY (PERS_ID_MODIFIER) REFERENCES PERSONS(ID);

DROP VIEW data;
DROP VIEW data_deleted;

CREATE OR REPLACE VIEW data AS
     SELECT id, code, dsty_id, dast_id, expe_id, data_producer_code, production_timestamp, samp_id, registration_timestamp, pers_id_registerer, pers_id_modifier, is_placeholder, is_valid, modification_timestamp, is_derived, ctnr_order, ctnr_id, del_id 
       FROM data_all 
      WHERE del_id IS NULL;
CREATE OR REPLACE VIEW data_deleted AS
     SELECT id, code, dsty_id, dast_id, expe_id, data_producer_code, production_timestamp, samp_id, registration_timestamp, pers_id_registerer, pers_id_modifier, is_placeholder, is_valid, modification_timestamp, is_derived, ctnr_order, ctnr_id, del_id 
       FROM data_all 
      WHERE del_id IS NOT NULL;


DROP VIEW experiments;
DROP VIEW experiments_deleted;

CREATE OR REPLACE VIEW experiments AS
     SELECT id, perm_id, code, exty_id, pers_id_registerer, pers_id_modifier, registration_timestamp, modification_timestamp, proj_id, del_id, is_public 
       FROM experiments_all 
      WHERE del_id IS NULL;
CREATE OR REPLACE VIEW experiments_deleted AS
     SELECT id, perm_id, code, exty_id, pers_id_registerer, pers_id_modifier, registration_timestamp, modification_timestamp, proj_id, del_id, is_public 
       FROM experiments_all 
      WHERE del_id IS NOT NULL;

DROP VIEW samples;
DROP VIEW samples_deleted;

CREATE OR REPLACE VIEW samples AS
     SELECT id, perm_id, code, expe_id, saty_id, registration_timestamp, modification_timestamp, pers_id_registerer, pers_id_modifier, del_id, dbin_id, space_id, samp_id_part_of 
       FROM samples_all 
      WHERE del_id IS NULL;
CREATE OR REPLACE VIEW samples_deleted AS
     SELECT id, perm_id, code, expe_id, saty_id, registration_timestamp, modification_timestamp, pers_id_registerer, pers_id_modifier, del_id, dbin_id, space_id, samp_id_part_of 
       FROM samples_all 
      WHERE del_id IS NOT NULL;
      
CREATE OR REPLACE RULE sample_insert AS
    ON INSERT TO samples DO INSTEAD 
       INSERT INTO samples_all (
         id, 
         code, 
         dbin_id,
         del_id,
         expe_id,
         modification_timestamp,
         perm_id,
         pers_id_registerer, 
         pers_id_modifier, 
         registration_timestamp, 
         samp_id_part_of,
         saty_id, 
         space_id
       ) VALUES (
         NEW.id, 
         NEW.code, 
         NEW.dbin_id,
         NEW.del_id,
         NEW.expe_id,
         NEW.modification_timestamp,
         NEW.perm_id,
         NEW.pers_id_registerer, 
         NEW.pers_id_modifier, 
         NEW.registration_timestamp, 
         NEW.samp_id_part_of,
         NEW.saty_id, 
         NEW.space_id
       );
     
CREATE OR REPLACE RULE sample_update AS
    ON UPDATE TO samples DO INSTEAD 
       UPDATE samples_all
          SET code = NEW.code,
              dbin_id = NEW.dbin_id,
              del_id = NEW.del_id,
              expe_id = NEW.expe_id,
              modification_timestamp = NEW.modification_timestamp,
              perm_id = NEW.perm_id,
              pers_id_registerer = NEW.pers_id_registerer,
              pers_id_modifier = NEW.pers_id_modifier,
              registration_timestamp = NEW.registration_timestamp,
              samp_id_part_of = NEW.samp_id_part_of,
              saty_id = NEW.saty_id,
              space_id = NEW.space_id
          WHERE id = NEW.id;
     
CREATE OR REPLACE RULE sample_delete AS
    ON DELETE TO samples DO INSTEAD
       DELETE FROM samples_all
              WHERE id = OLD.id;
              
CREATE OR REPLACE RULE sample_deleted_update AS
    ON UPDATE TO samples_deleted DO INSTEAD 
       UPDATE samples_all
          SET del_id = NEW.del_id,
              modification_timestamp = NEW.modification_timestamp
          WHERE id = NEW.id;
     
CREATE OR REPLACE RULE sample_deleted_delete AS
    ON DELETE TO samples_deleted DO INSTEAD
       DELETE FROM samples_all
              WHERE id = OLD.id;
              
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
       pers_id_modifier, 
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
       NEW.pers_id_modifier, 
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
              pers_id_modifier = NEW.pers_id_modifier,
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
                           
----------
-- data --
----------

CREATE OR REPLACE RULE data_insert AS
  ON INSERT TO data DO INSTEAD 
     INSERT INTO data_all (
       id, 
       code, 
       ctnr_id,
       ctnr_order,
       del_id,
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
       samp_id
     ) VALUES (
       NEW.id, 
       NEW.code, 
       NEW.ctnr_id,
       NEW.ctnr_order,
       NEW.del_id, 
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
       NEW.samp_id
     );
     
CREATE OR REPLACE RULE data_update AS
    ON UPDATE TO data DO INSTEAD 
       UPDATE data_all
          SET code = NEW.code,
              ctnr_id = NEW.ctnr_id,
              ctnr_order = NEW.ctnr_order,
              del_id = NEW.del_id,
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
              samp_id = NEW.samp_id
       WHERE id = NEW.id;
              
CREATE OR REPLACE RULE data_all AS
    ON DELETE TO data DO INSTEAD
       DELETE FROM data_all
              WHERE id = OLD.id;
              
CREATE OR REPLACE RULE data_deleted_update AS
    ON UPDATE TO data_deleted DO INSTEAD 
       UPDATE data_all
          SET del_id = NEW.del_id,
              modification_timestamp = NEW.modification_timestamp
          WHERE id = NEW.id;
     
CREATE OR REPLACE RULE data_deleted_delete AS
    ON DELETE TO data_deleted DO INSTEAD
       DELETE FROM data_all
              WHERE id = OLD.id;               

ALTER TABLE DATA_SET_RELATIONSHIPS_ALL ADD COLUMN PERS_ID_AUTHOR TECH_ID;
ALTER TABLE DATA_SET_RELATIONSHIPS_ALL ADD CONSTRAINT DATA_SET_RELATIONSHIPS_PERS_FK FOREIGN KEY (PERS_ID_AUTHOR) REFERENCES PERSONS(ID);
ALTER TABLE DATA_SET_RELATIONSHIPS_ALL ADD COLUMN REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE DATA_SET_RELATIONSHIPS_ALL ADD COLUMN MODIFICATION_TIMESTAMP TIME_STAMP DEFAULT CURRENT_TIMESTAMP;

CREATE OR REPLACE VIEW data_set_relationships AS
   SELECT data_id_parent, data_id_child, del_id, pers_id_author, registration_timestamp, modification_timestamp
   FROM data_set_relationships_all 
   WHERE del_id IS NULL;

CREATE OR REPLACE RULE data_set_relationships_insert AS
    ON INSERT TO data_set_relationships DO INSTEAD 
       INSERT INTO data_set_relationships_all (
         data_id_parent, 
         data_id_child,
         pers_id_author,
			   registration_timestamp,
			   modification_timestamp
       ) VALUES (
         NEW.data_id_parent, 
         NEW.data_id_child,
         NEW.pers_id_author,
			   NEW.registration_timestamp,
			   NEW.modification_timestamp
       );

CREATE OR REPLACE RULE data_set_relationships_update AS
    ON UPDATE TO data_set_relationships DO INSTEAD 
       UPDATE data_set_relationships_all
          SET 
			      data_id_parent = NEW.data_id_parent, 
			      data_id_child = NEW.data_id_child, 
			      del_id = NEW.del_id,
			      pers_id_author = NEW.pers_id_author,
			      registration_timestamp = NEW.registration_timestamp,
			      modification_timestamp = NEW.modification_timestamp
          WHERE data_id_parent = NEW.data_id_parent and data_id_child = NEW.data_id_child;
          
CREATE OR REPLACE RULE data_set_relationships_delete AS
    ON DELETE TO data_set_relationships DO INSTEAD
       DELETE FROM data_set_relationships_all
              WHERE data_id_parent = OLD.data_id_parent and data_id_child = OLD.data_id_child;

ALTER TABLE SAMPLE_RELATIONSHIPS_ALL ADD COLUMN PERS_ID_AUTHOR TECH_ID;
ALTER TABLE SAMPLE_RELATIONSHIPS_ALL ADD CONSTRAINT SAMPLE_RELATIONSHIPS_PERS_FK FOREIGN KEY (PERS_ID_AUTHOR) REFERENCES PERSONS(ID);
ALTER TABLE SAMPLE_RELATIONSHIPS_ALL ADD COLUMN REGISTRATION_TIMESTAMP TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE SAMPLE_RELATIONSHIPS_ALL ADD COLUMN MODIFICATION_TIMESTAMP TIME_STAMP DEFAULT CURRENT_TIMESTAMP;

CREATE OR REPLACE VIEW sample_relationships AS
   SELECT id, sample_id_parent, relationship_id, sample_id_child, del_id, pers_id_author, registration_timestamp, modification_timestamp
   FROM sample_relationships_all
   WHERE del_id IS NULL;

CREATE OR REPLACE RULE sample_relationships_insert AS
    ON INSERT TO sample_relationships DO INSTEAD 
       INSERT INTO sample_relationships_all (
         id, 
         sample_id_parent, 
         relationship_id, 
         sample_id_child,
         pers_id_author,
			   registration_timestamp,
   	     modification_timestamp
       ) VALUES (
         NEW.id, 
         NEW.sample_id_parent, 
         NEW.relationship_id, 
         NEW.sample_id_child,
         NEW.pers_id_author,
			   NEW.registration_timestamp,
			   NEW.modification_timestamp
       );
       
CREATE OR REPLACE RULE sample_relationships_update AS
    ON UPDATE TO sample_relationships DO INSTEAD 
       UPDATE sample_relationships_all
          SET 
			      sample_id_parent = NEW.sample_id_parent, 
			      relationship_id = NEW.relationship_id, 
			      sample_id_child = NEW.sample_id_child,
			      del_id = NEW.del_id,
			      pers_id_author = NEW.pers_id_author,
			      registration_timestamp = NEW.registration_timestamp,
			      modification_timestamp = NEW.modification_timestamp
          WHERE id = NEW.id;
     
CREATE OR REPLACE RULE sample_relationships_delete AS
    ON DELETE TO sample_relationships DO INSTEAD
       DELETE FROM sample_relationships_all
              WHERE id = OLD.id;
