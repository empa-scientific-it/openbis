----------------------------------------------------------------------------------------------
--  File: migration-007-008.sql
--
-- 
--  This script enables the migration of the database schema from 007 to 008.
-- 
--  Update History
--
--
--	Who						When				What
--	---						----				----
--	Randy (CRW)		26-10-2007	Initial Version
--	Randy (CRW)		31-10-2007	1. Added data migration of the MOLE_ID_INHIBITOR_OF column
--                            2. Removed the first drop column DESCRIPTION command as it was
--                               before the data migration of the DESCRIPTION
--  
----------------------------------------------------------------------------------------------


--------------
-- New Domains
--------------

CREATE DOMAIN GENERIC_VALUE AS VARCHAR(1024);
CREATE DOMAIN COLUMN_LABEL AS VARCHAR(40);


-------------
-- New Tables
-------------

CREATE TABLE DATA_TYPES  (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80 NOT NULL) ;

CREATE TABLE PROPERTY_TYPES  (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80 NOT NULL,LABEL COLUMN_LABEL NOT NULL,DATY_ID TECH_ID NOT NULL) ;

CREATE TABLE MATERIAL_PROPERTIES  (ID TECH_ID NOT NULL,MATE_ID TECH_ID NOT NULL,PRTY_ID TECH_ID NOT NULL,VALUE GENERIC_VALUE NOT NULL,DISPLAY_ORDER INTEGER) ;


----------------
-- Table Columns
----------------

alter table MATERIALS add column MATE_ID_INHIBITOR_OF TECH_ID;



--------------------------
-- Key Constraints
--------------------------

ALTER TABLE DATA_TYPES ADD CONSTRAINT DATY_PK PRIMARY KEY(ID);
ALTER TABLE DATA_TYPES ADD CONSTRAINT DATY_BK_UK UNIQUE(CODE);


ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_PK PRIMARY KEY(ID);
ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_BK_UK UNIQUE(CODE);
ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_DATY_FK FOREIGN KEY (DATY_ID)REFERENCES DATA_TYPES(ID);


ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_PK PRIMARY KEY(ID);
ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_BK_UK UNIQUE(MATE_ID,PRTY_ID);
ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_PRTY_FK FOREIGN KEY (PRTY_ID)REFERENCES PROPERTY_TYPES(ID);
ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_MATE_FK FOREIGN KEY (MATE_ID)REFERENCES MATERIALS(ID);


ALTER TABLE MATERIALS DROP CONSTRAINT MATE_BK_UK;
ALTER TABLE MATERIALS ADD  CONSTRAINT MATE_BK_UK UNIQUE(CODE,MATY_ID);
ALTER TABLE MATERIALS ADD  CONSTRAINT MATE_MATE_FK FOREIGN KEY (MATE_ID_INHIBITOR_OF)REFERENCES MATERIALS(ID);


-------------
--  Sequences
-------------

CREATE SEQUENCE DATA_TYPE_ID_SEQ;

CREATE SEQUENCE PROPERTY_TYPE_ID_SEQ;

CREATE SEQUENCE MATERIAL_PROPERTY_ID_SEQ;



---------------------------------------------------------------
-- Add the master data to tables: DATA_TYPES and PROPERTY_TYPES
---------------------------------------------------------------

----------------------------------------------------------------------
--  Purpose:  Insert an initial data set into the table DATA_TYPES
--
--  Authors: Randy Ramin-Wright (CRW)
--
--  Change History:
--
--  Date          Author       Description
--  25-Oct-2007   CRW          First Draft
--
-----------------------------------------------------------------------

insert into data_types
(id
,code
,description)
values 
(nextval('DATA_TYPE_ID_SEQ')
,'VARCHAR'
,'Variable length character'
);

insert into data_types
(id
,code
,description)
values 
(nextval('DATA_TYPE_ID_SEQ')
,'INTEGER'
,'Integer'
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


----------------------------------------------------------------------
--  Purpose:  Insert an initial data set into the table PROPERTY_TYPES
--
--  Authors: Randy Ramin-Wright (CRW)
--
--  Change History:
--
--  Date          Author       Description
--  25-Oct-2007   CRW          First Draft
--
-----------------------------------------------------------------------

insert into property_types
(id
,code
,description
,label
,daty_id)
values 
(nextval('PROPERTY_TYPE_ID_SEQ')
,'DESCRIPTION'
,'A Description'
,'description'
,(select id from data_types where code ='VARCHAR')
);

insert into property_types
(id
,code
,description
,label
,daty_id)
values 
(nextval('PROPERTY_TYPE_ID_SEQ')
,'GENE SYMBOL'
,'Gene Symbol, e.g. BMP15'
,'geneSymbol'
,(select id from data_types where code ='VARCHAR')
);

insert into property_types
(id
,code
,description
,label
,daty_id)
values 
(nextval('PROPERTY_TYPE_ID_SEQ')
,'NUCLEOTIDE SEQUENCE'
,'A sequence of nucleotides'
,'sequence'
,(select id from data_types where code ='VARCHAR')
);

insert into property_types
(id
,code
,description
,label
,daty_id)
values 
(nextval('PROPERTY_TYPE_ID_SEQ')
,'ACCESSION NUMBER'
,'Accession Number of the material'
,'accessionNumber'
,(select id from data_types where code ='VARCHAR')
);

insert into property_types
(id
,code
,description
,label
,daty_id)
values 
(nextval('PROPERTY_TYPE_ID_SEQ')
,'OFFSET'
,'Offset from the start of the sequence'
,'offset'
,(select id from data_types where code ='VARCHAR')
);






---------------------------------------------------------------------------------------
--  Migrate the data associated with the DESCRIPTION column of the MATERIALS table
--  to the MATERIAL_PROPERTIES table.
---------------------------------------------------------------------------------------

insert into MATERIAL_PROPERTIES ( id, mate_id, prty_id, value, display_order ) 
       select nextval('MATERIAL_PROPERTY_ID_SEQ')
               , ID
               , (select id from property_types where code = 'DESCRIPTION')
               , DESCRIPTION
               , 50
       from materials 
       where DESCRIPTION is not null;


---------------------------------------------------------------------------------------
--  Migrate the data associated with the columns of the MOLECULES table
--  to the MATERIAL_PROPERTIES table.
---------------------------------------------------------------------------------------

-- Column: GENE_SYMBOL
----------------------
insert into MATERIAL_PROPERTIES ( id, mate_id, prty_id, value, display_order ) 
       select nextval('MATERIAL_PROPERTY_ID_SEQ')
               , MATE_ID
               , (select id from property_types where code = 'GENE SYMBOL')
               , GENE_SYMBOL
               , 2
       from MOLECULES 
       where GENE_SYMBOL is not null;


-- Column: SEQUENCE 
----------------------

insert into MATERIAL_PROPERTIES ( id, mate_id, prty_id, value, display_order ) 
       select nextval('MATERIAL_PROPERTY_ID_SEQ')
               , MATE_ID
               , (select id from property_types where code = 'NUCLEOTIDE SEQUENCE')
               , SEQUENCE
               , 4
       from MOLECULES 
       where SEQUENCE is not null;


-- Column: ACCESSION_NUMBER
---------------------------

insert into MATERIAL_PROPERTIES ( id, mate_id, prty_id, value, display_order ) 
       select nextval('MATERIAL_PROPERTY_ID_SEQ')
               , MATE_ID
               , (select id from property_types where code = 'ACCESSION NUMBER')
               , ACCESSION_NUMBER
               , 6
       from MOLECULES 
       where ACCESSION_NUMBER is not null;

-- Column: OFF_SET
----------------------

insert into MATERIAL_PROPERTIES ( id, mate_id, prty_id, value, display_order ) 
       select nextval('MATERIAL_PROPERTY_ID_SEQ')
               , MATE_ID
               , (select id from property_types where code = 'OFFSET')
               , OFF_SET
               , 8
       from MOLECULES 
       where OFF_SET is not null;


-------------------------------
-- Column: MOLE_ID_INHIBITOR_OF
-------------------------------


update MATERIALS mate
set MATE_ID_INHIBITOR_OF = 
(select MOLE_ID_INHIBITOR_OF
 from MOLECULES mole
 where mate.ID = mole.MATE_ID 
 and   MOLE_ID_INHIBITOR_OF is not null)
;


---------------------
-- Drop Tables
---------------------

drop table MOLECULES;


---------------------
-- Drop Columns
---------------------

alter table MATERIALS drop column DESCRIPTION;


-- ===================================================================================================
-- Data Migration to enable activation of the unique key constraint MABA_BK_UK

-- Steps

--   0. Create the TEMP_MATERIAL_BATCHES table.

--   1. Insert a record into TEMP_MATERIAL_BATCHES for each material batch that is duplicated
--      in the MATERIAL_BATCHES table. The record with the minimum material batch id of
--      each set of duplicate material batches is selected and inserted into TEMP_MATERIAL_BATCHES.

--   2. Insert records into SAMPLE_COMPONENT_MATERIALS for each record that is linked to 
--      a material batch that is duplicated, but insert it with the material batch id
--      created in Step 1.
   
--   3. Delete all records in the SAMPLE_COMPONENT_MATERIALS table that are linked to 
--      duplicates in the MATERIAL_BATCHES table, but are not in the TEMP_MATERIAL_BATCHES
--      table.
   
--   4. Delete all records in the MATERIAL_BATCHES table that are duplicates, 
--      but are not in the TEMP_MATERIAL_BATCHES table.
   
--   5. Create the unique key constraint MABA_BK_UK

--   6. Drop the TEMP_MATERIAL_BATCHES table.

-- ===================================================================================================

---------
-- Step 0
---------


CREATE TABLE TEMP_MATERIAL_BATCHES (ID TECH_ID NOT NULL, MATE_ID TECH_ID NOT NULL,CODE CODE NOT NULL) ;

ALTER TABLE TEMP_MATERIAL_BATCHES ADD CONSTRAINT U_MABA_PK PRIMARY KEY(ID);

ALTER TABLE TEMP_MATERIAL_BATCHES ADD CONSTRAINT U_MABA_BK_UK UNIQUE(CODE, MATE_ID);


---------
-- Step 1
---------


insert into TEMP_MATERIAL_BATCHES ( id, mate_id, code ) 
   select min(id), mate_id, code
   from material_batches
   where (mate_id, code) in (select mate_id, code
                             from material_batches
                             group by mate_id, code having count(*) > 1)
   group by mate_id, code 
;

---------
-- Step 2
---------

insert into sample_component_materials (id, maba_id, saco_id)
   select nextval('SCMA_ID_SEQ')
         ,(select id from temp_material_batches temp_maba where temp_maba.mate_id = maba.mate_id and temp_maba.code = maba.code) as temp_maba_id
         ,scma.saco_id
   from material_batches maba
      , sample_component_materials scma
   where (maba.mate_id, maba.code) in (select mate_id, code
                             from material_batches
                             group by mate_id, code having count(*) > 1)
   and maba.id not in (select id from temp_material_batches )
   and maba.id = scma.maba_id
;


---------
-- Step 3
---------

delete from sample_component_materials
where (maba_id, saco_id) in (
   select  maba.id
         , scma.saco_id
   from material_batches maba
      , sample_component_materials scma
   where (maba.mate_id, maba.code) in (select mate_id, code
                             from material_batches
                             group by mate_id, code having count(*) > 1)
   and scma.maba_id not in (select id from temp_material_batches)
   and maba.id = scma.maba_id )
;


---------
-- Step 4
---------

delete from material_batches
where (id, mate_id, code) in (
   select  id
         , mate_id
         , code
   from material_batches maba
   where (maba.mate_id, maba.code) in (select mate_id, code
                             from material_batches
                             group by mate_id, code having count(*) > 1)
   and maba.id not in (select id from temp_material_batches)
   )
;


---------
-- Step 5
---------

ALTER TABLE MATERIAL_BATCHES ADD CONSTRAINT MABA_BK_UK UNIQUE(CODE,MATE_ID);


---------
-- Step 6
---------

drop table TEMP_MATERIAL_BATCHES;

