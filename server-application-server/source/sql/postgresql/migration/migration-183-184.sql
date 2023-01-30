UPDATE property_types SET is_managed_internally = is_managed_internally OR is_internal_namespace;
ALTER TABLE property_types DROP COLUMN is_internal_namespace;
ALTER TABLE property_types ADD CONSTRAINT prty_bk_uk UNIQUE (code, is_managed_internally);

UPDATE controlled_vocabularies SET is_managed_internally = is_managed_internally OR is_internal_namespace;
ALTER TABLE controlled_vocabularies DROP COLUMN is_internal_namespace;
ALTER TABLE controlled_vocabularies ADD CONSTRAINT covo_bk_uk UNIQUE (code, is_managed_internally);

UPDATE relationship_types SET is_managed_internally = is_managed_internally OR is_internal_namespace;
ALTER TABLE relationship_types DROP COLUMN is_internal_namespace;

CREATE OR REPLACE FUNCTION EXTERNAL_DATA_STORAGE_FORMAT_CHECK() RETURNS trigger AS $$
DECLARE
   v_covo_code  CODE;
   data_code CODE;
BEGIN

   select code into v_covo_code from controlled_vocabularies
      where is_managed_internally = true and 
         id = (select covo_id from controlled_vocabulary_terms where id = NEW.cvte_id_stor_fmt);
   -- Check if the data storage format is a term of the controlled vocabulary "STORAGE_FORMAT"
   if v_covo_code != 'STORAGE_FORMAT' then
      select code into data_code from data_all where id = NEW.id; 
      RAISE EXCEPTION 'Insert/Update of Data (Code: %) failed, as its Storage Format is %, but is required to be STORAGE_FORMAT.', data_code, v_covo_code;
   end if;

   RETURN NEW;

END;
$$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS EXTERNAL_DATA_STORAGE_FORMAT_CHECK ON EXTERNAL_DATA;
CREATE TRIGGER EXTERNAL_DATA_STORAGE_FORMAT_CHECK BEFORE INSERT OR UPDATE ON EXTERNAL_DATA
    FOR EACH ROW EXECUTE PROCEDURE EXTERNAL_DATA_STORAGE_FORMAT_CHECK();
