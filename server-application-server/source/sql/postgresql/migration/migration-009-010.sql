----------------------------------------------------------------------------------------------
--  File: migration-009-010.sql
--
-- 
--  This script enables the migration of the database schema from 009 to 010.
-- 
--  Update History
--
--
--	Who						When				What
--	---						----				----
--	Randy (CRW)		16-11-2007	Initial Version
--  
----------------------------------------------------------------------------------------------


-- ##############################################################
-- #      C R E A T E     N E W     Structures
-- ##############################################################


--------------
-- New Domains
--------------

CREATE DOMAIN BOOLEAN_CHAR AS BOOLEAN DEFAULT FALSE;


-------------
-- New Tables
-------------

CREATE TABLE MATERIAL_TYPE_PROPERTY_TYPES  (ID TECH_ID NOT NULL,MATY_ID TECH_ID NOT NULL,PRTY_ID TECH_ID NOT NULL,IS_MANDATORY BOOLEAN_CHAR NOT NULL DEFAULT 'F') ;


---------------------
-- New Table Columns
---------------------


--------------------------
-- New Key Constraints
--------------------------

ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MTPT_PK PRIMARY KEY(ID);
ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MTPT_BK_UK UNIQUE(MATY_ID,PRTY_ID);
ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MTPT_MATY_FK FOREIGN KEY (MATY_ID)REFERENCES MATERIAL_TYPES(ID);
ALTER TABLE MATERIAL_TYPE_PROPERTY_TYPES ADD CONSTRAINT MTPT_PRTY_FK FOREIGN KEY (PRTY_ID)REFERENCES PROPERTY_TYPES(ID);


------------------
--  New Sequences
------------------

--  Creating Sequence for the MATERIAL_TYPE_PROPERTY_TYPES table (standard naming convention was too long)
CREATE SEQUENCE MTPT_ID_SEQ;


-- ##############################################################
-- #      MIGRATE EXISTING Structures and Data
-- ##############################################################

------------------------------------
-- Drop Table Column DISPLAY_ORDER
------------------------------------

ALTER TABLE MATERIAL_PROPERTIES DROP DISPLAY_ORDER;



-- ======================================================
-- Migrate the data in the MATERIAL_PROPERTIES table 
-- from the old to the new structure, i.e. "switch FKs"
-- ======================================================

------------------------------
-- 1. Add Table Column MTPT_ID
------------------------------

ALTER TABLE MATERIAL_PROPERTIES ADD MTPT_ID TECH_ID;

---------------------------------------------------------
-- 2. Insert distinct Material Type - Property Type combinations into 
--    the MATERIAL_TYPE_PROPERTY_TYPES table
--    
--    Insert all occurrences as NOT Mandatory, initially.
---------------------------------------------------------
insert into material_type_property_types
(   id
   ,maty_id
   ,prty_id
   ,is_mandatory
   )
select nextval('MTPT_ID_SEQ'), maty.id, prty.id, false 
from material_types maty
, materials mate
, material_properties mapr
, property_types prty 
where maty.id      = mate.maty_id
and   mate.id      = mapr.mate_id
and   mapr.prty_id = prty.id
group by maty.id,prty.id
;

---------------------------------------------------------
-- 3. Change certain combinations to IS_MANDATORY = TRUE
--    in the MATERIAL_TYPE_PROPERTY_TYPES table
--    
---------------------------------------------------------
update MATERIAL_TYPE_PROPERTY_TYPES
set is_Mandatory = true
where maty_id = (select id from material_types where code = 'VIRUS')
and   prty_id = (select id from property_types where code = 'DESCRIPTION');

update MATERIAL_TYPE_PROPERTY_TYPES
set is_Mandatory = true
where maty_id = (select id from material_types where code = 'GENE')
and   prty_id = (select id from property_types where code = 'GENE_SYMBOL');

update MATERIAL_TYPE_PROPERTY_TYPES
set is_Mandatory = true
where maty_id = (select id from material_types where code = 'OLIGO')
and   prty_id = (select id from property_types where code = 'NUCLEOTIDE_SEQUENCE');



---------------------------------------------------------
-- 4. Link the records in the MATERIAL_PROPERTIES table
--    to the MATERIAL_TYPE_PROPERTY_TYPES table
--    via the new column MTPT_ID
---------------------------------------------------------
update material_properties mapr
set mtpt_id = (select mtpt.id 
   from material_type_property_types mtpt, property_types pt 
   where mtpt.prty_id = pt.id
   and   pt.id        = mapr.prty_id
   and   mtpt.maty_id = (select mt.id 
                        from material_types mt, materials mate 
                        where mate.maty_id = mt.id
                        and   mate.id      = mapr.mate_id
                        group by mt.id)
);

---------------------------------------------------------
-- 5. Make the column MTPT_ID mandatory
---------------------------------------------------------
ALTER TABLE MATERIAL_PROPERTIES ALTER MTPT_ID SET NOT NULL;

---------------------------------------------------------
-- 6. Add New foreign key constraint MAPR_MTPT_FK
---------------------------------------------------------
ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_MTPT_FK FOREIGN KEY (MTPT_ID)REFERENCES MATERIAL_TYPE_PROPERTY_TYPES(ID);

---------------------------------------------------------
-- 7. Drop the foreign key Constraint MAPR_PRTY_FK
---------------------------------------------------------
ALTER TABLE MATERIAL_PROPERTIES DROP CONSTRAINT MAPR_PRTY_FK;

---------------------------------------------------------
-- 8. Drop Table Column PRTY_ID
---------------------------------------------------------
ALTER TABLE MATERIAL_PROPERTIES DROP PRTY_ID;

-- ======================================================
--  Change the DATA TYPE associated with the PROPERTY TYPE 
--  'OFFSET' from VARCHAR to INTEGER
-- ======================================================
update property_types
set daty_id = (select id from data_types where code ='INTEGER')
where code = 'OFFSET'
;


-- =========================================================
--  Change the value of the CODE of the MATERIAL TYPE VIRUS 
--  to uppercase for the current values 'Ad3' and 'Ad5'. 
-- =========================================================
update materials
set code = upper(code)
where maty_id = (select id from material_types where code = 'VIRUS')
and   code in ('Ad3', 'Ad5')
;

-- =========================================================
--  Migrate the PROPERTY TYPE 'ACCESSION_NUMBER' to 'REFSEQ' 
-- =========================================================
update property_types
set code = 'REFSEQ'
, description = 'NCBI Reference Sequence code, applicable to sequences of type: DNA, RNA, protein'
, label = 'RefSeq'
, daty_id = (select id from data_types where code ='VARCHAR')
where code = 'ACCESSION_NUMBER'
;


-- ##############################################################
-- #      Insert     N E W     Data
-- ##############################################################

------------------------------------------------------------------------
-- Add three more MATERIAL TYPE PROPERTY TYPE pairs, namely:
--   1. GENE, GENE_SYMBOL, Mandatory
--   2. OLIGO, NUCLEOTIDE_SEQUENCE, Mandatory
--   3. OLIGO, OFFSET, Not Mandatory--
--
-- if the corresponding material type exists AND
-- if the corresponding property type exists AND
-- if the corresponding material type property type pair does NOT exists
------------------------------------------------------------------------

insert into material_type_property_types
(   id
   ,maty_id
   ,prty_id
   ,is_mandatory
   )
select
    nextval('MTPT_ID_SEQ')
   ,(select id from material_types where code = 'GENE')
   ,(select id from property_types where code = 'GENE_SYMBOL')
   ,true 
from material_types mt
where 1 = (select count(id) from material_types where code = 'GENE')
and   1 = (select count(id) from property_types where code = 'GENE_SYMBOL')
and   0 = (select count(id) from material_type_property_types
           where maty_id = (select id from material_types where code = 'GENE')
           and   prty_id = (select id from property_types where code = 'GENE_SYMBOL')
          )
and   mt.id = (select max(id) from material_types)
;

insert into material_type_property_types
(   id
   ,maty_id
   ,prty_id
   ,is_mandatory
   )
select
    nextval('MTPT_ID_SEQ')
   ,(select id from material_types where code = 'OLIGO')
   ,(select id from property_types where code = 'NUCLEOTIDE_SEQUENCE')
   ,true 
from material_types mt
where 1 = (select count(id) from material_types where code = 'OLIGO')
and   1 = (select count(id) from property_types where code = 'NUCLEOTIDE_SEQUENCE')
and   0 = (select count(id) from material_type_property_types
           where maty_id = (select id from material_types where code = 'OLIGO')
           and   prty_id = (select id from property_types where code = 'NUCLEOTIDE_SEQUENCE')
          )
and   mt.id = (select max(id) from material_types)
;


insert into material_type_property_types
(   id
   ,maty_id
   ,prty_id
   ,is_mandatory
   )
select
    nextval('MTPT_ID_SEQ')
   ,(select id from material_types where code = 'OLIGO')
   ,(select id from property_types where code = 'OFFSET')
   ,false 
from material_types mt
where 1 = (select count(id) from material_types where code = 'OLIGO')
and   1 = (select count(id) from property_types where code = 'OFFSET')
and   0 = (select count(id) from material_type_property_types
           where maty_id = (select id from material_types where code = 'OLIGO')
           and   prty_id = (select id from property_types where code = 'OFFSET')
          )
and   mt.id = (select max(id) from material_types)
;




