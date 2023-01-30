ALTER TABLE external_data RENAME COLUMN data_id TO id;
ALTER TABLE link_data RENAME COLUMN data_id TO id;

CREATE OR REPLACE FUNCTION EXTERNAL_DATA_STORAGE_FORMAT_CHECK() RETURNS trigger AS $$
DECLARE
   v_covo_code  CODE;
   data_code CODE;
BEGIN

   select code into v_covo_code from controlled_vocabularies
      where is_internal_namespace = true and 
         id = (select covo_id from controlled_vocabulary_terms where id = NEW.cvte_id_stor_fmt);
   -- Check if the data storage format is a term of the controlled vocabulary "STORAGE_FORMAT"
   if v_covo_code != 'STORAGE_FORMAT' then
      select code into data_code from data_all where id = NEW.id; 
      RAISE EXCEPTION 'Insert/Update of Data (Code: %) failed, as its Storage Format is %, but is required to be STORAGE_FORMAT.', data_code, v_covo_code;
   end if;

   RETURN NEW;

END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION check_data_set_kind_link() RETURNS trigger AS $$
DECLARE
    kind DATA_SET_KIND;
BEGIN
    SELECT data_set_kind INTO kind
        FROM data_all 
        WHERE id = NEW.id;
        IF (kind <> 'LINK') THEN 
            RAISE EXCEPTION 'Link data (Data Set Code: %) must reference a data set of kind LINK (is %).', 
                            NEW.id, kind;
        END IF;
    RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION check_data_set_kind_physical() RETURNS trigger AS $$
DECLARE
    kind DATA_SET_KIND;
BEGIN
    SELECT data_set_kind INTO kind
        FROM data_all 
        WHERE id = NEW.id;
        IF (kind <> 'PHYSICAL') THEN 
            RAISE EXCEPTION 'External data (Data Set Code: %) must reference a data set of kind PHYSICAL (is %).', 
                            NEW.id, kind;
        END IF;
    RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';