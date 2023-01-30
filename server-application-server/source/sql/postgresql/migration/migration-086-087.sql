-- Migration from 086 to 087

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
