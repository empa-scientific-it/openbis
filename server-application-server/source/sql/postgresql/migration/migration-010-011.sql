----------------------------------------------------------------------------------------------
--  File: migration-010-011.sql
--
-- 
--  This script enables the migration of the database schema from 010 to 011.
-- 
--  Update History
--
--
--	Who						When				What
--	---						----				----
--	Randy (CRW)		27-11-2007		Initial Version
--  
----------------------------------------------------------------------------------------------


-- ##############################################################
-- #      C R E A T E     N E W     Structures
-- ##############################################################


--------------
-- New Domains
--------------



-------------
-- New Tables
-------------



---------------------
-- New Table Columns
---------------------

ALTER TABLE MATERIAL_PROPERTIES ADD REGISTRATION_TIMESTAMP TIME_STAMP NOT NULL;
ALTER TABLE MATERIAL_PROPERTIES ADD PERS_ID_REGISTERER TECH_ID ;

ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD REGISTRATION_TIMESTAMP TIME_STAMP NOT NULL;
ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD PERS_ID_REGISTERER TECH_ID ;

ALTER TABLE PROPERTY_TYPES ADD REGISTRATION_TIMESTAMP TIME_STAMP NOT NULL;
ALTER TABLE PROPERTY_TYPES ADD PERS_ID_REGISTERER TECH_ID ;

--------------------------
-- New Key Constraints
--------------------------

ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);

ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MTPT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);

ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);

------------------
--  New Sequences
------------------



-- ##############################################################
-- #      MIGRATE EXISTING Structures and Data
-- ##############################################################


-- ======================================================
--   Migrate the existing data to be compatible with the
--   new NOT NULL columns in the following tables:
--      material_type_property_types
--      material_properties
--      property_types
-- ======================================================

-----------------------------------------------------------
--  1. Set the PERS_ID_REGISTERER column to a default value
--     associated with the 'system' PERSON. 
-----------------------------------------------------------
update material_type_property_types
set pers_id_registerer = (select id from persons where user_id = 'system');

update material_properties
set pers_id_registerer = (select id from persons where user_id = 'system');

update property_types
set pers_id_registerer = (select id from persons where user_id = 'system');


---------------------------------------
-- 2. Set Table Columns to be MANDATORY
---------------------------------------

ALTER TABLE MATERIAL_PROPERTIES ALTER PERS_ID_REGISTERER SET NOT NULL;

ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ALTER PERS_ID_REGISTERER SET NOT NULL;

ALTER TABLE PROPERTY_TYPES ALTER PERS_ID_REGISTERER SET NOT NULL;



-- ======================================================
--  Migrate the data asssociated with the foreign key 
--  SAMP_PROC_FK data in the SAMPLES table to the 
--  SAMPLE_INPUTS table.
-- ======================================================


---------------------------------------------------------
-- 1. Insert the records of the SAMPLES table into 
--    the SAMPLE_INPUTS table
--    
---------------------------------------------------------
insert into sample_inputs
(  id
  ,samp_id 
  ,proc_id
   )
select nextval('SAMPLE_INPUT_ID_SEQ')
  ,samp.id 
  ,samp.proc_id 
from samples samp
   , procedures proc
where proc.id      = samp.proc_id
;


---------------------------------------------------------
-- 2. Drop the foreign key Constraint SAMP_PROC_FK
---------------------------------------------------------

ALTER TABLE SAMPLES DROP CONSTRAINT SAMP_PROC_FK;

------------------------------------
-- 3. Drop Table Column PROC_ID
------------------------------------

ALTER TABLE SAMPLES DROP PROC_ID;

