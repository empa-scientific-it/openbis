-- Migration from 058 to 059

-- Add ENTITY_TYPE_CODE column to QUERIES
ALTER TABLE queries ADD COLUMN entity_type_code CODE;

------------------------------------------------------------------------------------
--  Purpose:  Create trigger that checks if entity_type_code specified in a query
--            matches a code of an entity of type specified in query_type.
------------------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION query_entity_type_code_check() RETURNS trigger AS $$
DECLARE
   counter  INTEGER;
BEGIN
   if (NEW.entity_type_code IS NOT NULL) then
     if (NEW.query_type = 'GENERIC') then
       RAISE EXCEPTION 'Insert/Update of query (Name: %) failed, as entity_type has to be null for GENERIC queries.', NEW.name;
     elsif (NEW.query_type = 'EXPERIMENT') then
       SELECT count(*) INTO counter FROM experiment_types WHERE code = NEW.entity_type_code;
		 elsif (NEW.query_type = 'DATA_SET') then
       SELECT count(*) INTO counter FROM data_set_types WHERE code = NEW.entity_type_code;
		 elsif (NEW.query_type = 'MATERIAL') then
       SELECT count(*) INTO counter FROM material_types WHERE code = NEW.entity_type_code;
		 elsif (NEW.query_type = 'SAMPLE') then
       SELECT count(*) INTO counter FROM sample_types WHERE code = NEW.entity_type_code;
		 end if;
		 if (counter = 0) then
			 RAISE EXCEPTION 'Insert/Update of query (Name: %) failed because % type (Code: %) does not exist.', NEW.name, NEW.query_type, NEW.entity_type_code;
		 end if;			
   end if;
   RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER query_entity_type_code_check BEFORE INSERT OR UPDATE ON queries
    FOR EACH ROW EXECUTE PROCEDURE query_entity_type_code_check();
