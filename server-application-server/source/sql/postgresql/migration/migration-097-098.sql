-- Migration from 097 to 098

ALTER TABLE DATA_SET_RELATIONSHIPS_ALL ADD COLUMN DEL_ID TECH_ID;
ALTER TABLE DATA_SET_RELATIONSHIPS_ALL ADD CONSTRAINT DSRE_DEL_FK FOREIGN KEY (DEL_ID) REFERENCES DELETIONS(ID);
CREATE INDEX DSRE_DEL_FK_I ON DATA_SET_RELATIONSHIPS_ALL (DEL_ID);

UPDATE data_set_relationships_all set del_id = (SELECT del_id FROM data_all WHERE ID = data_id_parent)
   WHERE (data_id_parent, data_id_child) IN (SELECT data_id_parent, data_id_child
   FROM data_set_relationships_all 
     JOIN data_all parent ON data_id_parent = parent.id  
     JOIN data_all child ON data_id_child = child.id 
   WHERE (parent.del_id IS NOT NULL and child.del_id is NULL)
     OR (parent.del_id IS NOT NULL and child.del_id is NOT NULL AND parent.del_id <= child.del_id));

UPDATE data_set_relationships_all set del_id = (SELECT del_id FROM data_all WHERE ID = data_id_child)
   WHERE (data_id_parent, data_id_child) IN (SELECT data_id_parent, data_id_child
   FROM data_set_relationships_all 
     JOIN data_all parent ON data_id_parent = parent.id  
     JOIN data_all child ON data_id_child = child.id 
   WHERE (parent.del_id IS NULL and child.del_id is NOT NULL)
     OR (parent.del_id IS NOT NULL and child.del_id is NOT NULL AND parent.del_id > child.del_id));
     
ALTER TABLE sample_relationships_all ADD COLUMN DEL_ID TECH_ID;
ALTER TABLE sample_relationships_all ADD CONSTRAINT sare_del_fk FOREIGN KEY (del_id) REFERENCES deletions(id);
CREATE INDEX sare_del_fk_i ON sample_relationships_all (del_id);

UPDATE sample_relationships_all set del_id = (SELECT del_id from samples_all where ID = sample_id_parent)
   WHERE (sample_id_parent, sample_id_child, relationship_id) IN (SELECT sample_id_parent, sample_id_child, relationship_id
   FROM sample_relationships_all 
     JOIN samples_all parent ON sample_id_parent = parent.id  
     JOIN samples_all child ON sample_id_child = child.id 
   WHERE (parent.del_id IS NOT NULL and child.del_id is NULL)
     OR (parent.del_id IS NOT NULL and child.del_id is NOT NULL AND parent.del_id <= child.del_id));

UPDATE sample_relationships_all set del_id = (SELECT del_id from samples_all where ID = sample_id_child)
   WHERE (sample_id_parent, sample_id_child, relationship_id) IN (SELECT sample_id_parent, sample_id_child, relationship_id
   FROM sample_relationships_all 
     JOIN samples_all parent ON sample_id_parent = parent.id  
     JOIN samples_all child ON sample_id_child = child.id 
   WHERE (parent.del_id IS NULL and child.del_id is NOT NULL)
     OR (parent.del_id IS NOT NULL and child.del_id is NOT NULL AND parent.del_id > child.del_id));
     
DROP VIEW data_set_relationships;
CREATE VIEW data_set_relationships AS
   SELECT data_id_parent, data_id_child, del_id
   FROM data_set_relationships_all 
   WHERE del_id IS NULL;

CREATE OR REPLACE RULE data_set_relationships_insert AS
    ON INSERT TO data_set_relationships DO INSTEAD 
       INSERT INTO data_set_relationships_all (
         data_id_parent, 
         data_id_child 
       ) VALUES (
         NEW.data_id_parent, 
         NEW.data_id_child 
       );

CREATE OR REPLACE RULE data_set_relationships_update AS
    ON UPDATE TO data_set_relationships DO INSTEAD 
       UPDATE data_set_relationships_all
          SET 
			      data_id_parent = NEW.data_id_parent, 
			      data_id_child = NEW.data_id_child, 
			      del_id = NEW.del_id
          WHERE  data_id_parent = NEW.data_id_parent and data_id_child = NEW.data_id_child;
          
CREATE OR REPLACE RULE data_set_relationships_delete AS
    ON DELETE TO data_set_relationships DO INSTEAD
       DELETE FROM data_set_relationships_all
              WHERE data_id_parent = OLD.data_id_parent and data_id_child = OLD.data_id_child;

DROP VIEW sample_relationships;
CREATE VIEW sample_relationships AS
   SELECT s.id as id, sample_id_parent, relationship_id, sample_id_child, del_id
   FROM sample_relationships_all s
   WHERE del_id IS NULL;

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
			      sample_id_child = NEW.sample_id_child,
			      del_id = NEW.del_id
          WHERE id = NEW.id;
     
CREATE OR REPLACE RULE sample_relationships_delete AS
    ON DELETE TO sample_relationships DO INSTEAD
       DELETE FROM sample_relationships_all
              WHERE id = OLD.id;

CREATE OR REPLACE FUNCTION preserve_deletion_consistency_on_data_set_relationships() RETURNS trigger AS $$
DECLARE
  delid  TECH_ID;
BEGIN
	IF (NEW.del_id IS NOT NULL OR OLD.del_id IS NULL) THEN
		RETURN NEW;
	END IF;
	SELECT del_id INTO delid
		FROM DATA_ALL where id = NEW.data_id_parent;
	IF (delid IS NOT NULL) THEN
		NEW.del_id = delid;
	END IF;
	SELECT del_id INTO delid
		FROM DATA_ALL where id = NEW.data_id_child;
	IF (delid IS NOT NULL) THEN
		NEW.del_id = delid;
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER preserve_deletion_consistency_on_data_set_relationships 
  BEFORE UPDATE ON data_set_relationships_all
	FOR EACH ROW 
	EXECUTE PROCEDURE preserve_deletion_consistency_on_data_set_relationships();

CREATE OR REPLACE FUNCTION preserve_deletion_consistency_on_sample_relationships() RETURNS trigger AS $$
DECLARE
  delid  TECH_ID;
BEGIN
	IF (NEW.del_id IS NOT NULL OR OLD.del_id IS NULL) THEN
		RETURN NEW;
	END IF;
	SELECT del_id INTO delid
		FROM SAMPLES_ALL where id = NEW.sample_id_parent;
	IF (delid IS NOT NULL) THEN
		NEW.del_id = delid;
	END IF;
	SELECT del_id INTO delid
		FROM SAMPLES_ALL where id = NEW.sample_id_child;
	IF (delid IS NOT NULL) THEN
		NEW.del_id = delid;
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER preserve_deletion_consistency_on_sample_relationships 
  BEFORE UPDATE ON sample_relationships_all
	FOR EACH ROW 
	EXECUTE PROCEDURE preserve_deletion_consistency_on_sample_relationships();
	

CREATE OR REPLACE FUNCTION check_deletion_consistency_on_sample_deletion() RETURNS trigger AS $$
DECLARE
  counter  INTEGER;
BEGIN
	IF (OLD.del_id IS NOT NULL OR NEW.del_id IS NULL) THEN
		RETURN NEW;
	END IF;

  -- all directly connected data sets need to be deleted
  -- check datasets
	SELECT count(*) INTO counter 
	  FROM data
	  WHERE data.samp_id = NEW.id AND data.del_id IS NULL;
	IF (counter > 0) THEN
	  RAISE EXCEPTION 'Sample (Code: %) deletion failed because at least one of its data sets was not deleted.', NEW.code;
	END IF;
  -- all components need to be deleted
	SELECT count(*) INTO counter 
	  FROM samples 
	  WHERE samples.samp_id_part_of = NEW.id AND samples.del_id IS NULL;
	IF (counter > 0) THEN
	  RAISE EXCEPTION 'Sample (Code: %) deletion failed because at least one of its component samples was not deleted.', NEW.code;
	END IF;
	-- all children need to be deleted
	SELECT count(*) INTO counter 
		FROM sample_relationships_all sr, samples sc
		WHERE sample_id_parent = NEW.id AND sc.id = sr.sample_id_child AND sc.del_id IS NULL;
	IF (counter > 0) THEN
		RAISE EXCEPTION 'Sample (Code: %) deletion failed because at least one of its child samples was not deleted.', NEW.code;
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';