----------------------------------------------------------------------
--  Purpose:  Insert an initial data set into the table PERSONS
-----------------------------------------------------------------------

insert into persons
(id
,first_name
,last_name
,user_id
,email)
values
(nextval('PERSON_ID_SEQ')
,''
,'System User'
,'system'
,'');

-----------------------------------------------------------------------------------
--  Purpose:  Create Controlled Vocabulary STORAGE_FORMAT
-----------------------------------------------------------------------------------
insert into controlled_vocabularies 
       ( id
       , code
       , description
       , pers_id_registerer
       , is_managed_internally)
values  (nextval('CONTROLLED_VOCABULARY_ID_SEQ')
       , 'STORAGE_FORMAT'
       , 'The on-disk storage format of a data set'
       , (select id from persons where user_id ='system')
       , true);


-----------------------------------------------------------------------------------
--  Purpose:  Create Controlled Vocabulary Terms for STORAGE_FORMAT
-----------------------------------------------------------------------------------
insert into controlled_vocabulary_terms 
       ( id
       , code
       , covo_id 
       , pers_id_registerer
       , ordinal )
values  (nextval('CVTE_ID_SEQ')
       , 'PROPRIETARY'
       , (select id from controlled_vocabularies where code = 'STORAGE_FORMAT' and is_managed_internally = true)
       , (select id from persons where user_id ='system')
       , 1);

insert into controlled_vocabulary_terms 
       ( id
       , code
       , covo_id 
       , pers_id_registerer
       , ordinal)
values  (nextval('CVTE_ID_SEQ')
       , 'BDS_DIRECTORY'
       , (select id from controlled_vocabularies where code = 'STORAGE_FORMAT' and is_managed_internally = true)
       , (select id from persons where user_id ='system')
       , 2);

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

insert into data_types
(id
 ,code
 ,description)
 values 
 (nextval('DATA_TYPE_ID_SEQ')
 ,'XML'
 ,'XML document'
);

insert into data_types
(id
 ,code
 ,description)
 values 
 (nextval('DATA_TYPE_ID_SEQ')
 ,'SAMPLE'
 ,'Reference to a sample'
);

insert into data_types
(id
,code
,description)
values 
(nextval('DATA_TYPE_ID_SEQ')
,'DATE'
,'Date. Format: yyyy-mm-dd'
);

insert into data_types
(id
,code
,description)
values
(nextval('DATA_TYPE_ID_SEQ')
,'JSON'
,'Json value'
);

insert into data_types
(id
,code
,description)
values
(nextval('DATA_TYPE_ID_SEQ')
,'ARRAY_INTEGER'
,'Array of integer values'
);

insert into data_types
(id
,code
,description)
values
(nextval('DATA_TYPE_ID_SEQ')
,'ARRAY_REAL'
,'Array of floating point values'
);

insert into data_types
(id
,code
,description)
values
(nextval('DATA_TYPE_ID_SEQ')
,'ARRAY_STRING'
,'Array of string values'
);

insert into data_types
(id
,code
,description)
values
(nextval('DATA_TYPE_ID_SEQ')
,'ARRAY_TIMESTAMP'
,'Array of timestamp values'
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
,pers_id_registerer)
values 
(nextval('PROPERTY_TYPE_ID_SEQ')
,'DESCRIPTION'
,'A Description'
,'Description'
,(select id from data_types where code ='VARCHAR')
,(select id from persons where user_id ='system')
);

--------------------------------------------------------------------------
--  Purpose:  Insert an initial data set into the table EXPERIMENT_TYPES
--------------------------------------------------------------------------

insert into experiment_types
(id
,code
,description)
values 
(nextval('EXPERIMENT_TYPE_ID_SEQ')
,'UNKNOWN'
,'Unknown'
);

--------------------------------------------------------------------------
--  Purpose:  Insert an initial data set into the table SAMPLE_TYPES
--------------------------------------------------------------------------

insert into sample_types
(id
,code
,description)
values 
(nextval('SAMPLE_TYPE_ID_SEQ')
,'UNKNOWN'
,'Unknown'
);

--------------------------------------------------------------------------
--  Purpose:  Insert an initial data set into the table DATA_SET_TYPES
--------------------------------------------------------------------------

insert into data_set_types
(id
,code
,description)
values 
(nextval('DATA_SET_TYPE_ID_SEQ')
,'UNKNOWN'
,'Unknown'
);

-------------------------------------------------------------------------
--  Purpose:  Insert an initial data set into the table FILE_FORMAT_TYPES
-------------------------------------------------------------------------

insert into file_format_types
(id
,code
,description)
values 
(nextval('FILE_FORMAT_TYPE_ID_SEQ')
,'HDF5'
,'Hierarchical Data Format File, version 5'
);

insert into file_format_types
(id
,code
,description)
values 
(nextval('FILE_FORMAT_TYPE_ID_SEQ')
,'PROPRIETARY'
,'Proprietary Format File'
);

insert into file_format_types
(id
,code
,description
)
values 
(nextval('FILE_FORMAT_TYPE_ID_SEQ')
,'SRF'
,'Sequence Read Format File'
);

insert into file_format_types
(id
,code
,description
)
values 
(nextval('FILE_FORMAT_TYPE_ID_SEQ')
,'TIFF'
,'TIFF File'
);

insert into file_format_types
(id
,code
,description
)
values 
(nextval('FILE_FORMAT_TYPE_ID_SEQ')
,'TSV'
,'Tab Separated Values File'
);

insert into file_format_types
(id
,code
,description
)
values 
(nextval('FILE_FORMAT_TYPE_ID_SEQ')
,'XML'
,'XML File'
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

---------------------------------------------------------------------
--  Purpose:  Insert an initial data into table RELATIONSHIP_TYPES
---------------------------------------------------------------------

insert into relationship_types
(id, 
code, 
label, 
parent_label, 
child_label, 
description, 
pers_id_registerer, 
is_managed_internally
) 
values
(
nextval('RELATIONSHIP_TYPE_ID_SEQ'),
'PARENT_CHILD',
'Parent - Child', 
'Parent', 
'Child', 
'Parent - Child relationship', 
(select id from persons where user_id ='system'), 
'T'
);

insert into relationship_types
(id, 
code, 
label, 
parent_label, 
child_label, 
description, 
pers_id_registerer, 
is_managed_internally) 
values
(
nextval('RELATIONSHIP_TYPE_ID_SEQ'),
'PLATE_CONTROL_LAYOUT',
'Plate - Control Layout', 
'Plate', 
'Control Layout', 
'Plate - Control Layout relationship', 
(select id from persons where user_id ='system'), 
'T'
);

insert into relationship_types
(id, 
code, 
label, 
parent_label, 
child_label, 
description, 
pers_id_registerer, 
is_managed_internally) 
values
(
nextval('RELATIONSHIP_TYPE_ID_SEQ'),
'CONTAINER_COMPONENT',
'Container - Component', 
'Container', 
'Component', 
'Container - Component relationship', 
(select id from persons where user_id ='system'), 
'T');

---------------------------------------------------------------------
--  Purpose:  Create default space
---------------------------------------------------------------------

insert into spaces
(id,
code,
pers_id_registerer)
values
(
nextval('SPACE_ID_SEQ'),
'DEFAULT',
(select id from persons where user_id ='system')
);

---------------------------------------------------------------------
--  Purpose:  Create default project
---------------------------------------------------------------------

select nextval('PROJECT_ID_SEQ');
insert into projects
(id,
perm_id,
code,
space_id,
pers_id_registerer)
values
(
currval('PROJECT_ID_SEQ'),
to_char(now(), 'YYYYMMDDHH24MISSMS')||'-'||currval('PROJECT_ID_SEQ'),
'DEFAULT',
(select id from spaces where code = 'DEFAULT'),
(select id from persons where user_id ='system')
);

---------------------------------------------------------------------
--  Purpose:  Create default experiment
---------------------------------------------------------------------

select nextval('EXPERIMENT_ID_SEQ');
insert into experiments_all
(id,
perm_id,
code,
proj_id,
exty_id,
pers_id_registerer,
tsvector_document)
values
(
currval('EXPERIMENT_ID_SEQ'),
to_char(now(), 'YYYYMMDDHH24MISSMS')||'-'||currval('EXPERIMENT_ID_SEQ'),
'DEFAULT',
(select id from projects where code = 'DEFAULT'),
(select id from experiment_types where code = 'UNKNOWN'),
(select id from persons where user_id ='system'),
(to_char(now(), 'YYYYMMDDHH24MISSMS')||'-'||currval('EXPERIMENT_ID_SEQ') || ':1')::tsvector ||
  'DEFAULT:1'::tsvector || '/DEFAULT/DEFAULT/DEFAULT:1'::tsvector
);

---------------------------------------------------------------------
--  Purpose:  Create default sample
---------------------------------------------------------------------

select nextval('SAMPLE_ID_SEQ');
insert into samples_all
(id,
perm_id,
code,
expe_id,
space_id,
saty_id,
pers_id_registerer,
tsvector_document)
values
(
currval('SAMPLE_ID_SEQ'),
to_char(now(), 'YYYYMMDDHH24MISSMS')||'-'||currval('SAMPLE_ID_SEQ'),
'DEFAULT',
(select id from experiments where code = 'DEFAULT'),
(select id from spaces where code = 'DEFAULT'),
(select id from sample_types where code = 'UNKNOWN'),
(select id from persons where user_id ='system'),
(to_char(now(), 'YYYYMMDDHH24MISSMS')||'-'||currval('SAMPLE_ID_SEQ') || ':1')::tsvector ||
  'DEFAULT:1'::tsvector || '/DEFAULT/DEFAULT:1'::tsvector
);

