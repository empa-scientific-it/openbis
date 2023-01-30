------------------------------------------------------------------------------
--  File: migration-005-006.sql
--
-- 
--  This script enables the migration of the database schema from 005 to 006.
-- 
--  Update History
--
--
--	Who						When				What
--	---						----				----
--	Randy (CRW)		06-08-2007	Initial Version
--  Randy					07-08-2007	Added the new column GENE_SYMBOL
--  Randy					07-08-2007	Added the new Unique Key constraint EXDA_BK_UK to the table EXTERNAL_DATA
------------------------------------------------------------------------------


---------
-- Tables
---------


----------------
-- Table Columns
----------------

alter table SAMPLES add column PERS_ID_REGISTERER TECH_ID;
alter table MOLECULES add column GENE_SYMBOL CODE;


--------------------------
-- Primary Key Constraints
--------------------------


---------------------------------------
-- Business (Candidate) Key Constraints
---------------------------------------

ALTER TABLE EXTERNAL_DATA ADD CONSTRAINT EXDA_BK_UK UNIQUE(LOCATION);

--------------------------
-- Foreign Key Constraints
--------------------------


-------------
--  Sequences
-------------

---------------------------------------------------------------------------------------
--  Set a default value for the new PERS_ID_REGISTERER column in the SAMPLES table
--  for all the existing SAMPLES rows before declaring its the NOT NULL constraint.
---------------------------------------------------------------------------------------
update SAMPLES set PERS_ID_REGISTERER = (select ID from PERSONS where user_id = 'System');

alter table SAMPLES alter column PERS_ID_REGISTERER set NOT NULL;
