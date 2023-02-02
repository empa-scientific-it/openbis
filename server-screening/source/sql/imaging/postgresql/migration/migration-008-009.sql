-- Migration from 008 to 009

ALTER TABLE channel_stacks ADD COLUMN is_representative BOOLEAN_CHAR NOT NULL DEFAULT 'F';
ALTER TABLE channel_stacks ADD COLUMN series_number INTEGER;    

--- ADD MICROSCOPY SUPPORT -----------------------------------------------------------------------

ALTER TABLE channel_stacks ALTER COLUMN spot_id DROP NOT NULL;
ALTER TABLE data_sets ALTER COLUMN cont_id DROP NOT NULL;
ALTER TABLE data_sets ADD COLUMN IMAGE_TRANSFORMER_FACTORY BYTEA;

CREATE OR REPLACE FUNCTION CHANNEL_STACKS_CHECK() RETURNS trigger AS $$
DECLARE
   v_cont_id  CODE;
BEGIN

   select cont_id into v_cont_id from data_sets where id = NEW.ds_id;

   -- Check that if there is no spot than there is no dataset container as well
   if v_cont_id IS NULL then
      if NEW.spot_id IS NOT NULL then
         RAISE EXCEPTION 'Insert/Update of CHANNEL_STACKS failed, as the dataset container is not set, but spot is (spot id = %).',NEW.spot_id;
      end if;
	 else
      if NEW.spot_id IS NULL then
         RAISE EXCEPTION 'Insert/Update of CHANNEL_STACKS failed, as the dataset container is set (id = %), but spot is not set.',v_cont_id;
      end if; 
   end if;
   RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER CHANNEL_STACKS_CHECK BEFORE INSERT OR UPDATE ON CHANNEL_STACKS
    FOR EACH ROW EXECUTE PROCEDURE CHANNEL_STACKS_CHECK();
    
--- for each spot set exactly one representative channel_stacks record (with minimal id) ---------

update channel_stacks
   set is_representative = 'T'
 where id in (select min(cs.id) 
		 from channel_stacks cs
		 join spots on spots.id = cs.spot_id
		 group by spots.id)