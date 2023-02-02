-- Migration from 081 to 092
-- S122
--
-- Create for each microscopy image data set type *IMG* create a corresponding 
-- *IMG_CONTAINER*
--

CREATE OR REPLACE FUNCTION copy_image_container_properties(old_type text, container_type text) RETURNS void AS $$
DECLARE
    properties_num integer;
BEGIN
		select count(*) 
			 into properties_num
			 from data_set_type_property_types 
			 join data_set_types org_type on dsty_id = org_type.id
			where org_type.code = container_type;

    if properties_num = 0 then
			insert into data_set_type_property_types(id, dsty_id, 
					prty_id,is_mandatory, is_managed_internally, pers_id_registerer, ordinal, section, script_id, is_shown_edit)
					(select nextval('dstpt_id_seq'), 
			   		     (select id from data_set_types where code = container_type),
			   		     org.prty_id,org.is_mandatory, org.is_managed_internally, org.pers_id_registerer, org.ordinal, 
								 org.section, org.script_id, org.is_shown_edit
	         from data_set_type_property_types org
	   			 join data_set_types org_type on org.dsty_id = org_type.id
	  			where org_type.code = old_type);
    end if;
    
END;
$$ LANGUAGE 'plpgsql';



CREATE OR REPLACE FUNCTION create_image_container_data_set_types() RETURNS void AS $$
DECLARE
    container_id TECH_ID;
    img_type RECORD;
BEGIN
	

    FOR img_type IN 
	        SELECT code, replace(code, 'IMG', 'IMG_CONTAINER') as ctnr_code 
	          FROM data_set_types 
	          where (code like '%IMG%') and (NOT code like '%IMG_CONTAINER%') 
    LOOP

				SELECT id into container_id from data_set_types where code = img_type.ctnr_code;
				
			  IF container_id IS NULL THEN
			  
					insert into data_set_types(id, code, description, is_container, dbin_id) 
					      values (nextval('DATA_SET_TYPE_ID_SEQ'), 
			                             img_type.ctnr_code, 
			                             'Container for Microscopy images of different resolutions.', 
			                             'T', 
			                             (select id from database_instances where is_original_source = 'T'));
			                             
	         PERFORM (SELECT copy_image_container_properties(img_type.code, img_type.ctnr_code));
			                             
				END IF;
    END LOOP;
    
END;
$$ LANGUAGE 'plpgsql';



SELECT create_image_container_data_set_types();

DROP FUNCTION create_image_container_data_set_types();
DROP FUNCTION copy_image_container_properties(text, text);


-------------------------------------
-- Introduce property type RESOLUTION
-------------------------------------
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
,'RESOLUTION'
,true
,'Images resolution'
,'Resolution'
,(select id from data_types where code ='VARCHAR')
,(select id from persons where user_id ='system')
,false
,(select id from database_instances where is_original_source = 'T')
);

-------------------------------------------------------
-- Assign RESOLUTION property to certain data set types
-------------------------------------------------------

CREATE OR REPLACE FUNCTION assign_resolution_property() RETURNS integer AS $$
DECLARE
    rec RECORD;
    next_ordinal ordinal_int;
BEGIN
    FOR rec IN select id from data_set_types where code = 'HCS_IMAGE_OVERVIEW' LOOP
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

insert into file_format_types (id, code, dbin_id) select nextval('file_format_type_id_seq') as id, 'JP2' as code, dbin_id from file_format_types where code = 'JPG';
