-- Migration from 014 to 015, sprint 116

alter table IMAGE_TRANSFORMATIONS add column IS_DEFAULT BOOLEAN_CHAR NOT NULL DEFAULT 'F';

-- allow to define a default image transformation

CREATE OR REPLACE FUNCTION IMAGE_TRANSFORMATIONS_DEFAULT_CHECK() RETURNS trigger AS $$
DECLARE
   v_is_default boolean;
BEGIN
   if NEW.is_default = 'T' then
	   select is_default into v_is_default from IMAGE_TRANSFORMATIONS 
	   	where is_default = 'T' 
	   			  and channel_id = NEW.channel_id
	   				and id != NEW.id;
	   if v_is_default is NOT NULL then
	      RAISE EXCEPTION 'Insert/Update of image transformation (Code: %) failed, as the new record has is_default set to true and there is already a default record defined.', NEW.code;
	   end if;
   end if;

   RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER IMAGE_TRANSFORMATIONS_DEFAULT_CHECK BEFORE INSERT OR UPDATE ON IMAGE_TRANSFORMATIONS
    FOR EACH ROW EXECUTE PROCEDURE IMAGE_TRANSFORMATIONS_DEFAULT_CHECK();

-- allow to specify RGB channel colors 

alter table CHANNELS add column RED_CC INTEGER; 
alter table CHANNELS add column GREEN_CC INTEGER; 
alter table CHANNELS add column BLUE_CC INTEGER; 

update CHANNELS
   set RED_CC = 255, GREEN_CC = 0, BLUE_CC = 0
 where color = 'RED';

update CHANNELS
   set RED_CC = 0, GREEN_CC = 255, BLUE_CC = 0
 where color = 'GREEN';
 
 update CHANNELS
    set RED_CC = 0, GREEN_CC = 0, BLUE_CC = 255
  where color = 'BLUE';

update CHANNELS
   set RED_CC = 255, GREEN_CC = 255, BLUE_CC = 0
 where color = 'RED_GREEN';

update CHANNELS
   set RED_CC = 0, GREEN_CC = 255, BLUE_CC = 255
 where color = 'GREEN_BLUE';
 
 update CHANNELS
    set RED_CC = 255, GREEN_CC = 0, BLUE_CC = 255
  where color = 'RED_BLUE';

 
alter table CHANNELS drop column COLOR;

alter table CHANNELS alter column RED_CC set NOT NULL; 
alter table CHANNELS alter column GREEN_CC set NOT NULL; 
alter table CHANNELS alter column BLUE_CC set NOT NULL; 
    