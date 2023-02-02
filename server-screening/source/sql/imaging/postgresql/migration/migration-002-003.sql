-----------------------------------
-- Migration 002-003
-----------------------------------

-- Create Table EVENTS

CREATE TABLE EVENTS (
  LAST_SEEN_DELETION_EVENT_ID TECH_ID NOT NULL
);

-- Create trigger deleting images unused in acquired_images

CREATE OR REPLACE FUNCTION DELETE_UNUSED_IMAGES() RETURNS trigger AS $$
BEGIN
   delete from images where id = OLD.img_id or id = OLD.thumbnail_id;
   RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER UNUSED_IMAGES AFTER DELETE ON ACQUIRED_IMAGES
    FOR EACH ROW EXECUTE PROCEDURE DELETE_UNUSED_IMAGES();
