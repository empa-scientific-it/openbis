-------------------------------------------------------
-- Create MICROSCOPY_IMG_OVERVIEW data set type
-------------------------------------------------------

insert into data_set_types (id, code, description, dbin_id, is_container) values (
  nextval('data_set_type_id_seq'),
  'MICROSCOPY_IMG_OVERVIEW',
  'Overview Microscopy Images. Generated from raw images.',
  (select id from database_instances where is_original_source = 'T'),
  'F'
);

-----------------------------------------------------------------------
-- Assign RESOLUTION property to MICROSCOPY_IMG_OVERVIEW data set type
-----------------------------------------------------------------------

CREATE OR REPLACE FUNCTION assign_resolution_property() RETURNS integer AS $$
DECLARE
    rec RECORD;
    next_ordinal ordinal_int;
BEGIN
    FOR rec IN select id from data_set_types where code = 'MICROSCOPY_IMG_OVERVIEW' LOOP
        next_ordinal = (select max(ordinal)+1 from data_set_type_property_types where id = rec.id);
        if next_ordinal is null then
            next_ordinal = 1;
        end if;
        insert into data_set_type_property_types
        (   id
           ,dsty_id
           ,prty_id
           ,is_mandatory
           ,is_managed_internally
           ,pers_id_registerer
           ,ordinal
           )
        values 
           (nextval('DSTPT_ID_SEQ')
           ,rec.id
           ,(select id from property_types where code = 'RESOLUTION' and is_internal_namespace = true)
           ,false
           ,false
           ,(select id from persons where user_id ='system')
           ,next_ordinal
        );
    END LOOP;
    RETURN 1;
END;
$$ LANGUAGE 'plpgsql';

SELECT assign_resolution_property();
DROP FUNCTION assign_resolution_property();