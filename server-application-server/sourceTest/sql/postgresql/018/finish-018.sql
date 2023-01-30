ALTER TABLE ONLY controlled_vocabularies
    ADD CONSTRAINT covo_bk_uk UNIQUE (code, is_internal_namespace);
ALTER TABLE ONLY controlled_vocabularies
    ADD CONSTRAINT covo_pk PRIMARY KEY (id);
ALTER TABLE ONLY controlled_vocabulary_terms
    ADD CONSTRAINT cvte_bk_uk UNIQUE (code, covo_id);
ALTER TABLE ONLY controlled_vocabulary_terms
    ADD CONSTRAINT cvte_pk PRIMARY KEY (id);
ALTER TABLE ONLY data
    ADD CONSTRAINT data_bk_uk UNIQUE (code);
ALTER TABLE ONLY data
    ADD CONSTRAINT data_pk PRIMARY KEY (id);
ALTER TABLE ONLY data_types
    ADD CONSTRAINT daty_bk_uk UNIQUE (code);
ALTER TABLE ONLY data_types
    ADD CONSTRAINT daty_pk PRIMARY KEY (id);
ALTER TABLE ONLY data_set_relationships
    ADD CONSTRAINT dsre_bk_uk UNIQUE (data_id_child, data_id_parent);
ALTER TABLE ONLY data_set_relationships
    ADD CONSTRAINT dsre_pk PRIMARY KEY (id);
ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_bk_uk UNIQUE (exty_id, prty_id);
ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_pk PRIMARY KEY (id);
ALTER TABLE ONLY experiment_attachments
    ADD CONSTRAINT exat_bk_uk UNIQUE (expe_id, file_name, version);
ALTER TABLE ONLY experiment_attachments
    ADD CONSTRAINT exat_pk PRIMARY KEY (id);
ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_bk_uk UNIQUE (location, loty_id);
ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_pk PRIMARY KEY (data_id);
ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_bk_uk UNIQUE (code, proj_id);
ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_pk PRIMARY KEY (id);
ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_bk_uk UNIQUE (expe_id, etpt_id);
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
ALTER TABLE ONLY groups
    ADD CONSTRAINT grou_bk_uk UNIQUE (code);
ALTER TABLE ONLY groups
    ADD CONSTRAINT grou_pk PRIMARY KEY (id);
ALTER TABLE ONLY invalidations
    ADD CONSTRAINT inva_pk PRIMARY KEY (id);
ALTER TABLE ONLY locator_types
    ADD CONSTRAINT loty_bk_uk UNIQUE (code);
ALTER TABLE ONLY locator_types
    ADD CONSTRAINT loty_pk PRIMARY KEY (id);
ALTER TABLE ONLY material_batches
    ADD CONSTRAINT maba_bk_uk UNIQUE (code, mate_id);
ALTER TABLE ONLY material_batches
    ADD CONSTRAINT maba_pk PRIMARY KEY (id);
ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_pk PRIMARY KEY (id);
ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_bk_uk UNIQUE (code, maty_id);
ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_pk PRIMARY KEY (id);
ALTER TABLE ONLY material_types
    ADD CONSTRAINT maty_bk_uk UNIQUE (code);
ALTER TABLE ONLY material_types
    ADD CONSTRAINT maty_pk PRIMARY KEY (id);
ALTER TABLE ONLY material_types
    ADD CONSTRAINT maty_uk UNIQUE (code);
ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_bk_uk UNIQUE (maty_id, prty_id);
ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_pk PRIMARY KEY (id);
ALTER TABLE ONLY observable_types
    ADD CONSTRAINT obty_bk_uk UNIQUE (code);
ALTER TABLE ONLY observable_types
    ADD CONSTRAINT obty_pk PRIMARY KEY (id);
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
    ADD CONSTRAINT proj_bk_uk UNIQUE (code, grou_id);
ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_pk PRIMARY KEY (id);
ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_bk_uk UNIQUE (code, is_internal_namespace);
ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_pk PRIMARY KEY (id);
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
ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_bk_uk UNIQUE (samp_id, stpt_id);
ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_pk PRIMARY KEY (id);
ALTER TABLE ONLY sample_types
    ADD CONSTRAINT saty_bk_uk UNIQUE (code);
ALTER TABLE ONLY sample_types
    ADD CONSTRAINT saty_pk PRIMARY KEY (id);
ALTER TABLE ONLY sample_component_materials
    ADD CONSTRAINT scma_bk_uk UNIQUE (maba_id, saco_id);
ALTER TABLE ONLY sample_component_materials
    ADD CONSTRAINT scma_pk PRIMARY KEY (id);
ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_bk_uk UNIQUE (saty_id, prty_id);
ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_pk PRIMARY KEY (id);
CREATE INDEX covo_pers_fk_i ON controlled_vocabularies USING btree (pers_id_registerer);
CREATE INDEX cvte_covo_fk_i ON controlled_vocabulary_terms USING btree (covo_id);
CREATE INDEX cvte_pers_fk_i ON controlled_vocabulary_terms USING btree (pers_id_registerer);
CREATE INDEX data_obty_fk_i ON data USING btree (obty_id);
CREATE INDEX data_proc_fk_i ON data USING btree (proc_id_produced_by);
CREATE INDEX data_samp_derived_from_fk_i ON data USING btree (samp_id_derived_from);
CREATE INDEX data_samp_fk_i ON data USING btree (samp_id_acquired_from);
CREATE INDEX dsre_data_fk_i_child ON data_set_relationships USING btree (data_id_child);
CREATE INDEX dsre_data_fk_i_parent ON data_set_relationships USING btree (data_id_parent);
CREATE INDEX etpt_exty_fk_i ON experiment_type_property_types USING btree (exty_id);
CREATE INDEX etpt_pers_fk_i ON experiment_type_property_types USING btree (pers_id_registerer);
CREATE INDEX etpt_prty_fk_i ON experiment_type_property_types USING btree (prty_id);
CREATE INDEX exat_expe_fk_i ON experiment_attachments USING btree (expe_id);
CREATE INDEX exat_pers_fk_i ON experiment_attachments USING btree (pers_id_registerer);
CREATE INDEX exda_ffty_fk_i ON external_data USING btree (ffty_id);
CREATE INDEX exda_loty_fk_i ON external_data USING btree (loty_id);
CREATE INDEX expe_exty_fk_i ON experiments USING btree (exty_id);
CREATE INDEX expe_inva_fk_i ON experiments USING btree (inva_id);
CREATE INDEX expe_mate_fk_i ON experiments USING btree (mate_id_study_object);
CREATE INDEX expe_pers_fk_i ON experiments USING btree (pers_id_registerer);
CREATE INDEX expe_proj_fk_i ON experiments USING btree (proj_id);
CREATE INDEX expr_cvte_fk_i ON experiment_properties USING btree (cvte_id);
CREATE INDEX expr_etpt_fk_i ON experiment_properties USING btree (etpt_id);
CREATE INDEX expr_expe_fk_i ON experiment_properties USING btree (expe_id);
CREATE INDEX expr_pers_fk_i ON experiment_properties USING btree (pers_id_registerer);
CREATE INDEX inva_pers_fk_i ON invalidations USING btree (pers_id_registerer);
CREATE INDEX maba_mate_fk_i ON material_batches USING btree (mate_id);
CREATE INDEX maba_pers_fk_i ON material_batches USING btree (pers_id_registerer);
CREATE INDEX maba_proc_fk_i ON material_batches USING btree (proc_id);
CREATE INDEX mapr_cvte_fk_i ON material_properties USING btree (cvte_id);
CREATE INDEX mapr_mate_fk_i ON material_properties USING btree (mate_id);
CREATE INDEX mapr_mtpt_fk_i ON material_properties USING btree (mtpt_id);
CREATE INDEX mapr_pers_fk_i ON material_properties USING btree (pers_id_registerer);
CREATE INDEX mate_mate_fk_i ON materials USING btree (mate_id_inhibitor_of);
CREATE INDEX mate_maty_fk_i ON materials USING btree (maty_id);
CREATE INDEX mate_pers_fk_i ON materials USING btree (pers_id_registerer);
CREATE INDEX mtpt_maty_fk_i ON material_type_property_types USING btree (maty_id);
CREATE INDEX mtpt_pers_fk_i ON material_type_property_types USING btree (pers_id_registerer);
CREATE INDEX mtpt_prty_fk_i ON material_type_property_types USING btree (prty_id);
CREATE INDEX proc_expe_fk_i ON procedures USING btree (expe_id);
CREATE INDEX proc_pcty_fk_i ON procedures USING btree (pcty_id);
CREATE INDEX proj_grou_fk_i ON projects USING btree (grou_id);
CREATE INDEX prty_covo_fk_i ON property_types USING btree (covo_id);
CREATE INDEX prty_daty_fk_i ON property_types USING btree (daty_id);
CREATE INDEX prty_pers_fk_i ON property_types USING btree (pers_id_registerer);
CREATE INDEX saco_samp_fk_i ON sample_components USING btree (samp_id);
CREATE INDEX sain_proc_fk_i ON sample_inputs USING btree (proc_id);
CREATE INDEX sain_samp_fk_i ON sample_inputs USING btree (samp_id);
CREATE INDEX samp_inva_fk_i ON samples USING btree (inva_id);
CREATE INDEX samp_pers_fk_i ON samples USING btree (pers_id_registerer);
CREATE INDEX samp_samp_fk_i_control_layout ON samples USING btree (samp_id_control_layout);
CREATE INDEX samp_samp_fk_i_generated_from ON samples USING btree (samp_id_generated_from);
CREATE INDEX samp_samp_fk_i_top ON samples USING btree (samp_id_top);
CREATE INDEX samp_saty_fk_i ON samples USING btree (saty_id);
CREATE INDEX sapr_cvte_fk_i ON sample_properties USING btree (cvte_id);
CREATE INDEX sapr_pers_fk_i ON sample_properties USING btree (pers_id_registerer);
CREATE INDEX sapr_samp_fk_i ON sample_properties USING btree (samp_id);
CREATE INDEX sapr_stpt_fk_i ON sample_properties USING btree (stpt_id);
CREATE INDEX scma_maba_fk_i ON sample_component_materials USING btree (maba_id);
CREATE INDEX scma_saco_fk_i ON sample_component_materials USING btree (saco_id);
CREATE INDEX stpt_pers_fk_i ON sample_type_property_types USING btree (pers_id_registerer);
CREATE INDEX stpt_prty_fk_i ON sample_type_property_types USING btree (prty_id);
CREATE INDEX stpt_saty_fk_i ON sample_type_property_types USING btree (saty_id);
CREATE TRIGGER controlled_vocabulary_check
    BEFORE INSERT OR UPDATE ON property_types
    FOR EACH ROW
    EXECUTE PROCEDURE controlled_vocabulary_check();
CREATE TRIGGER external_data_storage_format_check
    BEFORE INSERT OR UPDATE ON external_data
    FOR EACH ROW
    EXECUTE PROCEDURE external_data_storage_format_check();
ALTER TABLE ONLY controlled_vocabularies
    ADD CONSTRAINT covo_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY controlled_vocabulary_terms
    ADD CONSTRAINT cvte_covo_fk FOREIGN KEY (covo_id) REFERENCES controlled_vocabularies(id);
ALTER TABLE ONLY controlled_vocabulary_terms
    ADD CONSTRAINT cvte_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY data
    ADD CONSTRAINT data_obty_fk FOREIGN KEY (obty_id) REFERENCES observable_types(id);
ALTER TABLE ONLY data
    ADD CONSTRAINT data_proc_fk FOREIGN KEY (proc_id_produced_by) REFERENCES procedures(id);
ALTER TABLE ONLY data
    ADD CONSTRAINT data_samp_derived_from_fk FOREIGN KEY (samp_id_derived_from) REFERENCES samples(id);
ALTER TABLE ONLY data
    ADD CONSTRAINT data_samp_fk FOREIGN KEY (samp_id_acquired_from) REFERENCES samples(id);
ALTER TABLE ONLY data_set_relationships
    ADD CONSTRAINT dsre_data_fk_child FOREIGN KEY (data_id_child) REFERENCES data(id);
ALTER TABLE ONLY data_set_relationships
    ADD CONSTRAINT dsre_data_fk_parent FOREIGN KEY (data_id_parent) REFERENCES data(id);
ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_exty_fk FOREIGN KEY (exty_id) REFERENCES experiment_types(id);
ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_prty_fk FOREIGN KEY (prty_id) REFERENCES property_types(id);
ALTER TABLE ONLY experiment_attachments
    ADD CONSTRAINT exat_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments(id);
ALTER TABLE ONLY experiment_attachments
    ADD CONSTRAINT exat_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_data_fk FOREIGN KEY (data_id) REFERENCES data(id);
ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_ffty_fk FOREIGN KEY (ffty_id) REFERENCES file_format_types(id);
ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_loty_fk FOREIGN KEY (loty_id) REFERENCES locator_types(id);
ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_stor_fmt_fk FOREIGN KEY (cvte_id_stor_fmt) REFERENCES controlled_vocabulary_terms(id);
ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_exty_fk FOREIGN KEY (exty_id) REFERENCES experiment_types(id);
ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_inva_fk FOREIGN KEY (inva_id) REFERENCES invalidations(id);
ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_mate_fk FOREIGN KEY (mate_id_study_object) REFERENCES materials(id);
ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_proj_fk FOREIGN KEY (proj_id) REFERENCES projects(id);
ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_cvte_fk FOREIGN KEY (cvte_id) REFERENCES controlled_vocabulary_terms(id);
ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_etpt_fk FOREIGN KEY (etpt_id) REFERENCES experiment_type_property_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments(id);
ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY invalidations
    ADD CONSTRAINT inva_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY material_batches
    ADD CONSTRAINT maba_mate_fk FOREIGN KEY (mate_id) REFERENCES materials(id);
ALTER TABLE ONLY material_batches
    ADD CONSTRAINT maba_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY material_batches
    ADD CONSTRAINT maba_proc_fk FOREIGN KEY (proc_id) REFERENCES procedures(id);
ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_cvte_fk FOREIGN KEY (cvte_id) REFERENCES controlled_vocabulary_terms(id);
ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_mate_fk FOREIGN KEY (mate_id) REFERENCES materials(id);
ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_mtpt_fk FOREIGN KEY (mtpt_id) REFERENCES material_type_property_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_mate_fk FOREIGN KEY (mate_id_inhibitor_of) REFERENCES materials(id);
ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_maty_fk FOREIGN KEY (maty_id) REFERENCES material_types(id);
ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_maty_fk FOREIGN KEY (maty_id) REFERENCES material_types(id);
ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_prty_fk FOREIGN KEY (prty_id) REFERENCES property_types(id);
ALTER TABLE ONLY procedures
    ADD CONSTRAINT proc_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments(id);
ALTER TABLE ONLY procedures
    ADD CONSTRAINT proc_pcty_fk FOREIGN KEY (pcty_id) REFERENCES procedure_types(id);
ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_grou_fk FOREIGN KEY (grou_id) REFERENCES groups(id);
ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_covo_fk FOREIGN KEY (covo_id) REFERENCES controlled_vocabularies(id);
ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_daty_fk FOREIGN KEY (daty_id) REFERENCES data_types(id);
ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY sample_components
    ADD CONSTRAINT saco_samp_fk FOREIGN KEY (samp_id) REFERENCES samples(id);
ALTER TABLE ONLY sample_inputs
    ADD CONSTRAINT sain_proc_fk FOREIGN KEY (proc_id) REFERENCES procedures(id);
ALTER TABLE ONLY sample_inputs
    ADD CONSTRAINT sain_samp_fk FOREIGN KEY (samp_id) REFERENCES samples(id);
ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_inva_fk FOREIGN KEY (inva_id) REFERENCES invalidations(id);
ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_samp_fk_control_layout FOREIGN KEY (samp_id_control_layout) REFERENCES samples(id);
ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_samp_fk_generated_from FOREIGN KEY (samp_id_generated_from) REFERENCES samples(id);
ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_samp_fk_top FOREIGN KEY (samp_id_top) REFERENCES samples(id);
ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_saty_fk FOREIGN KEY (saty_id) REFERENCES sample_types(id);
ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_cvte_fk FOREIGN KEY (cvte_id) REFERENCES controlled_vocabulary_terms(id);
ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_samp_fk FOREIGN KEY (samp_id) REFERENCES samples(id);
ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_stpt_fk FOREIGN KEY (stpt_id) REFERENCES sample_type_property_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY sample_component_materials
    ADD CONSTRAINT scma_maba_fk FOREIGN KEY (maba_id) REFERENCES material_batches(id);
ALTER TABLE ONLY sample_component_materials
    ADD CONSTRAINT scma_saco_fk FOREIGN KEY (saco_id) REFERENCES sample_components(id);
ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_prty_fk FOREIGN KEY (prty_id) REFERENCES property_types(id);
ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_saty_fk FOREIGN KEY (saty_id) REFERENCES sample_types(id);
REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;

