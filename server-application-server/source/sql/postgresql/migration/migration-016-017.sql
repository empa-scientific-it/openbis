----------------------------------------------------------------------------------------------
--  File: migration-016-017.sql
--
-- 
--  This script enables the migration of the database schema from 016 to 017.
-- 
--  Update History
--
--
--	Who			              When		    What
--	---			              ----      	----
--	Charles Ramin-Wright	2008-04-11	Initial Version 
--  
----------------------------------------------------------------------------------------------


--=================================
-- Create Objects
--=================================

-- Domains

-- 'F' is false, 'T' is true and 'U' is unknown
CREATE DOMAIN BOOLEAN_CHAR_OR_UNKNOWN AS CHAR(1) CHECK (VALUE in ('F', 'T', 'U'));

-- Tables and related objects

-- Sequences

-- Indices

--====================================
-- Delete Objects
--====================================

-- Domains

-- Tables

-- Sequences

-- Indices

--====================================
-- Alter Objects
--====================================

-- Add column IS_INTERNAL_NAMESPACE to table CONTROLLED_VOCABULARIES 

ALTER TABLE CONTROLLED_VOCABULARIES ADD IS_INTERNAL_NAMESPACE BOOLEAN_CHAR NOT NULL DEFAULT 'F';

-- Add column IS_INTERNAL_NAMESPACE to table PROPERTY_TYPES 

ALTER TABLE PROPERTY_TYPES ADD IS_INTERNAL_NAMESPACE BOOLEAN_CHAR NOT NULL DEFAULT 'F';

-- Add column IS_INTERNAL_NAMESPACE as key component to the unique key constraint COVO_BK_UK

ALTER TABLE CONTROLLED_VOCABULARIES DROP CONSTRAINT COVO_BK_UK;
ALTER TABLE CONTROLLED_VOCABULARIES ADD CONSTRAINT COVO_BK_UK UNIQUE(CODE,IS_INTERNAL_NAMESPACE);

-- Add column IS_INTERNAL_NAMESPACE as key component to the unique key constraint PRTY_BK_UK

ALTER TABLE PROPERTY_TYPES DROP CONSTRAINT PRTY_BK_UK;
ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_BK_UK UNIQUE(CODE,IS_INTERNAL_NAMESPACE);

-- Delete unique key constraint PRTY_BK_UK_LBL (which was for column LABEL) from table PROPERTY_TYPES

ALTER TABLE PROPERTY_TYPES DROP CONSTRAINT PRTY_BK_UK_LBL;


-- Change unique constraint EXDA_BK_UK to contain EXTERNAL_DATA.LOTY_ID 

ALTER TABLE EXTERNAL_DATA DROP CONSTRAINT EXDA_BK_UK;
ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_BK_UK UNIQUE(LOCATION,LOTY_ID);


-- Increase length of EXTERNAL_DATA.LOCATION from 200 to 1024

ALTER TABLE EXTERNAL_DATA ALTER COLUMN LOCATION TYPE VARCHAR(1024);


-- Add column IS_COMPLETE to table EXTERNAL_DATA

ALTER TABLE EXTERNAL_DATA ADD COLUMN IS_COMPLETE BOOLEAN_CHAR_OR_UNKNOWN NOT NULL DEFAULT 'U';


--=================================
-- Replace EXTERNAL_DATA_STORAGE_FORMAT_CHECK() function
--=================================

CREATE OR REPLACE FUNCTION EXTERNAL_DATA_STORAGE_FORMAT_CHECK() RETURNS trigger AS $$
DECLARE
   v_covo_code  CODE;
   data_code CODE;
BEGIN

   select code into v_covo_code from controlled_vocabularies
      where is_internal_namespace = true and 
         id = (select covo_id from controlled_vocabulary_terms where id = NEW.cvte_id_stor_fmt);

-- Check if the data storage format is a term of the controlled vocabulary "STORAGE_FORMAT"
   if v_covo_code != 'STORAGE_FORMAT' then
      select code into data_code from data where id = NEW.data_id; 
      RAISE EXCEPTION 'Insert/Update of Data (Code: %) failed, as its Storage Format is %, but is required to be STORAGE_FORMAT.', data_code, v_covo_code;
   end if;

   RETURN NEW;

END;
$$ LANGUAGE 'plpgsql';


--=================================
-- Data Migration
--=================================


---------------------------------------------------------------------------------------
--   Translate the internally managed CODES which are represented by the Dot-Prefix to:
--
--   CODE without a Dot-Prefix plus the new column IS_INTERNAL_NAMESPACE = true.
---------------------------------------------------------------------------------------

UPDATE property_types
SET    code = substr(code,2,length(code))
     , is_internal_namespace = true 
WHERE  substr(code,1,1) = '.';

UPDATE controlled_vocabularies
SET    code = substr(code,2,length(code))
     , is_internal_namespace = true 
WHERE  substr(code,1,1) = '.';

------------------------------------------------------------------------------------
--  Purpose: Move experiment descriptions from column to property DESCRIPTION
------------------------------------------------------------------------------------

   -----------------------
   --  Material Type SIRNHACS
   -----------------------

insert into experiment_type_property_types
(   id
   ,exty_id
   ,prty_id
   ,is_mandatory
   ,is_managed_internally
   ,pers_id_registerer
   )
values 
(   nextval('ETPT_ID_SEQ')
   ,(select id from experiment_types where code = 'SIRNAHCS')
   ,(select id from property_types where code = 'DESCRIPTION')
   ,true
   ,true
   ,(select id from persons where user_id ='system')
);

-- Add experiment_properties for descriptions of all experiments of type SIRNAHCS 

insert into experiment_properties
(   id
   ,expe_id
   ,value
   ,etpt_id
   ,pers_id_registerer
)
select
    nextval('EXPERIMENT_PROPERTY_ID_SEQ')
   ,id
   ,case when description is not null then description else 'No description given' end
   ,currval('ETPT_ID_SEQ')
   ,(select id from persons where user_id ='system')  
from experiments where exty_id = (select id from experiment_types where code = 'SIRNAHCS');

-- Finally: drop column

ALTER TABLE EXPERIMENTS DROP COLUMN DESCRIPTION;