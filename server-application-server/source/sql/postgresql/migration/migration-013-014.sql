----------------------------------------------------------------------------------------------
--  File: migration-013-014.sql
--
-- 
--  This script enables the migration of the database schema from 013 to 014.
-- 
--  Update History
--
--
--	Who			When		What
--	---			----		----
--	Charles Ramin-Wright	2008-01-29	Initial Version 
--  
----------------------------------------------------------------------------------------------

--=================================
-- New Tables and related objects
--=================================

------------------------------------------------------------------------------------
--  Purpose:  Add tables CONTROLLED_VOCABULARIES and CONTROLLED_VOCABULARY_TERMS
------------------------------------------------------------------------------------

CREATE TABLE CONTROLLED_VOCABULARIES (ID TECH_ID NOT NULL,CODE CODE NOT NULL,DESCRIPTION DESCRIPTION_80,REGISTRATION_TIMESTAMP TIME_STAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,PERS_ID TECH_ID NOT NULL);
CREATE TABLE CONTROLLED_VOCABULARY_TERMS (ID TECH_ID NOT NULL,CODE OBJECT_NAME NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,COVO_ID TECH_ID NOT NULL,PERS_ID TECH_ID NOT NULL);


-- Creating primary key constraints

ALTER TABLE CONTROLLED_VOCABULARIES ADD CONSTRAINT COVO_PK PRIMARY KEY(ID);
ALTER TABLE CONTROLLED_VOCABULARY_TERMS ADD CONSTRAINT CVTE_PK PRIMARY KEY(ID);

-- Creating unique constraints

ALTER TABLE CONTROLLED_VOCABULARIES ADD CONSTRAINT COVO_BK_UK UNIQUE(CODE);
ALTER TABLE CONTROLLED_VOCABULARY_TERMS ADD CONSTRAINT CVTE_BK_UK UNIQUE(CODE,COVO_ID);

-- Creating foreign key constraints

ALTER TABLE CONTROLLED_VOCABULARIES ADD CONSTRAINT COVO_PERS_FK FOREIGN KEY (PERS_ID) REFERENCES PERSONS(ID);
ALTER TABLE CONTROLLED_VOCABULARY_TERMS ADD CONSTRAINT CVTE_COVO_FK FOREIGN KEY (COVO_ID) REFERENCES CONTROLLED_VOCABULARIES(ID);
ALTER TABLE CONTROLLED_VOCABULARY_TERMS ADD CONSTRAINT CVTE_PERS_FK FOREIGN KEY (PERS_ID) REFERENCES PERSONS(ID);

-- Creating sequences

CREATE SEQUENCE CONTROLLED_VOCABULARY_ID_SEQ;
CREATE SEQUENCE CVTE_ID_SEQ;


-- Creating indexes

CREATE INDEX COVO_PERS_FK_I ON CONTROLLED_VOCABULARIES (PERS_ID);
CREATE INDEX CVTE_COVO_FK_I ON CONTROLLED_VOCABULARY_TERMS (COVO_ID);
CREATE INDEX CVTE_PERS_FK_I ON CONTROLLED_VOCABULARY_TERMS (PERS_ID);


--====================================
-- Modifications of existing objects
--====================================

---------------------------------------------------------------------------------------------------
--  Purpose:  Add column INVA_ID to table EXPERIMENTS in order to allow invalidation of experiments
---------------------------------------------------------------------------------------------------

ALTER TABLE EXPERIMENTS ADD INVA_ID TECH_ID;
ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_INVA_FK FOREIGN KEY (INVA_ID) REFERENCES INVALIDATIONS(ID);
CREATE INDEX EXPE_INVA_FK_I ON EXPERIMENTS (INVA_ID);

------------------------------------------------------------------------------------
--  Purpose:  Add column SAMP_ID_CONTROL_LAYOUT to table SAMPLES in order to allow 
--            the association of a sample of type CONTROL_LAYOUT to samples
------------------------------------------------------------------------------------

ALTER TABLE SAMPLES ADD SAMP_ID_CONTROL_LAYOUT TECH_ID;
ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_SAMP_FK_CONTROL_LAYOUT FOREIGN KEY (SAMP_ID_CONTROL_LAYOUT) REFERENCES SAMPLES(ID);
CREATE INDEX SAMP_SAMP_FK_I_CONTROL_LAYOUT ON SAMPLES (SAMP_ID_CONTROL_LAYOUT);


--------------------------------------------------------------
--  Purpose:  Add column CVTE_ID to table MATERIAL_PROPERTIES 
--------------------------------------------------------------
ALTER TABLE MATERIAL_PROPERTIES ADD CVTE_ID TECH_ID;
ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_CVTE_FK FOREIGN KEY (CVTE_ID) REFERENCES CONTROLLED_VOCABULARY_TERMS(ID);
CREATE INDEX MAPR_CVTE_FK_I ON MATERIAL_PROPERTIES (CVTE_ID);

--------------------------------------------------------------
--  Purpose:  Change column VALUE from mandatory to optional
--------------------------------------------------------------
ALTER TABLE MATERIAL_PROPERTIES ALTER VALUE DROP NOT NULL;

-- Creating check constraints

ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT VALUE_OR_CVTE_ID CHECK ((value IS NOT NULL and cvte_id IS NULL) or (value IS NULL and cvte_id IS NOT NULL));


--------------------------------------------------------------
--  Purpose:  Add column COVO_ID to table PROPERTY_TYPES  
--------------------------------------------------------------
ALTER TABLE PROPERTY_TYPES ADD COVO_ID TECH_ID;
ALTER TABLE PROPERTY_TYPES ADD CONSTRAINT PRTY_COVO_FK FOREIGN KEY (COVO_ID) REFERENCES CONTROLLED_VOCABULARIES(ID);
CREATE INDEX PRTY_COVO_FK_I ON PROPERTY_TYPES (COVO_ID);


------------------------------------------------------------------------------------
--  Purpose:  Create trigger CONTROLLED_VOCABULARY_CHECK 
------------------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION CONTROLLED_VOCABULARY_CHECK() RETURNS trigger AS $$
DECLARE
   cnt     INTEGER;
   v_code  CODE;
BEGIN

   select code into v_code from data_types where id  = NEW.daty_id;

   -- Check if the data is of type "CONTROLLEDVOCABULARY"
   if v_code = 'CONTROLLEDVOCABULARY' then
      if NEW.covo_id IS NULL then
         RAISE EXCEPTION 'Insert/Update of Property Type (Code: %) failed, as its Data Type is CONTROLLEDVOCABULARY, but it is not linked to a Controlled Vocabulary.', NEW.code;
      end if;
   end if;

   RETURN NEW;

END;
$$ LANGUAGE 'plpgsql';


CREATE TRIGGER CONTROLLED_VOCABULARY_CHECK BEFORE INSERT OR UPDATE ON PROPERTY_TYPES
    FOR EACH ROW EXECUTE PROCEDURE CONTROLLED_VOCABULARY_CHECK();


--====================================
-- Deletion of existing objects
--====================================

drop trigger br001_crtl_plate_chk on samples;
drop function br001_crtl_plate_chk();


--====================================
-- Add Master Data
--====================================

-----------------------------------------------------------------------------------
--  Purpose:  Insert into the table DATA_TYPES
--
--            Create the data type CONTROLLEDVOCABULARY.
-----------------------------------------------------------------------------------
insert into data_types
(id
,code
,description)
values 
(nextval('DATA_TYPE_ID_SEQ')
,'CONTROLLEDVOCABULARY'
,'Controlled Vocabulary'
);

--==========================================
--
--   Data Migration
--
--==========================================

-----------------------------------------------------------------------------------
--  Purpose:  Migrate the CONTROL_LAYOUTs from the SAMPLE_INPUTS table 
--            to the foreign key SAMP_SAMP_FK_CONTROL_LAYOUT in the SAMPLES table
--
--            This results in having all CONTROL_LAYOUTs being directly linked 
--            to CELL_PLATEs.            
-----------------------------------------------------------------------------------

UPDATE samples cell_plate
   SET samp_id_control_layout = 
          (select control_layout.samp_id
           from   sample_inputs control_layout inner join samples s 
           on     control_layout.samp_id = s.id
           where  s.saty_id = (select id from sample_types where code = 'CONTROL_LAYOUT')
           and    proc_id = (
              select  proc_id
              from sample_inputs cp
              where cp.samp_id = cell_plate.id))
   WHERE saty_id = (select id from sample_types where code = 'CELL_PLATE')
   and   id = (select distinct (samp_id)
                  from   sample_inputs sain inner join samples s2 
                  on  sain.samp_id = cell_plate.id)
;

-----------------------------------------------------------------------------------
--   Delete the CONTROL LAYOUTs from the SAMPLE_INPUTS table
-----------------------------------------------------------------------------------

delete from sample_inputs
where  id in (select si.id 
              from   sample_inputs si inner join samples s 
              on     si.samp_id = s.id
              where  s.saty_id = (select id from sample_types where code = 'CONTROL_LAYOUT')
              )
;

