----------------------------------------------------------------------------
--  Purpose:  Insert an initial data set into the table DATABASE_INSTANCES
----------------------------------------------------------------------------

INSERT INTO database_instances(
              id
            , code
	    	, uuid
            , is_original_source)
    VALUES (  nextval('DATABASE_INSTANCE_ID_SEQ')
            , 'SYSTEM_DEFAULT'
	    	, 'SYSTEM_DEFAULT'
            , 'T');

----------------------------------------------------------------------
--  Purpose:  Insert an initial data set into the table DATA_STORES
----------------------------------------------------------------------

insert into data_stores
(id
,code
,download_url
,remote_url
,session_token
,dbin_id)
values
(nextval('DATA_STORE_ID_SEQ')
,'STANDARD'
,''
,''
,''
,(select id from database_instances where code = 'SYSTEM_DEFAULT')
);

----------------------------------------------------------------------
--  Purpose:  Insert an initial data set into the table PERSONS
-----------------------------------------------------------------------

insert into persons
(id
,first_name
,last_name
,user_id
,email
,dbin_id)
values
(nextval('PERSON_ID_SEQ')
,''
,'System User'
,'system'
,''
,(select id from database_instances where code = 'SYSTEM_DEFAULT') );

-----------------------------------------------------------------------------------
--  Purpose:  Create Controlled Vocabulary STORAGE_FORMAT
-----------------------------------------------------------------------------------
insert into controlled_vocabularies 
       ( id
       , code
       , is_internal_namespace      
       , description
       , pers_id_registerer
       , is_managed_internally
       , dbin_id )
values  (nextval('CONTROLLED_VOCABULARY_ID_SEQ')
       , 'STORAGE_FORMAT'
       , true
       , 'The on-disk storage format of a data set'
       , (select id from persons where user_id ='system')
       , true
       ,(select id from database_instances where code = 'SYSTEM_DEFAULT'));


-----------------------------------------------------------------------------------
--  Purpose:  Create Controlled Vocabulary Terms for STORAGE_FORMAT
-----------------------------------------------------------------------------------
insert into controlled_vocabulary_terms 
       ( id
       , code
       , covo_id 
       , pers_id_registerer )
values  (nextval('CVTE_ID_SEQ')
       , 'PROPRIETARY'
       , (select id from controlled_vocabularies where code = 'STORAGE_FORMAT' and is_internal_namespace = true)
       , (select id from persons where user_id ='system'));

insert into controlled_vocabulary_terms 
       ( id
       , code
       , covo_id 
       , pers_id_registerer )
values  (nextval('CVTE_ID_SEQ')
       , 'BDS_DIRECTORY'
       , (select id from controlled_vocabularies where code = 'STORAGE_FORMAT' and is_internal_namespace = true)
       , (select id from persons where user_id ='system'));

------------------------------------------------------------------
--  Purpose:  Insert an initial data set into the table DATA_TYPES
------------------------------------------------------------------

insert into data_types
(id
,code
,description)
values 
(nextval('DATA_TYPE_ID_SEQ')
,'VARCHAR'
,'Short text'
);

insert into data_types
(id
 ,code
 ,description)
 values 
 (nextval('DATA_TYPE_ID_SEQ')
 ,'MULTILINE_VARCHAR'
 ,'Long text'
);

insert into data_types
(id
,code
,description)
values 
(nextval('DATA_TYPE_ID_SEQ')
,'INTEGER'
,'Integer number'
);

insert into data_types
(id
,code
,description)
values 
(nextval('DATA_TYPE_ID_SEQ')
,'REAL'
,'Real number, i.e. an inexact, variable-precision numeric type'
);

insert into data_types
(id
,code
,description)
values 
(nextval('DATA_TYPE_ID_SEQ')
,'BOOLEAN'
,'True or False'
);

insert into data_types
(id
,code
,description)
values 
(nextval('DATA_TYPE_ID_SEQ')
,'TIMESTAMP'
,'Both date and time. Format: yyyy-mm-dd hh:mm:ss'
);

insert into data_types
(id
 ,code
 ,description)
 values 
 (nextval('DATA_TYPE_ID_SEQ')
 ,'CONTROLLEDVOCABULARY'
 ,'Controlled Vocabulary'
);

insert into data_types
(id
 ,code
 ,description)
 values 
 (nextval('DATA_TYPE_ID_SEQ')
 ,'MATERIAL'
 ,'Reference to a material'
);

insert into data_types
(id
 ,code
 ,description)
 values 
 (nextval('DATA_TYPE_ID_SEQ')
 ,'HYPERLINK'
 ,'Address of a web page'
);

----------------------------------------------------------------------
--  Purpose:  Insert an initial data set into the table PROPERTY_TYPES
-----------------------------------------------------------------------

insert into property_types
(id
,code
,description
,label
,daty_id
,pers_id_registerer
,dbin_id)
values 
(nextval('PROPERTY_TYPE_ID_SEQ')
,'DESCRIPTION'
,'A Description'
,'Description'
,(select id from data_types where code ='VARCHAR')
,(select id from persons where user_id ='system')
,(select id from database_instances where code = 'SYSTEM_DEFAULT')
);

--------------------------------------------------------------------------
--  Purpose:  Insert an initial data set into the table DATA_SET_TYPES
--------------------------------------------------------------------------

insert into data_set_types
(id
,code
,description
,dbin_id)
values 
(nextval('DATA_SET_TYPE_ID_SEQ')
,'UNKNOWN'
,'Unknown'
,(select id from database_instances where code = 'SYSTEM_DEFAULT')
);

-------------------------------------------------------------------------
--  Purpose:  Insert an initial data set into the table FILE_FORMAT_TYPES
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
,(select id from database_instances where code = 'SYSTEM_DEFAULT')
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
,(select id from database_instances where code = 'SYSTEM_DEFAULT')
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
,(select id from database_instances where code = 'SYSTEM_DEFAULT')
);

insert into file_format_types
(id
,code
,description
,dbin_id)
values 
(nextval('FILE_FORMAT_TYPE_ID_SEQ')
,'TIFF'
,'TIFF File'
,(select id from database_instances where code = 'SYSTEM_DEFAULT')
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
,(select id from database_instances where code = 'SYSTEM_DEFAULT')
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
,(select id from database_instances where code = 'SYSTEM_DEFAULT')
);

---------------------------------------------------------------------
--  Purpose:  Insert an initial data set into the table LOCATOR_TYPES
---------------------------------------------------------------------

insert into locator_types
(id
,code
,description)
values 
(nextval('LOCATOR_TYPE_ID_SEQ')
,'RELATIVE_LOCATION'
,'Relative Location'
);
