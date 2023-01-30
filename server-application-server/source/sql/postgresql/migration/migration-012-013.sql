----------------------------------------------------------------------------------------------
--  File: migration-012-013.sql
--
-- 
--  This script enables the migration of the database schema from 012 to 013.
-- 
--  Update History
--
--
--	Who							When				What
--	---							----				----
--	Bernd Rinn		2008-01-04	Initial Version - add support for invalidations and material type BACTERIUM 
--	Bernd Rinn		2008-01-05	Add material type COMPOUND and material type property type entries for the new material types 
--  
----------------------------------------------------------------------------------------------

------------------------------------------------------------------------------------
--  Purpose:  Create function RENAME_SEQUENCE() that is required for renaming the sequences belonging to tables
------------------------------------------------------------------------------------

CREATE FUNCTION RENAME_SEQUENCE(OLD_NAME VARCHAR, NEW_NAME VARCHAR) RETURNS INTEGER AS $$
DECLARE
  CURR_SEQ_VAL   INTEGER;
BEGIN
  SELECT INTO CURR_SEQ_VAL NEXTVAL(OLD_NAME);
  EXECUTE 'CREATE SEQUENCE ' || NEW_NAME || ' START WITH ' || CURR_SEQ_VAL;
  EXECUTE 'DROP SEQUENCE ' || OLD_NAME;
  RETURN CURR_SEQ_VAL;
END;
$$ LANGUAGE 'plpgsql';


------------------------------------------------------------------------------------
--  Purpose:  Add table INVALIDATIONS to store invalidations
------------------------------------------------------------------------------------

CREATE TABLE INVALIDATIONS (ID TECH_ID NOT NULL,PERS_ID_REGISTERER TECH_ID NOT NULL,REGISTRATION_TIMESTAMP TIME_STAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,REASON DESCRIPTION_250);
ALTER TABLE INVALIDATIONS ADD CONSTRAINT INVA_PK PRIMARY KEY(ID);
ALTER TABLE INVALIDATIONS ADD CONSTRAINT INVA_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);
CREATE SEQUENCE INVALIDATION_ID_SEQ;
CREATE INDEX INVA_PERS_FK_I ON INVALIDATIONS (PERS_ID_REGISTERER);

------------------------------------------------------------------------------------
--  Purpose:  Add column INVA_ID to table SAMPLES in order to allow invalidation of samples  
------------------------------------------------------------------------------------

ALTER TABLE SAMPLES ADD INVA_ID TECH_ID;
ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_INVA_FK FOREIGN KEY (INVA_ID) REFERENCES INVALIDATIONS(ID);
CREATE INDEX SAMP_INVA_FK_I ON SAMPLES (INVA_ID);


------------------------------------------------------------------------------------
--  Purpose:  Rename table EXPERIMENT_PROPERTIES TO EXPERIMENT_ATTACHMENTS  
------------------------------------------------------------------------------------

ALTER TABLE EXPERIMENT_PROPERTIES DROP CONSTRAINT EXPR_PK;
ALTER TABLE EXPERIMENT_PROPERTIES DROP CONSTRAINT EXPR_BK_UK;
ALTER TABLE EXPERIMENT_PROPERTIES DROP CONSTRAINT EXPR_EXPE_FK;
ALTER TABLE EXPERIMENT_PROPERTIES DROP CONSTRAINT EXPR_PERS_FK;

ALTER TABLE EXPERIMENT_PROPERTIES RENAME TO EXPERIMENT_ATTACHMENTS;
ALTER INDEX EXPR_EXPE_FK_I RENAME TO EXAT_EXPE_FK_I;
ALTER INDEX EXPR_PERS_FK_I RENAME TO EXAT_PERS_FK_I;
SELECT RENAME_SEQUENCE('EXPERIMENT_PROPERTY_ID_SEQ', 'EXPERIMENT_ATTACHMENT_ID_SEQ');

ALTER TABLE EXPERIMENT_ATTACHMENTS ADD CONSTRAINT EXAT_PK PRIMARY KEY(ID);
ALTER TABLE EXPERIMENT_ATTACHMENTS ADD CONSTRAINT EXAT_BK_UK UNIQUE(EXPE_ID,FILE_NAME,VERSION);
ALTER TABLE EXPERIMENT_ATTACHMENTS ADD CONSTRAINT EXAT_EXPE_FK FOREIGN KEY (EXPE_ID) REFERENCES EXPERIMENTS(ID);
ALTER TABLE EXPERIMENT_ATTACHMENTS ADD CONSTRAINT EXAT_PERS_FK FOREIGN KEY (PERS_ID_REGISTERER) REFERENCES PERSONS(ID);


------------------------------------------------------------------------------------
--  Purpose:  Add the material type BACTERIUM
------------------------------------------------------------------------------------

insert into material_types
(id
,code
,description)
values 
(nextval('MATERIAL_TYPE_ID_SEQ')
,'BACTERIUM'
,'Bacterium'
);

------------------------------------------------------------------------------------
--  Purpose:  Add the material type COMPOUND
------------------------------------------------------------------------------------

insert into material_types
(id
,code
,description)
values 
(nextval('MATERIAL_TYPE_ID_SEQ')
,'COMPOUND'
,'Compound'
);

------------------------------------------------------------------------------------
--  Purpose:  Insert an initial data set into the table MATERIAL_TYPE_PROPERTY_TYPES
------------------------------------------------------------------------------------

   -----------------------
   --  Material Type BACTERIUM
   -----------------------

insert into material_type_property_types
(   id
   ,maty_id
   ,prty_id
   ,is_mandatory
   ,pers_id_registerer
   )
values 
   (nextval('MTPT_ID_SEQ')
   ,(select id from material_types where code = 'BACTERIUM')
   ,(select id from property_types where code = 'DESCRIPTION')
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
   ,pers_id_registerer
   )
values 
   (nextval('MTPT_ID_SEQ')
   ,(select id from material_types where code = 'COMPOUND')
   ,(select id from property_types where code = 'DESCRIPTION')
   ,true
   ,(select id from persons where user_id ='system')
);


------------------------------------------------------------------------------------
--  Purpose:  Change according to naming convention for CODEs
------------------------------------------------------------------------------------

update procedure_types set code='DATA_ACQUISITION' where code='DATA ACQUISITION';
update procedure_types set code='IMAGE_ANALYSIS' where code='IMAGE ANALYSIS';
update locator_types set code='RELATIVE_LOCATION' where code='RELATIVE LOCATION';
update sample_types set code='MASTER_PLATE' where code='MASTER PLATE';
update sample_types set code='DILUTION_PLATE' where code='DILUTION PLATE';
update sample_types set code='CELL_PLATE' where code='CELL PLATE';
update sample_types set code='REINFECT_PLATE' where code='REINFECT PLATE';
update sample_types set description='Re-infection Plate' where code='REINFECT_PLATE';
update sample_types set code='CONTROL_LAYOUT' where code='CONTROL LAYOUT';