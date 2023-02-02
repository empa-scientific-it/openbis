-- Migration from 089 to 090
-- S120

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

SELECT copy_image_container_properties('HCS_IMAGE_RAW', 'HCS_IMAGE_CONTAINER_RAW');
SELECT copy_image_container_properties('HCS_IMAGE_SEGMENTATION', 'HCS_IMAGE_CONTAINER_SEGMENTATION');
DROP FUNCTION copy_image_container_properties(text, text);

  
  