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
--  Purpose:  Create Controlled Vocabulary PLATE_GEOMETRY
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
       , 'PLATE_GEOMETRY'
       , true
       , 'The geometry or dimensions of a plate'
       , (select id from persons where user_id ='system')
       , true
       ,(select id from database_instances where code = 'SYSTEM_DEFAULT'));


-----------------------------------------------------------------------------------
--  Purpose:  Create Controlled Vocabulary Terms for PLATE_GEOMETRY
-----------------------------------------------------------------------------------
insert into controlled_vocabulary_terms 
       ( id
       , code
       , covo_id 
       , pers_id_registerer)
values  (nextval('CVTE_ID_SEQ')
       , '96_WELLS_8X12'
       , (select id from controlled_vocabularies where code = 'PLATE_GEOMETRY' and is_internal_namespace = true)
       , (select id from persons where user_id ='system'));

insert into controlled_vocabulary_terms 
       ( id
       , code
       , covo_id 
       , pers_id_registerer)
values  (nextval('CVTE_ID_SEQ')
       , '384_WELLS_16X24'
       , (select id from controlled_vocabularies where code = 'PLATE_GEOMETRY' and is_internal_namespace = true)
       , (select id from persons where user_id ='system'));

insert into controlled_vocabulary_terms 
       ( id
       , code
       , covo_id 
       , pers_id_registerer)
values  (nextval('CVTE_ID_SEQ')
       , '1536_WELLS_32X48'
       , (select id from controlled_vocabularies where code = 'PLATE_GEOMETRY' and is_internal_namespace = true)
       , (select id from persons where user_id ='system'));

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


-------------------------------------------------------------------------
--  Purpose:  Insert an initial data set into the table EXPERIMENT_TYPES
--------------------------------------------------------------------------

insert into experiment_types
(id
,code
,description
,dbin_id)
values 
(nextval('EXPERIMENT_TYPE_ID_SEQ')
,'SIRNA_HCS'
,'Small Interfering RNA High Content Screening'
,(select id from database_instances where code = 'SYSTEM_DEFAULT'));

insert into experiment_types
(id
,code
,description
,dbin_id)
values 
(nextval('EXPERIMENT_TYPE_ID_SEQ')
,'COMPOUND_HCS'
,'Compound High Content Screening'
,(select id from database_instances where code = 'SYSTEM_DEFAULT'));


----------------------------------------------------------------------
--  Purpose:  Insert an initial data set into the table MATERIAL_TYPES
-----------------------------------------------------------------------

insert into material_types
(id
,code
,description
,dbin_id)
values 
(nextval('MATERIAL_TYPE_ID_SEQ')
,'VIRUS'
,'Virus'
,(select id from database_instances where code = 'SYSTEM_DEFAULT'));

insert into material_types
(id
,code
,description
,dbin_id)
values 
(nextval('MATERIAL_TYPE_ID_SEQ')
,'CELL_LINE'
,'Cell Line or Cell Culture. The growing of cells under controlled conditions.'
,(select id from database_instances where code = 'SYSTEM_DEFAULT'));

insert into material_types
(id
,code
,description
,dbin_id)
values 
(nextval('MATERIAL_TYPE_ID_SEQ')
,'GENE'
,'Gene'
,(select id from database_instances where code = 'SYSTEM_DEFAULT'));

insert into material_types
(id
,code
,description
,dbin_id)
values 
(nextval('MATERIAL_TYPE_ID_SEQ')
,'OLIGO'
,'Oligo nucleotide'
,(select id from database_instances where code = 'SYSTEM_DEFAULT'));

insert into material_types
(id
,code
,description
,dbin_id)
values 
(nextval('MATERIAL_TYPE_ID_SEQ')
,'CONTROL'
,'Control of a control layout'
,(select id from database_instances where code = 'SYSTEM_DEFAULT'));

insert into material_types
(id
,code
,description
,dbin_id)
values 
(nextval('MATERIAL_TYPE_ID_SEQ')
,'BACTERIUM'
,'Bacterium'
,(select id from database_instances where code = 'SYSTEM_DEFAULT'));

insert into material_types
(id
,code
,description
,dbin_id)
values 
(nextval('MATERIAL_TYPE_ID_SEQ')
,'COMPOUND'
,'Compound'
,(select id from database_instances where code = 'SYSTEM_DEFAULT'));


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
,'GENE_SYMBOL'
,'Gene Symbol, e.g. BMP15'
,'Gene Symbol'
,(select id from data_types where code ='VARCHAR')
,(select id from persons where user_id ='system')
,(select id from database_instances where code = 'SYSTEM_DEFAULT')
);

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
,'NUCLEOTIDE_SEQUENCE'
,'A sequence of nucleotides'
,'Nucleotide Sequence'
,(select id from data_types where code ='VARCHAR')
,(select id from persons where user_id ='system')
,(select id from database_instances where code = 'SYSTEM_DEFAULT')
);

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
,'REFSEQ'
,'NCBI Reference Sequence code, applicable to sequences of type: DNA, RNA, protein'
,'RefSeq'
,(select id from data_types where code ='VARCHAR')
,(select id from persons where user_id ='system')
,(select id from database_instances where code = 'SYSTEM_DEFAULT')
);

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
,'OFFSET'
,'Offset from the start of the sequence'
,'Offset'
,(select id from data_types where code ='INTEGER')
,(select id from persons where user_id ='system')
,(select id from database_instances where code = 'SYSTEM_DEFAULT')
);

-----------------------------------------------------------------------------------
--  Purpose:  Create property type PLATE_GEOMETRY
-----------------------------------------------------------------------------------
insert into property_types
(id
,code
,is_internal_namespace
,description
,label
,daty_id
,covo_id
,pers_id_registerer
, is_managed_internally 
,dbin_id)
values 
(nextval('PROPERTY_TYPE_ID_SEQ')
,'PLATE_GEOMETRY'
,true
,'Plate Geometry'
,'Plate Geometry'
,(select id from data_types where code ='CONTROLLEDVOCABULARY')
,(select id from controlled_vocabularies where code ='PLATE_GEOMETRY' and is_internal_namespace = true)
,(select id from persons where user_id ='system')
,true
,(select id from database_instances where code = 'SYSTEM_DEFAULT')
);


----------------------------------------------------------------------
--  Purpose:  Insert an initial data set into the table SAMPLE_TYPES
----------------------------------------------------------------------

insert into sample_types
(id
,code
,description
,dbin_id)
values 
(nextval('SAMPLE_TYPE_ID_SEQ')
,'MASTER_PLATE'
,'Master Plate'
,(select id from database_instances where code = 'SYSTEM_DEFAULT')
);

insert into sample_types
(id
,code
,description
,dbin_id
,generated_from_depth)
values 
(nextval('SAMPLE_TYPE_ID_SEQ')
,'DILUTION_PLATE'
,'Dilution Plate'
,(select id from database_instances where code = 'SYSTEM_DEFAULT')
,1
);

insert into sample_types
(id
,code
,description
,dbin_id
,generated_from_depth)
values 
(nextval('SAMPLE_TYPE_ID_SEQ')
,'CELL_PLATE'
,'Cell Plate'
,(select id from database_instances where code = 'SYSTEM_DEFAULT')
,2
);

insert into sample_types
(id
,code
,description
,dbin_id
,generated_from_depth)
values 
(nextval('SAMPLE_TYPE_ID_SEQ')
,'REINFECT_PLATE'
,'Re-infection Plate'
,(select id from database_instances where code = 'SYSTEM_DEFAULT')
,3
);

insert into sample_types
(id
,code
,description
,dbin_id)
values 
(nextval('SAMPLE_TYPE_ID_SEQ')
,'CONTROL_LAYOUT'
,'Control layout'
,(select id from database_instances where code = 'SYSTEM_DEFAULT')
);

insert into sample_types
(id
,code
,description
,dbin_id
,is_listable
,generated_from_depth
,part_of_depth)
values 
(nextval('SAMPLE_TYPE_ID_SEQ')
,'WELL'
,'Plate Well'
,(select id from database_instances where code = 'SYSTEM_DEFAULT')
,'F'
,0
,1
);


------------------------------------------------------------------------------------
--  Purpose:  Insert an initial data set into the table EXPERIMENT_TYPE_PROPERTY_TYPES
------------------------------------------------------------------------------------

----
-- Note: we rely on DESCRIPTION to be present and internally_managed for all experiment types!
----

   ----------------------------------
   --  Experiment Type SIRNA_HCS
   ----------------------------------

insert into experiment_type_property_types
(   id
   ,exty_id
   ,prty_id
   ,is_mandatory
   ,is_managed_internally
   ,pers_id_registerer
   )
values 
   (nextval('ETPT_ID_SEQ')
   ,(select id from experiment_types where code = 'SIRNA_HCS')
   ,(select id from property_types where code = 'DESCRIPTION' and is_internal_namespace = false)
   ,true
   ,true
   ,(select id from persons where user_id ='system')
);

   ----------------------------------
   --  Experiment Type COMPOUND_HCS
   ----------------------------------

insert into experiment_type_property_types
(   id
   ,exty_id
   ,prty_id
   ,is_mandatory
   ,is_managed_internally
   ,pers_id_registerer
   )
values 
   (nextval('ETPT_ID_SEQ')
   ,(select id from experiment_types where code = 'COMPOUND_HCS')
   ,(select id from property_types where code = 'DESCRIPTION' and is_internal_namespace = false)
   ,true
   ,true
   ,(select id from persons where user_id ='system')
);


------------------------------------------------------------------------------------
--  Purpose:  Insert an initial data set into the table SAMPLE_TYPE_PROPERTY_TYPES
------------------------------------------------------------------------------------

   ---------------------------------
   --  Sample Type   MASTER_PLATE
   --  Property Type PLATE_GEOMETRY   
   ---------------------------------

insert into sample_type_property_types
(   id
   ,saty_id
   ,prty_id
   ,is_mandatory
   ,is_managed_internally
   ,pers_id_registerer
   )
values 
   (nextval('STPT_ID_SEQ')
   ,(select id from sample_types where code = 'MASTER_PLATE')
   ,(select id from property_types where code = 'PLATE_GEOMETRY' and is_internal_namespace = true)
   ,true
   ,true
   ,(select id from persons where user_id ='system')
);

   ---------------------------------
   --  Sample Type   CONTROL_LAYOUT
   --  Property Type PLATE_GEOMETRY   
   ---------------------------------

insert into sample_type_property_types
(   id
   ,saty_id
   ,prty_id
   ,is_mandatory
   ,is_managed_internally
   ,pers_id_registerer
   )
values 
   (nextval('STPT_ID_SEQ')
   ,(select id from sample_types where code = 'CONTROL_LAYOUT')
   ,(select id from property_types where code = 'PLATE_GEOMETRY' and is_internal_namespace = true)
   ,true
   ,true
   ,(select id from persons where user_id ='system')
);


------------------------------------------------------------------------------------
--  Purpose:  Insert an initial data set into the table MATERIAL_TYPE_PROPERTY_TYPES
------------------------------------------------------------------------------------

----
-- Note: we rely on DESCRIPTION to be present and internally_managed for all material types!
----

   -----------------------
   --  Material Type VIRUS
   -----------------------

insert into material_type_property_types
(   id
   ,maty_id
   ,prty_id
   ,is_mandatory
   ,is_managed_internally
   ,pers_id_registerer
   )
values 
   (nextval('MTPT_ID_SEQ')
   ,(select id from material_types where code = 'VIRUS')
   ,(select id from property_types where code = 'DESCRIPTION' and is_internal_namespace = false)
   ,true
   ,true
   ,(select id from persons where user_id ='system')
);

   -----------------------
   --  Material Type BACTERIUM
   -----------------------

insert into material_type_property_types
(   id
   ,maty_id
   ,prty_id
   ,is_mandatory
   ,is_managed_internally
   ,pers_id_registerer
   )
values 
   (nextval('MTPT_ID_SEQ')
   ,(select id from material_types where code = 'BACTERIUM')
   ,(select id from property_types where code = 'DESCRIPTION' and is_internal_namespace = false)
   ,true
   ,true
   ,(select id from persons where user_id ='system')
);

   -----------------------
   --  Material Type COMPOUND
   -----------------------

insert into material_type_property_types
(   id
   ,maty_id
   ,prty_id
   ,is_mandatory
   ,is_managed_internally
   ,pers_id_registerer
   )
values 
   (nextval('MTPT_ID_SEQ')
   ,(select id from material_types where code = 'COMPOUND')
   ,(select id from property_types where code = 'DESCRIPTION' and is_internal_namespace = false)
   ,true
   ,true
   ,(select id from persons where user_id ='system')
);

   -----------------------
   --  Material Type GENE
   -----------------------

insert into material_type_property_types
(   id
   ,maty_id
   ,prty_id
   ,is_mandatory
   ,is_managed_internally
   ,pers_id_registerer
   )
values 
   (nextval('MTPT_ID_SEQ')
   ,(select id from material_types where code = 'GENE')
   ,(select id from property_types where code = 'GENE_SYMBOL' and is_internal_namespace = false)
   ,true
   ,true
   ,(select id from persons where user_id ='system')
);

insert into material_type_property_types
(   id
   ,maty_id
   ,prty_id
   ,is_mandatory
   ,is_managed_internally
   ,pers_id_registerer
   )
values 
   (nextval('MTPT_ID_SEQ')
   ,(select id from material_types where code = 'GENE')
   ,(select id from property_types where code = 'DESCRIPTION' and is_internal_namespace = false)
   ,false
   ,true
   ,(select id from persons where user_id ='system')
);


   -----------------------
   --  Material Type OLIGO
   -----------------------

insert into material_type_property_types
(   id
   ,maty_id
   ,prty_id
   ,is_mandatory
   ,is_managed_internally
   ,pers_id_registerer
   )
values 
   (nextval('MTPT_ID_SEQ')
   ,(select id from material_types where code = 'OLIGO')
   ,(select id from property_types where code = 'NUCLEOTIDE_SEQUENCE' and is_internal_namespace = false)
   ,true
   ,true
   ,(select id from persons where user_id ='system')
);

insert into material_type_property_types
(   id
   ,maty_id
   ,prty_id
   ,is_mandatory
   ,is_managed_internally
   ,pers_id_registerer
   )
values 
   (nextval('MTPT_ID_SEQ')
   ,(select id from material_types where code = 'OLIGO')
   ,(select id from property_types where code = 'OFFSET' and is_internal_namespace = false)
   ,false
   ,true
   ,(select id from persons where user_id ='system')
);

insert into material_type_property_types
(   id
   ,maty_id
   ,prty_id
   ,is_mandatory
   ,is_managed_internally
   ,pers_id_registerer
   )
values 
   (nextval('MTPT_ID_SEQ')
   ,(select id from material_types where code = 'OLIGO')
   ,(select id from property_types where code = 'DESCRIPTION' and is_internal_namespace = false)
   ,false
   ,true
   ,(select id from persons where user_id ='system')
);

   -------------------------
   --  Material Type CONTROL
   -------------------------

insert into material_type_property_types
(   id
   ,maty_id
   ,prty_id
   ,is_mandatory
   ,is_managed_internally
   ,pers_id_registerer
   )
values 
   (nextval('MTPT_ID_SEQ')
   ,(select id from material_types where code = 'CONTROL')
   ,(select id from property_types where code = 'DESCRIPTION' and is_internal_namespace = false)
   ,false
   ,true
   ,(select id from persons where user_id ='system')
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

insert into data_set_types
(id
,code
,description
,dbin_id)
values 
(nextval('DATA_SET_TYPE_ID_SEQ')
,'HCS_IMAGE'
,'High Content Screening Image'
,(select id from database_instances where code = 'SYSTEM_DEFAULT')
);

insert into data_set_types
(id
,code
,description
,dbin_id)
values 
(nextval('DATA_SET_TYPE_ID_SEQ')
,'HCS_IMAGE_ANALYSIS_DATA'
,'Data derived from analysis of HCS images'
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
,'3VPROPRIETARY'
,'Data Analysis 3V proprietary format'
,(select id from database_instances where code = 'SYSTEM_DEFAULT')
);

insert into file_format_types
(id
,code
,description
,dbin_id)
values 
(nextval('FILE_FORMAT_TYPE_ID_SEQ')
,'PLKPROPRIETARY'
,'Data Analysis Pelkmans group proprietary format'
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
