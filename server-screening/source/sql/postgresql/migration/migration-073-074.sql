---------------------------------------------
-- Introduce property type ANALYSIS_PROCEDURE
---------------------------------------------
insert into property_types
(id
,code
,is_internal_namespace
,description
,label
,daty_id
,pers_id_registerer
, is_managed_internally 
,dbin_id)
values 
(nextval('PROPERTY_TYPE_ID_SEQ')
,'ANALYSIS_PROCEDURE'
,true
,'Analysis procedure code'
,'Analysis procedure'
,(select id from data_types where code ='VARCHAR')
,(select id from persons where user_id ='system')
,false
,(select id from database_instances where is_original_source = 'T')
);

-----------------------------------------------------
-- Assign ANALYSIS_PROPERTY to certain data set types
-----------------------------------------------------

CREATE OR REPLACE FUNCTION assign_analysis_property() RETURNS integer AS $$
DECLARE
    rec RECORD;
    next_ordinal ordinal_int;
BEGIN
    FOR rec IN select id from data_set_types where code = 'HCS_IMAGE_ANALYSIS_DATA' 
                                                or code like 'HCS_ANALYSIS_WELL%' 
                                                or code like 'HCS_IMAGE%OVERLAY%' 
                                                or code like 'HCS_IMAGE_SEGMENTATION%' LOOP
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
				   ,(select id from property_types where code = 'ANALYSIS_PROCEDURE' and is_internal_namespace = true)
				   ,false
				   ,false
				   ,(select id from persons where user_id ='system')
				   ,next_ordinal
				);
    END LOOP;
    RETURN 1;
END;
$$ LANGUAGE 'plpgsql';

SELECT assign_analysis_property();
DROP FUNCTION assign_analysis_property();

