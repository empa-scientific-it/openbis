-- -------
-- Modify PERSON
-- -------

ALTER TABLE persons
    ADD COLUMN display_settings file;


-------------------------------------------------------------------------
--  Purpose:  Insert additional initial data set into the table FILE_FORMAT_TYPES
-------------------------------------------------------------------------

insert into file_format_types
(id
,code
,description
,dbin_id)
values 
(nextval('FILE_FORMAT_TYPE_ID_SEQ')
,'HDF5'
,'Hierarchical Data Format File, version 5'
,(select id from database_instances where is_original_source = 'T')
);

insert into file_format_types
(id
,code
,description
,dbin_id)
values 
(nextval('FILE_FORMAT_TYPE_ID_SEQ')
,'PROPRIETARY'
,'Proprietary Format File'
,(select id from database_instances where is_original_source = 'T')
);

insert into file_format_types
(id
,code
,description
,dbin_id)
values 
(nextval('FILE_FORMAT_TYPE_ID_SEQ')
,'SRF'
,'Sequence Read Format File'
,(select id from database_instances where is_original_source = 'T')
);

insert into file_format_types
(id
,code
,description
,dbin_id)
values 
(nextval('FILE_FORMAT_TYPE_ID_SEQ')
,'TSV'
,'Tab Separated Values File'
,(select id from database_instances where is_original_source = 'T')
);

insert into file_format_types
(id
,code
,description
,dbin_id)
values 
(nextval('FILE_FORMAT_TYPE_ID_SEQ')
,'XML'
,'XML File'
,(select id from database_instances where is_original_source = 'T')
);

-------------------------------------------------------------------------
--  Purpose:  Delete etpt assignments when entity-types are deleted 
-------------------------------------------------------------------------

ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES DROP CONSTRAINT MTPT_MATY_FK;
ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MTPT_MATY_FK FOREIGN KEY (MATY_ID) REFERENCES MATERIAL_TYPES(ID) ON DELETE CASCADE;
ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES DROP CONSTRAINT STPT_SATY_FK;
ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT STPT_SATY_FK FOREIGN KEY (SATY_ID) REFERENCES SAMPLE_TYPES(ID) ON DELETE CASCADE;
ALTER TABLE DATA_SET_TYPE_PROPERTY_TYPES DROP CONSTRAINT DSTPT_DSTY_FK;
ALTER TABLE DATA_SET_TYPE_PROPERTY_TYPES ADD CONSTRAINT DSTPT_DSTY_FK FOREIGN KEY (DSTY_ID) REFERENCES DATA_SET_TYPES(ID)  ON DELETE CASCADE;
ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES DROP CONSTRAINT ETPT_EXTY_FK;
ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT ETPT_EXTY_FK FOREIGN KEY (EXTY_ID) REFERENCES EXPERIMENT_TYPES(ID) ON DELETE CASCADE;

-------------------------------------------------------------------------
--  Purpose:  Delete property types when material-types are deleted 
-------------------------------------------------------------------------
ALTER TABLE PROPERTY_TYPES DROP CONSTRAINT PRTY_MATY_FK;
ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_MATY_FK FOREIGN KEY (MATY_PROP_ID) REFERENCES MATERIAL_TYPES(ID) ON DELETE CASCADE;

-------------------------------------------------------------------------
--  Purpose:  Delete etpt assignments when property-types are deleted 
-------------------------------------------------------------------------
ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES DROP CONSTRAINT ETPT_PRTY_FK;
ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT ETPT_PRTY_FK FOREIGN KEY (PRTY_ID) REFERENCES PROPERTY_TYPES(ID) ON DELETE CASCADE;

ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES DROP CONSTRAINT MTPT_PRTY_FK;
ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MTPT_PRTY_FK FOREIGN KEY (PRTY_ID) REFERENCES PROPERTY_TYPES(ID) ON DELETE CASCADE;

ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES DROP CONSTRAINT STPT_PRTY_FK;
ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT STPT_PRTY_FK FOREIGN KEY (PRTY_ID) REFERENCES PROPERTY_TYPES(ID) ON DELETE CASCADE;

ALTER TABLE DATA_SET_TYPE_PROPERTY_TYPES DROP CONSTRAINT DSTPT_PRTY_FK;
ALTER TABLE DATA_SET_TYPE_PROPERTY_TYPES ADD CONSTRAINT DSTPT_PRTY_FK FOREIGN KEY (PRTY_ID) REFERENCES PROPERTY_TYPES(ID) ON DELETE CASCADE;
