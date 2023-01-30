----------------------------------------------------------------------------------------------
--  File: migration-014-015.sql
--
-- 
--  This script enables the migration of the database schema from 014 to 015.
-- 
--  Update History
--
--
--	Who			When		What
--	---			----		----
--	Charles Ramin-Wright	2008-02-19	Initial Version 
--  
----------------------------------------------------------------------------------------------

--=================================
-- New Tables and related objects
--=================================

------------------------------------------------------------------------------------
--
--      created table 'SAMPLE_TYPE_PROPERTY_TYPES' to implement entity 'SAMPLE TYPE PROPERTY TYPE'
--      created table 'SAMPLE_PROPERTIES' to implement entity 'SAMPLE PROPERTY'
--      created table 'EXPERIMENT_TYPE_PROPERTY_TYPES' to implement entity 'EXPERIMENT TYPE PROPERTY TYPE'
--      created table 'EXPERIMENT_PROPERTIES' to implement entity 'EXPERIMENT PROPERTY'
--
--      created primary key 'EXPERIMENT_PROPERTIES.EXPR_PK' for table 'EXPERIMENT_PROPERTIES'
--      created unique key 'EXPERIMENT_PROPERTIES.EXPR_BK_UK' for table 'EXPERIMENT_PROPERTIES'
--
--      created primary key 'EXPERIMENT_TYPE_PROPERTY_TYPES.ETPT_PK' for table 'EXPERIMENT_TYPE_PROPERTY_TYPES'
--      created unique key 'EXPERIMENT_TYPE_PROPERTY_TYPES.ETPT_BK_UK' for table 'EXPERIMENT_TYPE_PROPERTY_TYPES'
--
--      created primary key 'SAMPLE_PROPERTIES.SAPR_PK' for table 'SAMPLE_PROPERTIES'
--      created unique key 'SAMPLE_PROPERTIES.SAPR_BK_UK' for table 'SAMPLE_PROPERTIES'
--
--      created primary key 'SAMPLE_TYPE_PROPERTY_TYPES.STPT_PK' for table 'SAMPLE_TYPE_PROPERTY_TYPES'
--      created unique key 'SAMPLE_TYPE_PROPERTY_TYPES.STPT_BK_UK' for table 'SAMPLE_TYPE_PROPERTY_TYPES'
------------------------------------------------------------------------------------

-- Creating tables

CREATE TABLE EXPERIMENT_PROPERTIES (ID TECH_ID NOT NULL,VALUE GENERIC_VALUE,REGISTRATION_TIMESTAMP TIME_STAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,EXPE_ID TECH_ID NOT NULL,ETPT_ID TECH_ID NOT NULL,CVTE_ID TECH_ID,PERS_ID_REGISTERER TECH_ID NOT NULL);
CREATE TABLE EXPERIMENT_TYPE_PROPERTY_TYPES (ID TECH_ID NOT NULL,EXTY_ID TECH_ID NOT NULL,PRTY_ID TECH_ID NOT NULL,IS_MANDATORY BOOLEAN_CHAR NOT NULL DEFAULT 'F',IS_MANAGED_INTERNALLY BOOLEAN_CHAR NOT NULL DEFAULT 'F',PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE SAMPLE_PROPERTIES (ID TECH_ID NOT NULL,SAMP_ID TECH_ID NOT NULL,STPT_ID TECH_ID NOT NULL,VALUE GENERIC_VALUE,CVTE_ID TECH_ID,PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE SAMPLE_TYPE_PROPERTY_TYPES (ID TECH_ID NOT NULL,SATY_ID TECH_ID NOT NULL,PRTY_ID TECH_ID NOT NULL,IS_MANDATORY BOOLEAN_CHAR NOT NULL DEFAULT 'F',IS_MANAGED_INTERNALLY BOOLEAN_CHAR NOT NULL DEFAULT 'F',PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);

-- Creating primary key constraints

ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_PK PRIMARY KEY(ID);
ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT ETPT_PK PRIMARY KEY(ID);
ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_PK PRIMARY KEY(ID);
ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT STPT_PK PRIMARY KEY(ID);

-- Creating unique constraints

ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_BK_UK UNIQUE(EXPE_ID,ETPT_ID);
ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT ETPT_BK_UK UNIQUE(EXTY_ID,PRTY_ID);
ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_BK_UK UNIQUE(SAMP_ID,STPT_ID);
ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT STPT_BK_UK UNIQUE(SATY_ID,PRTY_ID);

-- Creating foreign key constraints

ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_CVTE_FK FOREIGN KEY (CVTE_ID) REFERENCES CONTROLLED_VOCABULARY_TERMS(ID);
ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_ETPT_FK FOREIGN KEY (ETPT_ID) REFERENCES EXPERIMENT_TYPE_PROPERTY_TYPES(ID) ON DELETE CASCADE;
ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_EXPE_FK FOREIGN KEY (EXPE_ID) REFERENCES EXPERIMENTS(ID);
ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);
ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT ETPT_EXTY_FK FOREIGN KEY (EXTY_ID) REFERENCES EXPERIMENT_TYPES(ID);
ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT ETPT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);
ALTER TABLE EXPERIMENT_TYPE_PROPERTY_TYPES ADD CONSTRAINT ETPT_PRTY_FK FOREIGN KEY (PRTY_ID) REFERENCES PROPERTY_TYPES(ID);
ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_CVTE_FK FOREIGN KEY (CVTE_ID) REFERENCES CONTROLLED_VOCABULARY_TERMS(ID);
ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);
ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_SAMP_FK FOREIGN KEY (SAMP_ID) REFERENCES SAMPLES(ID);
ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_STPT_FK FOREIGN KEY (STPT_ID) REFERENCES SAMPLE_TYPE_PROPERTY_TYPES(ID) ON DELETE CASCADE;
ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT STPT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);
ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT STPT_PRTY_FK FOREIGN KEY (PRTY_ID) REFERENCES PROPERTY_TYPES(ID);
ALTER TABLE SAMPLE_TYPE_PROPERTY_TYPES ADD CONSTRAINT STPT_SATY_FK FOREIGN KEY (SATY_ID) REFERENCES SAMPLE_TYPES(ID);

-- Creating check constraints

ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_CK CHECK ((VALUE IS NOT NULL AND CVTE_ID IS NULL) OR (VALUE IS NULL AND CVTE_ID IS NOT NULL));
ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_CK CHECK ((VALUE IS NOT NULL AND CVTE_ID IS NULL) OR (VALUE IS NULL AND CVTE_ID IS NOT NULL));


-- Creating sequences

CREATE SEQUENCE ETPT_ID_SEQ;
CREATE SEQUENCE EXPERIMENT_PROPERTY_ID_SEQ;
CREATE SEQUENCE SAMPLE_PROPERTY_ID_SEQ;
CREATE SEQUENCE STPT_ID_SEQ;


-- Creating indexes

CREATE INDEX ETPT_EXTY_FK_I ON EXPERIMENT_TYPE_PROPERTY_TYPES (EXTY_ID);
CREATE INDEX ETPT_PERS_FK_I ON EXPERIMENT_TYPE_PROPERTY_TYPES (PERS_ID_REGISTERER);
CREATE INDEX ETPT_PRTY_FK_I ON EXPERIMENT_TYPE_PROPERTY_TYPES (PRTY_ID);
CREATE INDEX EXPR_CVTE_FK_I ON EXPERIMENT_PROPERTIES (CVTE_ID);
CREATE INDEX EXPR_ETPT_FK_I ON EXPERIMENT_PROPERTIES (ETPT_ID);
CREATE INDEX EXPR_EXPE_FK_I ON EXPERIMENT_PROPERTIES (EXPE_ID);
CREATE INDEX EXPR_PERS_FK_I ON EXPERIMENT_PROPERTIES (PERS_ID_REGISTERER);
CREATE INDEX SAPR_CVTE_FK_I ON SAMPLE_PROPERTIES (CVTE_ID);
CREATE INDEX SAPR_PERS_FK_I ON SAMPLE_PROPERTIES (PERS_ID_REGISTERER);
CREATE INDEX SAPR_SAMP_FK_I ON SAMPLE_PROPERTIES (SAMP_ID);
CREATE INDEX SAPR_STPT_FK_I ON SAMPLE_PROPERTIES (STPT_ID);
CREATE INDEX STPT_PERS_FK_I ON SAMPLE_TYPE_PROPERTY_TYPES (PERS_ID_REGISTERER);
CREATE INDEX STPT_PRTY_FK_I ON SAMPLE_TYPE_PROPERTY_TYPES (PRTY_ID);
CREATE INDEX STPT_SATY_FK_I ON SAMPLE_TYPE_PROPERTY_TYPES (SATY_ID);



--====================================
-- Modifications of existing objects
--====================================

---------------------------------------------------------------------------------------------------
--  Purpose:  Add column IS_MANAGED_INTERNALLY to table MATERIAL_TYPE_PROPERTY_TYPES in order 
--- to allow internally managed material properties.
---------------------------------------------------------------------------------------------------

ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD IS_MANAGED_INTERNALLY BOOLEAN_CHAR NOT NULL DEFAULT 'F';

------------------------------------------------------------------------------------
--  Purpose:  Drop PERS_ID for PROCEDURES
------------------------------------------------------------------------------------

alter table procedures drop column pers_id;

------------------------------------------------------------------------------------
--  Purpose:  Rename PERS_ID into PERS_ID_REGISTERER for CONTROLLED_VOCABULARIES 
--  and CONTROLLED_VOCABULARY_TERMS
------------------------------------------------------------------------------------

alter table controlled_vocabularies rename column pers_id to pers_id_registerer;
alter table controlled_vocabulary_terms rename column pers_id to pers_id_registerer; 


---------------------------------------------------------------------------------------------------
--  Purpose:  Rename the check constraint from VALUE_OR_CVTE_ID to MAPR_CK of table MATERIAL_PROPERTIES
---------------------------------------------------------------------------------------------------

ALTER TABLE MATERIAL_PROPERTIES DROP CONSTRAINT VALUE_OR_CVTE_ID;
ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_CK CHECK ((VALUE IS NOT NULL AND CVTE_ID IS NULL) OR (VALUE IS NULL AND CVTE_ID IS NOT NULL));


---------------------------------------------------------------------------------------------------
--  Purpose:  Add the column IS_MANAGED_INTERNALLY to the CONTROLLED_VOCABULARIES table 
---------------------------------------------------------------------------------------------------
alter table controlled_vocabularies add is_managed_internally boolean_char NOT NULL DEFAULT 'F';

--====================================
-- Add Master Data
--====================================

-----------------------------------------------------------------------------------
--  Purpose:  Create Controlled Vocabulary PLATE_GEOMETRY
-----------------------------------------------------------------------------------
insert into controlled_vocabularies 
       ( id
       , code
       , description
       , pers_id_registerer
       , is_managed_internally )
values  (nextval('CONTROLLED_VOCABULARY_ID_SEQ')
       , 'PLATE_GEOMETRY'
       , 'The geometry or dimensions of a plate'
       , (select id from persons where user_id ='system')
       , true);


-----------------------------------------------------------------------------------
--  Purpose:  Create Controlled Vocabulary Terms for PLATE_GEOMETRY
-----------------------------------------------------------------------------------
insert into controlled_vocabulary_terms 
       ( id
       , code
       , covo_id 
       , pers_id_registerer)
values  (nextval('CVTE_ID_SEQ')
       , '384_WELLS_16X24'
       , (select id from controlled_vocabularies where code ='PLATE_GEOMETRY')
       , (select id from persons where user_id ='system'));


-----------------------------------------------------------------------------------
--  Purpose:  Create property type PLATE_GEOMETRY
-----------------------------------------------------------------------------------
insert into property_types
(id
,code
,description
,label
,daty_id
,covo_id
,pers_id_registerer)
values 
(nextval('PROPERTY_TYPE_ID_SEQ')
,'PLATE_GEOMETRY'
,'Plate Geometry'
,'Plate Geometry'
,(select id from data_types where code ='CONTROLLEDVOCABULARY')
,(select id from controlled_vocabularies where code ='PLATE_GEOMETRY')
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
   ,(select id from property_types where code = 'PLATE_GEOMETRY')
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
   ,(select id from property_types where code = 'PLATE_GEOMETRY')
   ,true
   ,true
   ,(select id from persons where user_id ='system')
);


--==========================================
--
--   Data Migration
--
--==========================================

---------------------------------------------------------------------------------
--  As the 2 new sample_type_property_types records were defined to be mandatory,
--  then there must be a corresponding SAMPLE_PROPERTIES record
--  for each existing SAMPLES record. 
--
--  Given the current knowledge of the data to be migrated, the following value
--  is used for each Sample Property:
--
--  Linked to the controlled vocabulary term 384_WELLS_16X24
--  of the controlled vocabulary PLATE_GEOMETRY.
---------------------------------------------------------------------------------

insert into sample_properties
     ( id
     , samp_id
     , stpt_id
     , cvte_id
     , pers_id_registerer)
select nextval('SAMPLE_PROPERTY_ID_SEQ')
     , samp.id
     , stpt.id
     , cvte.id
     , (select id from persons where user_id ='system')
from sample_types saty
, sample_type_property_types stpt
, property_types prty
, controlled_vocabularies covo
, controlled_vocabulary_terms cvte
, samples samp
where saty.id = stpt.saty_id
and   stpt.prty_id = prty.id
and   prty.covo_id = covo.id 
and   covo.id = cvte.covo_id 
and   saty.id = samp.saty_id
and   (saty.code = 'MASTER_PLATE' or saty.code = 'CONTROL_LAYOUT')
and   prty.code = 'PLATE_GEOMETRY'
and   cvte.code = '384_WELLS_16X24' ;
