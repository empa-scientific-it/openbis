------------------------------------------------------------------------------------
--  Purpose:  Remove the material type CELLNUCL
------------------------------------------------------------------------------------

delete from material_types
where code = 'CELLNUCL';

------------------------------------------------------------------------------------
--  Purpose:  Rename the material type code
--            from 'CELLLINE' to 'CELL_LINE'
------------------------------------------------------------------------------------

update material_types
set   code = 'CELL_LINE'
where code = 'CELLLINE';

------------------------------------------------------------------------------------
--  Purpose:  Remove all references to the foreign key column CONT_ID.
------------------------------------------------------------------------------------

ALTER TABLE SAMPLES DROP CONT_ID;

ALTER TABLE MATERIAL_BATCHES DROP CONT_ID;

------------------------------------------------------------------------------------
--  Purpose:  Add the ON DELETE CASCADE qualifier to the foreign key MAPR_MTPT_FK of
--            the MATERIAL_PROPERTIES table (forgotten from migration 10 -> 11).
------------------------------------------------------------------------------------

ALTER TABLE MATERIAL_PROPERTIES DROP CONSTRAINT MAPR_MTPT_FK;
ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_MTPT_FK FOREIGN KEY (MTPT_ID)REFERENCES MATERIAL_TYPE_PROPERTY_TYPES(ID) ON DELETE CASCADE;

------------------------------------------------------------------------------------
--  Purpose:  Add new data types to the DATA_TYPES table.
--            In particular: BOOLEAN and TIMESTAMP.
------------------------------------------------------------------------------------

insert into data_types
(id
,code
,description)
values 
(nextval('DATA_TYPE_ID_SEQ')
,'BOOLEAN'
,'An enumerated type with values True and False'
);

insert into data_types
(id
,code
,description)
values 
(nextval('DATA_TYPE_ID_SEQ')
,'TIMESTAMP'
,'Both date and time. Format: yyyy-mm-dd hh:mm:ss'
);


------------------------------------------------------------------------------------
--  Purpose:  Rename the observable type code
--            from 'IMAGE ANALYSIS' to 'IMAGE_ANALYSIS_DATA'
------------------------------------------------------------------------------------

update observable_types
set   code = 'IMAGE_ANALYSIS_DATA'
where code = 'IMAGE ANALYSIS';


------------------------------------------------------------------------------------
--  Purpose:  Assign the property type DESCRIPTION to the existing material types,
--            except VIRUS, as an optional property type. VIRUS is not included as
--            it already possesses the property type DESCRIPTION, but as a
--            mandatory property type.
------------------------------------------------------------------------------------

insert into material_type_property_types
select  nextval('MTPT_ID_SEQ')
   ,(select id from material_types where code = 'GENE')
   ,(select id from property_types where code = 'DESCRIPTION')
   ,false
   ,now()
   ,(select id from persons where user_id ='system')
from material_type_property_types
where id in (select min(id) from material_type_property_types)
and 0 = (select count(*) from material_type_property_types
         where  maty_id = (select id from material_types where code = 'GENE')
         and    prty_id = (select id from property_types where code = 'DESCRIPTION')
        )
;


insert into material_type_property_types
select  nextval('MTPT_ID_SEQ')
   ,(select id from material_types where code = 'OLIGO')
   ,(select id from property_types where code = 'DESCRIPTION')
   ,false
   ,now()
   ,(select id from persons where user_id ='system')
from material_type_property_types
where id in (select min(id) from material_type_property_types)
and 0 = (select count(*) from material_type_property_types
         where  maty_id = (select id from material_types where code = 'OLIGO')
         and    prty_id = (select id from property_types where code = 'DESCRIPTION')
        )
;


insert into material_type_property_types
select  nextval('MTPT_ID_SEQ')
   ,(select id from material_types where code = 'CONTROL')
   ,(select id from property_types where code = 'DESCRIPTION')
   ,false
   ,now()
   ,(select id from persons where user_id ='system')
from material_type_property_types
where id in (select min(id) from material_type_property_types)
and 0 = (select count(*) from material_type_property_types
         where  maty_id = (select id from material_types where code = 'CONTROL')
         and    prty_id = (select id from property_types where code = 'DESCRIPTION')
        )
;


insert into material_type_property_types
select  nextval('MTPT_ID_SEQ')
   ,(select id from material_types where code = 'CELL_LINE')
   ,(select id from property_types where code = 'DESCRIPTION')
   ,false
   ,now()
   ,(select id from persons where user_id ='system')
from material_type_property_types
where id in (select min(id) from material_type_property_types)
and 0 = (select count(*) from material_type_property_types
         where  maty_id = (select id from material_types where code = 'CELL_LINE')
         and    prty_id = (select id from property_types where code = 'DESCRIPTION')
        )
;



-- Creating Indexes (forgotten from migration 10 -> 11)


-- Creating Index 'EXPR_PERS_FK_I'
CREATE INDEX EXPR_PERS_FK_I ON EXPERIMENT_PROPERTIES
 (PERS_ID_REGISTERER)
;

-- Creating Index 'EXPR_EXPE_FK_I'
CREATE INDEX EXPR_EXPE_FK_I ON EXPERIMENT_PROPERTIES
 (EXPE_ID)
;

-- Creating Index 'DAVA_SACO_FK_I'
CREATE INDEX DAVA_SACO_FK_I ON DATA_VALUES
 (SACO_ID)
;

-- Creating Index 'DAVA_DATA_FK_I'
CREATE INDEX DAVA_DATA_FK_I ON DATA_VALUES
 (DATA_ID)
;

-- Creating Index 'SAIN_SAMP_FK_I'
CREATE INDEX SAIN_SAMP_FK_I ON SAMPLE_INPUTS
 (SAMP_ID)
;

-- Creating Index 'SAIN_PROC_FK_I'
CREATE INDEX SAIN_PROC_FK_I ON SAMPLE_INPUTS
 (PROC_ID)
;

-- Creating Index 'SACO_SAMP_FK_I'
CREATE INDEX SACO_SAMP_FK_I ON SAMPLE_COMPONENTS
 (SAMP_ID)
;

-- Creating Index 'PRTY_PERS_FK_I'
CREATE INDEX PRTY_PERS_FK_I ON PROPERTY_TYPES
 (PERS_ID_REGISTERER)
;

-- Creating Index 'PRTY_DATY_FK_I'
CREATE INDEX PRTY_DATY_FK_I ON PROPERTY_TYPES
 (DATY_ID)
;

-- Creating Index 'PROC_PCTY_FK_I'
CREATE INDEX PROC_PCTY_FK_I ON PROCEDURES
 (PCTY_ID)
;

-- Creating Index 'PROC_PERS_FK_I'
CREATE INDEX PROC_PERS_FK_I ON PROCEDURES
 (PERS_ID)
;

-- Creating Index 'PROC_EXPE_FK_I'
CREATE INDEX PROC_EXPE_FK_I ON PROCEDURES
 (EXPE_ID)
;

-- Creating Index 'MAPR_PERS_FK_I'
CREATE INDEX MAPR_PERS_FK_I ON MATERIAL_PROPERTIES
 (PERS_ID_REGISTERER)
;

-- Creating Index 'MAPR_MATE_FK_I'
CREATE INDEX MAPR_MATE_FK_I ON MATERIAL_PROPERTIES
 (MATE_ID)
;

-- Creating Index 'MAPR_MTPT_FK_I'
CREATE INDEX MAPR_MTPT_FK_I ON MATERIAL_PROPERTIES
 (MTPT_ID)
;

-- Creating Index 'PROJ_ORGA_FK_I'
CREATE INDEX PROJ_ORGA_FK_I ON PROJECTS
 (ORGA_ID)
;

-- Creating Index 'MTPT_PERS_FK_I'
CREATE INDEX MTPT_PERS_FK_I ON MATERIAL_TYPE_PROPERTY_TYPES
 (PERS_ID_REGISTERER)
;

-- Creating Index 'MTPT_PRTY_FK_I'
CREATE INDEX MTPT_PRTY_FK_I ON MATERIAL_TYPE_PROPERTY_TYPES
 (PRTY_ID)
;

-- Creating Index 'MTPT_MATY_FK_I'
CREATE INDEX MTPT_MATY_FK_I ON MATERIAL_TYPE_PROPERTY_TYPES
 (MATY_ID)
;

-- Creating Index 'SCMA_MABA_FK_I'
CREATE INDEX SCMA_MABA_FK_I ON SAMPLE_COMPONENT_MATERIALS
 (MABA_ID)
;

-- Creating Index 'SCMA_SACO_FK_I'
CREATE INDEX SCMA_SACO_FK_I ON SAMPLE_COMPONENT_MATERIALS
 (SACO_ID)
;

-- Creating Index 'EXDA_LOTY_FK_I'
CREATE INDEX EXDA_LOTY_FK_I ON EXTERNAL_DATA
 (LOTY_ID)
;

-- Creating Index 'EXDA_FFTY_FK_I'
CREATE INDEX EXDA_FFTY_FK_I ON EXTERNAL_DATA
 (FFTY_ID)
;

-- Creating Index 'DATA_PROC_FK_I'
CREATE INDEX DATA_PROC_FK_I ON DATA
 (PROC_ID_ACQUIRED_BY)
;

-- Creating Index 'DATA_SAMP_DERIVED_FROM_FK_I'
CREATE INDEX DATA_SAMP_DERIVED_FROM_FK_I ON DATA
 (SAMP_ID_DERIVED_FROM)
;

-- Creating Index 'DATA_SAMP_FK_I'
CREATE INDEX DATA_SAMP_FK_I ON DATA
 (SAMP_ID_ACQUIRED_FROM)
;

-- Creating Index 'DATA_OBTY_FK_I'
CREATE INDEX DATA_OBTY_FK_I ON DATA
 (OBTY_ID)
;

-- Creating Index 'SAMP_PERS_FK_I'
CREATE INDEX SAMP_PERS_FK_I ON SAMPLES
 (PERS_ID_REGISTERER)
;

-- Creating Index 'SAMP_SAMP_FK_I_GENERATED_FROM'
CREATE INDEX SAMP_SAMP_FK_I_GENERATED_FROM ON SAMPLES
 (SAMP_ID_GENERATED_FROM)
;

-- Creating Index 'SAMP_SAMP_FK_I_TOP'
CREATE INDEX SAMP_SAMP_FK_I_TOP ON SAMPLES
 (SAMP_ID_TOP)
;

-- Creating Index 'SAMP_SATY_FK_I'
CREATE INDEX SAMP_SATY_FK_I ON SAMPLES
 (SATY_ID)
;

-- Creating Index 'MABA_PERS_FK_I'
CREATE INDEX MABA_PERS_FK_I ON MATERIAL_BATCHES
 (PERS_ID_REGISTERER)
;

-- Creating Index 'MABA_MATE_FK_I'
CREATE INDEX MABA_MATE_FK_I ON MATERIAL_BATCHES
 (MATE_ID)
;

-- Creating Index 'MABA_PROC_FK_I'
CREATE INDEX MABA_PROC_FK_I ON MATERIAL_BATCHES
 (PROC_ID)
;

-- Creating Index 'MATE_PERS_FK_I'
CREATE INDEX MATE_PERS_FK_I ON MATERIALS
 (PERS_ID_REGISTERER)
;

-- Creating Index 'MATE_MATY_FK_I'
CREATE INDEX MATE_MATY_FK_I ON MATERIALS
 (MATY_ID)
;

-- Creating Index 'MATE_MATE_FK_I'
CREATE INDEX MATE_MATE_FK_I ON MATERIALS
 (MATE_ID_INHIBITOR_OF)
;

-- Creating Index 'EXPE_PERS_FK_I'
CREATE INDEX EXPE_PERS_FK_I ON EXPERIMENTS
 (PERS_ID_REGISTERER)
;

-- Creating Index 'EXPE_EXTY_FK_I'
CREATE INDEX EXPE_EXTY_FK_I ON EXPERIMENTS
 (EXTY_ID)
;

-- Creating Index 'EXPE_MATE_FK_I'
CREATE INDEX EXPE_MATE_FK_I ON EXPERIMENTS
 (MATE_ID_STUDY_OBJECT)
;

-- Creating Index 'EXPE_PROJ_FK_I'
CREATE INDEX EXPE_PROJ_FK_I ON EXPERIMENTS
 (PROJ_ID)
;
