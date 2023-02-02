-- Migration from 088 to 089
-- S119

CREATE OR REPLACE FUNCTION create_image_container_data_set_types() RETURNS void AS $$
DECLARE
    old_raw_container_id TECH_ID;
    new_raw_container_id TECH_ID;
    segm_container_id TECH_ID;
BEGIN
    select id into new_raw_container_id from data_set_types where code = 'HCS_IMAGE_CONTAINER_RAW';
    if new_raw_container_id is null then
				select id into old_raw_container_id from data_set_types where code = 'HCS_IMAGE_CONTAINER';
			  if old_raw_container_id is null then
					-- old dataset type has been deleted manually, so insert a new one
					insert into data_set_types(id, code, description, is_container, dbin_id) 
					      values (nextval('DATA_SET_TYPE_ID_SEQ'), 
			                             'HCS_IMAGE_CONTAINER_RAW', 
			                             'Container for HCS images of different resolutions (raw, overviews, thumbnails).', 
			                             'T', 
			                             (select id from database_instances where is_original_source = 'T'));
				else
					-- update the code
					update data_set_types set code = 'HCS_IMAGE_CONTAINER_RAW' where code = 'HCS_IMAGE_CONTAINER';
				end if;
    end if;
    
    select id into segm_container_id from data_set_types where code = 'HCS_IMAGE_CONTAINER_SEGMENTATION';
    if segm_container_id is null then
				insert into data_set_types(id, code, description, is_container, dbin_id) 
	             values (nextval('DATA_SET_TYPE_ID_SEQ'), 
	                             'HCS_IMAGE_CONTAINER_SEGMENTATION', 
	                             'Container for HCS segmentation (a.k.a. overlays) images of different resolutions (original, overviews, thumbnails).', 
	                             'T', 
	                             (select id from database_instances where is_original_source = 'T'));
    end if;
END;
$$ LANGUAGE 'plpgsql';

SELECT create_image_container_data_set_types();
DROP FUNCTION create_image_container_data_set_types();

