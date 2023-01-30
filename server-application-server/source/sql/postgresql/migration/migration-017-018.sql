----------------------------------------------------------------------------------------------
--  File: migration-017-018.sql
--
-- 
--  This script enables the migration of the database schema from 016 to 017.
-- 
----------------------------------------------------------------------------------------------


--=================================
-- Create Objects
--=================================

-- Domains

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


--=================================
-- Data Migration
--=================================

-------------------------------------------------------------------------
--  Purpose:  Insert a new EXPERIMENT_TYPE called 'COMPOUND_HCS'
--------------------------------------------------------------------------

insert into experiment_types
(id
,code
,description)
values 
(nextval('EXPERIMENT_TYPE_ID_SEQ')
,'COMPOUND_HCS'
,'Compound High Content Screening'
);


-------------------------------------------------------------------------
--  Purpose:  Link a mandatory DESCRIPTION to the new EXPERIMENT_TYPE 
--            called 'COMPOUND_HCS'
--------------------------------------------------------------------------

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


-------------------------------------------------------------------------
--  Purpose:  Change EXPERIMENT_TYPE CODE 'SIRNAHCS' to 'SIRNA_HCS'
--------------------------------------------------------------------------

update experiment_types
set code = 'SIRNA_HCS'
where code = 'SIRNAHCS'
;

