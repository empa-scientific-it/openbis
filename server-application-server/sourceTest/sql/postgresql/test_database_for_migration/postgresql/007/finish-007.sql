ALTER TABLE ONLY data
    ADD CONSTRAINT data_pk PRIMARY KEY (id);
ALTER TABLE ONLY data_values
    ADD CONSTRAINT dava_bk_uk UNIQUE (data_id, saco_id);
ALTER TABLE ONLY data_values
    ADD CONSTRAINT dava_pk PRIMARY KEY (id);
ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_bk_uk UNIQUE ("location");
ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_pk PRIMARY KEY (data_id);
ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_bk_uk UNIQUE (code, proj_id);
ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_pk PRIMARY KEY (id);
ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_bk_uk UNIQUE (expe_id, file_name, version);
ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_pk PRIMARY KEY (id);
ALTER TABLE ONLY experiment_types
    ADD CONSTRAINT exty_bk_uk UNIQUE (code);
ALTER TABLE ONLY experiment_types
    ADD CONSTRAINT exty_pk PRIMARY KEY (id);
ALTER TABLE ONLY file_format_types
    ADD CONSTRAINT ffty_bk_uk UNIQUE (code);
ALTER TABLE ONLY file_format_types
    ADD CONSTRAINT ffty_pk PRIMARY KEY (id);
ALTER TABLE ONLY locator_types
    ADD CONSTRAINT loty_bk_uk UNIQUE (code);
ALTER TABLE ONLY locator_types
    ADD CONSTRAINT loty_pk PRIMARY KEY (id);
ALTER TABLE ONLY material_batches
    ADD CONSTRAINT maba_pk PRIMARY KEY (id);
ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_bk_uk UNIQUE (code);
ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_pk PRIMARY KEY (id);
ALTER TABLE ONLY material_types
    ADD CONSTRAINT maty_bk_uk UNIQUE (code);
ALTER TABLE ONLY material_types
    ADD CONSTRAINT maty_pk PRIMARY KEY (id);
ALTER TABLE ONLY material_types
    ADD CONSTRAINT maty_uk UNIQUE (code);
ALTER TABLE ONLY molecules
    ADD CONSTRAINT mole_pk PRIMARY KEY (mate_id);
ALTER TABLE ONLY observable_types
    ADD CONSTRAINT obty_bk_uk UNIQUE (code);
ALTER TABLE ONLY observable_types
    ADD CONSTRAINT obty_pk PRIMARY KEY (id);
ALTER TABLE ONLY organizations
    ADD CONSTRAINT orga_bk_uk UNIQUE (code);
ALTER TABLE ONLY organizations
    ADD CONSTRAINT orga_pk PRIMARY KEY (id);
ALTER TABLE ONLY procedure_types
    ADD CONSTRAINT pcty_bk_uk UNIQUE (code);
ALTER TABLE ONLY procedure_types
    ADD CONSTRAINT pcty_pk PRIMARY KEY (id);
ALTER TABLE ONLY persons
    ADD CONSTRAINT pers_bk_uk UNIQUE (user_id);
ALTER TABLE ONLY persons
    ADD CONSTRAINT pers_pk PRIMARY KEY (id);
ALTER TABLE ONLY procedures
    ADD CONSTRAINT proc_pk PRIMARY KEY (id);
ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_bk_uk UNIQUE (code, orga_id);
ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_pk PRIMARY KEY (id);
ALTER TABLE ONLY sample_components
    ADD CONSTRAINT saco_bk_uk UNIQUE (code, samp_id);
ALTER TABLE ONLY sample_components
    ADD CONSTRAINT saco_pk PRIMARY KEY (id);
ALTER TABLE ONLY sample_inputs
    ADD CONSTRAINT sain_bk_uk UNIQUE (samp_id, proc_id);
ALTER TABLE ONLY sample_inputs
    ADD CONSTRAINT sain_pk PRIMARY KEY (id);
ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_bk_uk UNIQUE (code);
ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_pk PRIMARY KEY (id);
ALTER TABLE ONLY sample_types
    ADD CONSTRAINT saty_bk_uk UNIQUE (code);
ALTER TABLE ONLY sample_types
    ADD CONSTRAINT saty_pk PRIMARY KEY (id);
ALTER TABLE ONLY sample_component_materials
    ADD CONSTRAINT scma_bk_uk UNIQUE (maba_id, saco_id);
ALTER TABLE ONLY sample_component_materials
    ADD CONSTRAINT scma_pk PRIMARY KEY (id);
CREATE TRIGGER br001_crtl_plate_chk
    BEFORE INSERT ON samples
    FOR EACH ROW
    EXECUTE PROCEDURE br001_crtl_plate_chk();
ALTER TABLE ONLY data
    ADD CONSTRAINT data_obty_fk FOREIGN KEY (obty_id) REFERENCES observable_types(id);
ALTER TABLE ONLY data
    ADD CONSTRAINT data_proc_fk FOREIGN KEY (proc_id_acquired_by) REFERENCES procedures(id);
ALTER TABLE ONLY data
    ADD CONSTRAINT data_samp_derived_from_fk FOREIGN KEY (samp_id_derived_from) REFERENCES samples(id);
ALTER TABLE ONLY data
    ADD CONSTRAINT data_samp_fk FOREIGN KEY (samp_id_acquired_from) REFERENCES samples(id);
ALTER TABLE ONLY data_values
    ADD CONSTRAINT dava_data_fk FOREIGN KEY (data_id) REFERENCES data(id);
ALTER TABLE ONLY data_values
    ADD CONSTRAINT dava_saco_fk FOREIGN KEY (saco_id) REFERENCES sample_components(id);
ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_data_fk FOREIGN KEY (data_id) REFERENCES data(id);
ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_ffty_fk FOREIGN KEY (ffty_id) REFERENCES file_format_types(id);
ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_loty_fk FOREIGN KEY (loty_id) REFERENCES locator_types(id);
ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_exty_fk FOREIGN KEY (exty_id) REFERENCES experiment_types(id);
ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_mate_fk FOREIGN KEY (mate_id_study_object) REFERENCES materials(id);
ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_proj_fk FOREIGN KEY (proj_id) REFERENCES projects(id);
ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments(id);
ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY material_batches
    ADD CONSTRAINT maba_mate_fk FOREIGN KEY (mate_id) REFERENCES materials(id);
ALTER TABLE ONLY material_batches
    ADD CONSTRAINT maba_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY material_batches
    ADD CONSTRAINT maba_proc_fk FOREIGN KEY (proc_id) REFERENCES procedures(id);
ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_maty_fk FOREIGN KEY (maty_id) REFERENCES material_types(id);
ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY molecules
    ADD CONSTRAINT mole_mate_fk FOREIGN KEY (mate_id) REFERENCES materials(id);
ALTER TABLE ONLY molecules
    ADD CONSTRAINT mole_mole_fk FOREIGN KEY (mole_id_inhibitor_of) REFERENCES molecules(mate_id);
ALTER TABLE ONLY procedures
    ADD CONSTRAINT proc_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments(id);
ALTER TABLE ONLY procedures
    ADD CONSTRAINT proc_pcty_fk FOREIGN KEY (pcty_id) REFERENCES procedure_types(id);
ALTER TABLE ONLY procedures
    ADD CONSTRAINT proc_pers_fk FOREIGN KEY (pers_id) REFERENCES persons(id);
ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_orga_fk FOREIGN KEY (orga_id) REFERENCES organizations(id);
ALTER TABLE ONLY sample_components
    ADD CONSTRAINT saco_samp_fk FOREIGN KEY (samp_id) REFERENCES samples(id);
ALTER TABLE ONLY sample_inputs
    ADD CONSTRAINT sain_proc_fk FOREIGN KEY (proc_id) REFERENCES procedures(id);
ALTER TABLE ONLY sample_inputs
    ADD CONSTRAINT sain_samp_fk FOREIGN KEY (samp_id) REFERENCES samples(id);
ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_proc_fk FOREIGN KEY (proc_id) REFERENCES procedures(id);
ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_samp_fk_generated_from FOREIGN KEY (samp_id_generated_from) REFERENCES samples(id);
ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_samp_fk_top FOREIGN KEY (samp_id_top) REFERENCES samples(id);
ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_saty_fk FOREIGN KEY (saty_id) REFERENCES sample_types(id);
ALTER TABLE ONLY sample_component_materials
    ADD CONSTRAINT scma_maba_fk FOREIGN KEY (maba_id) REFERENCES material_batches(id);
ALTER TABLE ONLY sample_component_materials
    ADD CONSTRAINT scma_saco_fk FOREIGN KEY (saco_id) REFERENCES sample_components(id);
REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;

