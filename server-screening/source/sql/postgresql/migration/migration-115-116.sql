-------------------------------------------------------
-- migrate custom property types from HCS_ANALYSIS_WELL_FEATURES to HCS_ANALYSIS_WELL_FEATURES_CONTAINER
-------------------------------------------------------


insert into data_set_type_property_types( id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, ordinal, section, script_id, is_shown_edit )

select 
    nextval('dstpt_id_seq'), 
    container.id, 
    dstpt.prty_id, 
    dstpt.is_mandatory, 
    dstpt.is_managed_internally, 
    dstpt.pers_id_registerer, 
    now(), 
    dstpt.ordinal, 
    dstpt.section, 
    dstpt.script_id, 
    dstpt.is_shown_edit
from 
    data_set_type_property_types dstpt,
    data_set_types container
where 
    dstpt.dsty_id = (select dst.id from data_set_types dst where dst.code = 'HCS_ANALYSIS_WELL_FEATURES') AND
    dstpt.prty_id <> (select id from property_types where code = 'ANALYSIS_PROCEDURE') AND
    container.code = 'HCS_ANALYSIS_WELL_FEATURES_CONTAINER';