-- Migration from 017 to 018

ALTER TABLE ACQUIRED_IMAGES DROP CONSTRAINT IF EXISTS FK_IMAGES_IMG_OR_THUMB_ARC_CK;

CREATE OR REPLACE FUNCTION DELETE_UNUSED_NULLED_IMAGES() RETURNS trigger AS $$
BEGIN
	if NEW.img_id IS NULL then
		if OLD.img_id IS NOT NULL then
		  delete from images where id = OLD.img_id;
		end if;
	end if;
	if NEW.thumbnail_id IS NULL then
		if OLD.thumbnail_id IS NOT NULL then
		  delete from images where id = OLD.thumbnail_id;
		end if;
	end if;
	RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';
