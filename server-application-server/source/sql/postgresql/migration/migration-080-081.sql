-- Migration from 080 to 081

----------------------------------------------------------------------------------------------------
-- PURPOSE: Introduce views to avoid hibernate problems when loading relationships for 
-- deleted entities. 
----------------------------------------------------------------------------------------------------

----------------------------------------------------------------------------------------------------
-- data_set_relationships
----------------------------------------------------------------------------------------------------

ALTER TABLE data_set_relationships RENAME TO data_set_relationships_all;

CREATE VIEW data_set_relationships AS
   SELECT data_id_parent, data_id_child 
   FROM data_set_relationships_all 
     JOIN data_all parent ON data_id_parent = parent.id  
     JOIN data_all child ON data_id_child = child.id 
   WHERE parent.del_id IS NULL and child.del_id is NULL;
   
GRANT SELECT ON data_set_relationships TO GROUP openbis_readonly;

CREATE OR REPLACE RULE data_set_relationships_insert AS
    ON INSERT TO data_set_relationships DO INSTEAD 
       INSERT INTO data_set_relationships_all (
         data_id_parent, 
         data_id_child 
       ) VALUES (
         NEW.data_id_parent, 
         NEW.data_id_child 
       );
     
CREATE OR REPLACE RULE data_set_relationships_delete AS
    ON DELETE TO data_set_relationships DO INSTEAD
       DELETE FROM data_set_relationships_all
              WHERE data_id_parent = OLD.data_id_parent and data_id_child = OLD.data_id_child;

----------------------------------------------------------------------------------------------------
-- sample_relationships
----------------------------------------------------------------------------------------------------

ALTER TABLE sample_relationships RENAME TO sample_relationships_all;

CREATE VIEW sample_relationships AS
   SELECT s.id as id, sample_id_parent, relationship_id, sample_id_child 
   FROM sample_relationships_all s
     JOIN samples_all parent ON sample_id_parent = parent.id  
     JOIN samples_all child ON sample_id_child = child.id 
   WHERE parent.del_id IS NULL and child.del_id is NULL;
   
GRANT SELECT ON sample_relationships TO GROUP openbis_readonly;

CREATE OR REPLACE RULE sample_relationships_insert AS
    ON INSERT TO sample_relationships DO INSTEAD 
       INSERT INTO sample_relationships_all (
         id, 
         sample_id_parent, 
         relationship_id, 
         sample_id_child
       ) VALUES (
         NEW.id, 
         NEW.sample_id_parent, 
         NEW.relationship_id, 
         NEW.sample_id_child
       );
       
CREATE OR REPLACE RULE sample_relationships_update AS
    ON UPDATE TO sample_relationships DO INSTEAD 
       UPDATE sample_relationships_all
          SET 
			      sample_id_parent = NEW.sample_id_parent, 
			      relationship_id = NEW.relationship_id, 
			      sample_id_child = NEW.sample_id_child
          WHERE id = NEW.id;
     
CREATE OR REPLACE RULE sample_relationships_delete AS
    ON DELETE TO sample_relationships DO INSTEAD
       DELETE FROM sample_relationships_all
              WHERE id = OLD.id;
              