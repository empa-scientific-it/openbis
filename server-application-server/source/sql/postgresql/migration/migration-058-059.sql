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
       RAISE EXCEPTION 'Insert/Update of Query (Name: %) failed because entity_type has to be null for GENERIC queries.', NEW.name;
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
			 RAISE EXCEPTION 'Insert/Update of Query (Name: %) failed because % Type (Code: %) does not exist.', NEW.name, NEW.query_type, NEW.entity_type_code;
		 end if;			
   end if;
   RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER query_entity_type_code_check BEFORE INSERT OR UPDATE ON queries
    FOR EACH ROW EXECUTE PROCEDURE query_entity_type_code_check();
    
------------------------------------------------------------------------------------
--  Add a new domain to capture the allowed values for ReportingPluginType
--  Store the ReportingPluginType on the DATA_STORE_SERVICES table
------------------------------------------------------------------------------------
CREATE DOMAIN DATA_STORE_SERVICE_REPORTING_PLUGIN_TYPE AS VARCHAR(40) CHECK (VALUE IN ('TABLE_MODEL', 'DSS_LINK'));
ALTER TABLE DATA_STORE_SERVICES ADD COLUMN REPORTING_PLUGIN_TYPE DATA_STORE_SERVICE_REPORTING_PLUGIN_TYPE;

------------------------------------------------------------------------------------
-- Put the sequences query_id_seq and grid_custom_columns in use
--  (by mistake, filter_id_seq had been used for queries and grid custom columns)
------------------------------------------------------------------------------------
SELECT setval('query_id_seq', nextval('filter_id_seq'));
SELECT setval('grid_custom_columns_id_seq', currval('filter_id_seq'));