------------------------------------------------------------------------------
--  File: migration-004-005.sql
--
-- 
--  This script enables the migration of the database schema from 004 to 005.
-- 
--  Update History
--
--
--	Who			When		What
--	---			----		----
--	Randy (CRW)		24-07-2007	Initial Version
------------------------------------------------------------------------------


---------
-- Tables
---------


----------------
-- Table Columns
----------------

ALTER TABLE SAMPLES ADD COLUMN REGISTRATION_TIMESTAMP TIME_STAMP DEFAULT CURRENT_TIMESTAMP;


--------------------------
-- Primary Key Constraints
--------------------------


---------------------------------------
-- Business (Candidate) Key Constraints
---------------------------------------


--------------------------
-- Foreign Key Constraints
--------------------------


-------------
--  Sequences
-------------



---------------------------------------------------------------------------------------
--  Set a default value for the new REGISTRATION_TIMESTAMP column in the SAMPLES table
--  for all the existing SAMPLES rows before declaring its the NOT NULL constraint.
---------------------------------------------------------------------------------------
update SAMPLES set REGISTRATION_TIMESTAMP = CURRENT_TIMESTAMP;

alter table SAMPLES alter column REGISTRATION_TIMESTAMP set NOT NULL;
