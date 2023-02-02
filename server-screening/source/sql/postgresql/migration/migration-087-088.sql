-- Migration from 087 to 088

CREATE OR REPLACE FUNCTION create_image_container_data_set_type() RETURNS void AS $$
DECLARE
    container_id TECH_ID;
BEGIN
    select id into container_id from data_set_types where code = 'HCS_IMAGE_CONTAINER';
    if container_id is null then
			insert into data_set_types(id, code, description, is_container, dbin_id) 
             values (nextval('DATA_SET_TYPE_ID_SEQ'), 
                             'HCS_IMAGE_CONTAINER', 
                             'Container for HCS images of different resolutions (raw, overviews, thumbnails).', 
                             'T', 
                             (select id from database_instances where is_original_source = 'T'));
    end if;
END;
$$ LANGUAGE 'plpgsql';

SELECT create_image_container_data_set_type();
DROP FUNCTION create_image_container_data_set_type();

