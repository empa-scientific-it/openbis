ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_expe_bk_uk UNIQUE (expe_id, file_name, version);
ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_pk PRIMARY KEY (id);
ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_proj_bk_uk UNIQUE (proj_id, file_name, version);
ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_samp_bk_uk UNIQUE (samp_id, file_name, version);
ALTER TABLE ONLY controlled_vocabularies
    ADD CONSTRAINT covo_bk_uk UNIQUE (code, is_internal_namespace, dbin_id);
ALTER TABLE ONLY controlled_vocabularies
    ADD CONSTRAINT covo_pk PRIMARY KEY (id);
ALTER TABLE ONLY controlled_vocabulary_terms
    ADD CONSTRAINT cvte_bk_uk UNIQUE (code, covo_id);
ALTER TABLE ONLY controlled_vocabulary_terms
    ADD CONSTRAINT cvte_pk PRIMARY KEY (id);
ALTER TABLE ONLY data_stores
    ADD CONSTRAINT dast_bk_uk UNIQUE (code, dbin_id);
ALTER TABLE ONLY data_stores
    ADD CONSTRAINT dast_pk PRIMARY KEY (id);
ALTER TABLE ONLY data
    ADD CONSTRAINT data_bk_uk UNIQUE (code);
ALTER TABLE ONLY data
    ADD CONSTRAINT data_pk PRIMARY KEY (id);
ALTER TABLE ONLY data_types
    ADD CONSTRAINT daty_bk_uk UNIQUE (code);
ALTER TABLE ONLY data_types
    ADD CONSTRAINT daty_pk PRIMARY KEY (id);
ALTER TABLE ONLY database_instances
    ADD CONSTRAINT dbin_bk_uk UNIQUE (code);
ALTER TABLE ONLY database_instances
    ADD CONSTRAINT dbin_pk PRIMARY KEY (id);
ALTER TABLE ONLY database_instances
    ADD CONSTRAINT dbin_uuid_uk UNIQUE (uuid);
ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_bk_uk UNIQUE (ds_id, dstpt_id);
ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_pk PRIMARY KEY (id);
ALTER TABLE ONLY data_set_relationships
    ADD CONSTRAINT dsre_bk_uk UNIQUE (data_id_child, data_id_parent);
ALTER TABLE ONLY data_set_type_property_types
    ADD CONSTRAINT dstpt_bk_uk UNIQUE (dsty_id, prty_id);
ALTER TABLE ONLY data_set_type_property_types
    ADD CONSTRAINT dstpt_pk PRIMARY KEY (id);
ALTER TABLE ONLY data_set_types
    ADD CONSTRAINT dsty_bk_uk UNIQUE (code, dbin_id);
ALTER TABLE ONLY data_set_types
    ADD CONSTRAINT dsty_pk PRIMARY KEY (id);
ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_bk_uk UNIQUE (exty_id, prty_id);
ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_pk PRIMARY KEY (id);
ALTER TABLE ONLY events
    ADD CONSTRAINT evnt_bk_uk UNIQUE (event_type, entity_type, identifier);
ALTER TABLE ONLY events
    ADD CONSTRAINT evnt_pk PRIMARY KEY (id);
ALTER TABLE ONLY attachment_contents
    ADD CONSTRAINT exac_pk PRIMARY KEY (id);
ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_bk_uk UNIQUE (location, loty_id);
ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_pk PRIMARY KEY (data_id);
ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_bk_uk UNIQUE (code, proj_id);
ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_pi_uk UNIQUE (perm_id);
ALTER TABLE ONLY experiments
    ADD CONSTRAINT expe_pk PRIMARY KEY (id);
ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_bk_uk UNIQUE (expe_id, etpt_id);
ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_pk PRIMARY KEY (id);
ALTER TABLE ONLY experiment_types
    ADD CONSTRAINT exty_bk_uk UNIQUE (code, dbin_id);
ALTER TABLE ONLY experiment_types
    ADD CONSTRAINT exty_pk PRIMARY KEY (id);
ALTER TABLE ONLY file_format_types
    ADD CONSTRAINT ffty_bk_uk UNIQUE (code, dbin_id);
ALTER TABLE ONLY file_format_types
    ADD CONSTRAINT ffty_pk PRIMARY KEY (id);
ALTER TABLE ONLY groups
    ADD CONSTRAINT grou_bk_uk UNIQUE (code, dbin_id);
ALTER TABLE ONLY groups
    ADD CONSTRAINT grou_pk PRIMARY KEY (id);
ALTER TABLE ONLY invalidations
    ADD CONSTRAINT inva_pk PRIMARY KEY (id);
ALTER TABLE ONLY locator_types
    ADD CONSTRAINT loty_bk_uk UNIQUE (code);
ALTER TABLE ONLY locator_types
    ADD CONSTRAINT loty_pk PRIMARY KEY (id);
ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_bk_uk UNIQUE (mate_id, mtpt_id);
ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_pk PRIMARY KEY (id);
ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_bk_uk UNIQUE (code, maty_id, dbin_id);
ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_pk PRIMARY KEY (id);
ALTER TABLE ONLY material_types
    ADD CONSTRAINT maty_bk_uk UNIQUE (code, dbin_id);
ALTER TABLE ONLY material_types
    ADD CONSTRAINT maty_pk PRIMARY KEY (id);
ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_bk_uk UNIQUE (maty_id, prty_id);
ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_pk PRIMARY KEY (id);
ALTER TABLE ONLY persons
    ADD CONSTRAINT pers_bk_uk UNIQUE (dbin_id, user_id);
ALTER TABLE ONLY persons
    ADD CONSTRAINT pers_pk PRIMARY KEY (id);
ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_bk_uk UNIQUE (code, grou_id);
ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_pk PRIMARY KEY (id);
ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_bk_uk UNIQUE (code, is_internal_namespace, dbin_id);
ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_pk PRIMARY KEY (id);
ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_group_bk_uk UNIQUE (pers_id_grantee, role_code, grou_id);
ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_instance_bk_uk UNIQUE (pers_id_grantee, role_code, dbin_id);
ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_pk PRIMARY KEY (id);
ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_pi_uk UNIQUE (perm_id);
ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_pk PRIMARY KEY (id);
ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_bk_uk UNIQUE (samp_id, stpt_id);
ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_pk PRIMARY KEY (id);
ALTER TABLE ONLY sample_types
    ADD CONSTRAINT saty_bk_uk UNIQUE (code, dbin_id);
ALTER TABLE ONLY sample_types
    ADD CONSTRAINT saty_pk PRIMARY KEY (id);
ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_bk_uk UNIQUE (saty_id, prty_id);
ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_pk PRIMARY KEY (id);
CREATE INDEX atta_exac_fk_i ON attachments USING btree (exac_id);
CREATE INDEX atta_expe_fk_i ON attachments USING btree (expe_id);
CREATE INDEX atta_pers_fk_i ON attachments USING btree (pers_id_registerer);
CREATE INDEX atta_proj_fk_i ON attachments USING btree (proj_id);
CREATE INDEX atta_samp_fk_i ON attachments USING btree (samp_id);
CREATE INDEX covo_pers_fk_i ON controlled_vocabularies USING btree (pers_id_registerer);
CREATE INDEX cvte_covo_fk_i ON controlled_vocabulary_terms USING btree (covo_id);
CREATE INDEX cvte_pers_fk_i ON controlled_vocabulary_terms USING btree (pers_id_registerer);
CREATE INDEX dast_dbin_fk_i ON data_stores USING btree (dbin_id);
CREATE INDEX data_dsty_fk_i ON data USING btree (dsty_id);
CREATE INDEX data_expe_fk_i ON data USING btree (expe_id);
CREATE INDEX data_samp_fk_i ON data USING btree (samp_id);
CREATE INDEX dspr_cvte_fk_i ON data_set_properties USING btree (cvte_id);
CREATE INDEX dspr_ds_fk_i ON data_set_properties USING btree (ds_id);
CREATE INDEX dspr_dstpt_fk_i ON data_set_properties USING btree (dstpt_id);
CREATE INDEX dspr_pers_fk_i ON data_set_properties USING btree (pers_id_registerer);
CREATE INDEX dsre_data_fk_i_child ON data_set_relationships USING btree (data_id_child);
CREATE INDEX dsre_data_fk_i_parent ON data_set_relationships USING btree (data_id_parent);
CREATE INDEX dstpt_dsty_fk_i ON data_set_type_property_types USING btree (dsty_id);
CREATE INDEX dstpt_pers_fk_i ON data_set_type_property_types USING btree (pers_id_registerer);
CREATE INDEX dstpt_prty_fk_i ON data_set_type_property_types USING btree (prty_id);
CREATE INDEX etpt_exty_fk_i ON experiment_type_property_types USING btree (exty_id);
CREATE INDEX etpt_pers_fk_i ON experiment_type_property_types USING btree (pers_id_registerer);
CREATE INDEX etpt_prty_fk_i ON experiment_type_property_types USING btree (prty_id);
CREATE INDEX evnt_pers_fk_i ON events USING btree (pers_id_registerer);
CREATE INDEX exda_cvte_fk_i ON external_data USING btree (cvte_id_stor_fmt);
CREATE INDEX exda_cvte_stored_on_fk_i ON external_data USING btree (cvte_id_store);
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
CREATE INDEX grou_dbin_fk_i ON groups USING btree (dbin_id);
CREATE INDEX grou_grou_fk_i ON groups USING btree (grou_id_parent);
CREATE INDEX grou_pers_fk_i ON groups USING btree (pers_id_leader);
CREATE INDEX grou_pers_registered_by_fk_i ON groups USING btree (pers_id_registerer);
CREATE INDEX inva_pers_fk_i ON invalidations USING btree (pers_id_registerer);
CREATE INDEX mapr_cvte_fk_i ON material_properties USING btree (cvte_id);
CREATE INDEX mapr_mate_fk_i ON material_properties USING btree (mate_id);
CREATE INDEX mapr_mtpt_fk_i ON material_properties USING btree (mtpt_id);
CREATE INDEX mapr_pers_fk_i ON material_properties USING btree (pers_id_registerer);
CREATE INDEX mate_maty_fk_i ON materials USING btree (maty_id);
CREATE INDEX mate_pers_fk_i ON materials USING btree (pers_id_registerer);
CREATE INDEX mtpt_maty_fk_i ON material_type_property_types USING btree (maty_id);
CREATE INDEX mtpt_pers_fk_i ON material_type_property_types USING btree (pers_id_registerer);
CREATE INDEX mtpt_prty_fk_i ON material_type_property_types USING btree (prty_id);
CREATE INDEX pers_grou_fk_i ON persons USING btree (grou_id);
CREATE INDEX proj_grou_fk_i ON projects USING btree (grou_id);
CREATE INDEX proj_pers_fk_i_leader ON projects USING btree (pers_id_leader);
CREATE INDEX proj_pers_fk_i_registerer ON projects USING btree (pers_id_registerer);
CREATE INDEX prty_covo_fk_i ON property_types USING btree (covo_id);
CREATE INDEX prty_daty_fk_i ON property_types USING btree (daty_id);
CREATE INDEX prty_pers_fk_i ON property_types USING btree (pers_id_registerer);
CREATE INDEX roas_dbin_fk_i ON role_assignments USING btree (dbin_id);
CREATE INDEX roas_grou_fk_i ON role_assignments USING btree (grou_id);
CREATE INDEX roas_pers_fk_i_grantee ON role_assignments USING btree (pers_id_grantee);
CREATE INDEX roas_pers_fk_i_registerer ON role_assignments USING btree (pers_id_registerer);
CREATE INDEX samp_code_i ON samples USING btree (code);
CREATE INDEX samp_expe_fk_i ON samples USING btree (expe_id);
CREATE INDEX samp_inva_fk_i ON samples USING btree (inva_id);
CREATE INDEX samp_pers_fk_i ON samples USING btree (pers_id_registerer);
CREATE INDEX samp_samp_fk_i_control_layout ON samples USING btree (samp_id_control_layout);
CREATE INDEX samp_samp_fk_i_generated_from ON samples USING btree (samp_id_generated_from);
CREATE INDEX samp_samp_fk_i_part_of ON samples USING btree (samp_id_part_of);
CREATE INDEX samp_samp_fk_i_top ON samples USING btree (samp_id_top);
CREATE INDEX samp_saty_fk_i ON samples USING btree (saty_id);
CREATE INDEX sapr_cvte_fk_i ON sample_properties USING btree (cvte_id);
CREATE INDEX sapr_pers_fk_i ON sample_properties USING btree (pers_id_registerer);
CREATE INDEX sapr_samp_fk_i ON sample_properties USING btree (samp_id);
CREATE INDEX sapr_stpt_fk_i ON sample_properties USING btree (stpt_id);
CREATE INDEX stpt_pers_fk_i ON sample_type_property_types USING btree (pers_id_registerer);
CREATE INDEX stpt_prty_fk_i ON sample_type_property_types USING btree (prty_id);
CREATE INDEX stpt_saty_fk_i ON sample_type_property_types USING btree (saty_id);
CREATE TRIGGER controlled_vocabulary_check
    BEFORE INSERT OR UPDATE ON property_types
    FOR EACH ROW
    EXECUTE PROCEDURE controlled_vocabulary_check();
CREATE TRIGGER data_set_property_with_material_data_type_check
    BEFORE INSERT OR UPDATE ON data_set_properties
    FOR EACH ROW
    EXECUTE PROCEDURE data_set_property_with_material_data_type_check();
CREATE TRIGGER experiment_property_with_material_data_type_check
    BEFORE INSERT OR UPDATE ON experiment_properties
    FOR EACH ROW
    EXECUTE PROCEDURE experiment_property_with_material_data_type_check();
CREATE TRIGGER external_data_storage_format_check
    BEFORE INSERT OR UPDATE ON external_data
    FOR EACH ROW
    EXECUTE PROCEDURE external_data_storage_format_check();
CREATE TRIGGER material_property_with_material_data_type_check
    BEFORE INSERT OR UPDATE ON material_properties
    FOR EACH ROW
    EXECUTE PROCEDURE material_property_with_material_data_type_check();
CREATE TRIGGER sample_code_uniqueness_check
    BEFORE INSERT OR UPDATE ON samples
    FOR EACH ROW
    EXECUTE PROCEDURE sample_code_uniqueness_check();
CREATE TRIGGER sample_property_with_material_data_type_check
    BEFORE INSERT OR UPDATE ON sample_properties
    FOR EACH ROW
    EXECUTE PROCEDURE sample_property_with_material_data_type_check();
ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_cont_fk FOREIGN KEY (exac_id) REFERENCES attachment_contents(id);
ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments(id);
ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_proj_fk FOREIGN KEY (proj_id) REFERENCES projects(id);
ALTER TABLE ONLY attachments
    ADD CONSTRAINT atta_samp_fk FOREIGN KEY (samp_id) REFERENCES samples(id);
ALTER TABLE ONLY controlled_vocabularies
    ADD CONSTRAINT covo_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);
ALTER TABLE ONLY controlled_vocabularies
    ADD CONSTRAINT covo_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY controlled_vocabulary_terms
    ADD CONSTRAINT cvte_covo_fk FOREIGN KEY (covo_id) REFERENCES controlled_vocabularies(id);
ALTER TABLE ONLY controlled_vocabulary_terms
    ADD CONSTRAINT cvte_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY data_stores
    ADD CONSTRAINT dast_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);
ALTER TABLE ONLY data
    ADD CONSTRAINT data_dast_fk FOREIGN KEY (dast_id) REFERENCES data_stores(id);
ALTER TABLE ONLY data
    ADD CONSTRAINT data_dsty_fk FOREIGN KEY (dsty_id) REFERENCES data_set_types(id);
ALTER TABLE ONLY data
    ADD CONSTRAINT data_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments(id);
ALTER TABLE ONLY data
    ADD CONSTRAINT data_samp_fk FOREIGN KEY (samp_id) REFERENCES samples(id);
ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_cvte_fk FOREIGN KEY (cvte_id) REFERENCES controlled_vocabulary_terms(id);
ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_ds_fk FOREIGN KEY (ds_id) REFERENCES data(id);
ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_dstpt_fk FOREIGN KEY (dstpt_id) REFERENCES data_set_type_property_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES materials(id);
ALTER TABLE ONLY data_set_properties
    ADD CONSTRAINT dspr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY data_set_relationships
    ADD CONSTRAINT dsre_data_fk_child FOREIGN KEY (data_id_child) REFERENCES data(id);
ALTER TABLE ONLY data_set_relationships
    ADD CONSTRAINT dsre_data_fk_parent FOREIGN KEY (data_id_parent) REFERENCES data(id);
ALTER TABLE ONLY data_set_type_property_types
    ADD CONSTRAINT dstpt_dsty_fk FOREIGN KEY (dsty_id) REFERENCES data_set_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY data_set_type_property_types
    ADD CONSTRAINT dstpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY data_set_type_property_types
    ADD CONSTRAINT dstpt_prty_fk FOREIGN KEY (prty_id) REFERENCES property_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY data_set_types
    ADD CONSTRAINT dsty_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);
ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_exty_fk FOREIGN KEY (exty_id) REFERENCES experiment_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY experiment_type_property_types
    ADD CONSTRAINT etpt_prty_fk FOREIGN KEY (prty_id) REFERENCES property_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY events
    ADD CONSTRAINT evnt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_cvte_fk FOREIGN KEY (cvte_id_stor_fmt) REFERENCES controlled_vocabulary_terms(id);
ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_cvte_stored_on_fk FOREIGN KEY (cvte_id_store) REFERENCES controlled_vocabulary_terms(id);
ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_data_fk FOREIGN KEY (data_id) REFERENCES data(id);
ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_ffty_fk FOREIGN KEY (ffty_id) REFERENCES file_format_types(id);
ALTER TABLE ONLY external_data
    ADD CONSTRAINT exda_loty_fk FOREIGN KEY (loty_id) REFERENCES locator_types(id);
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
    ADD CONSTRAINT expr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES materials(id);
ALTER TABLE ONLY experiment_properties
    ADD CONSTRAINT expr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY experiment_types
    ADD CONSTRAINT exty_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);
ALTER TABLE ONLY file_format_types
    ADD CONSTRAINT ffty_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);
ALTER TABLE ONLY groups
    ADD CONSTRAINT grou_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);
ALTER TABLE ONLY groups
    ADD CONSTRAINT grou_grou_fk FOREIGN KEY (grou_id_parent) REFERENCES groups(id);
ALTER TABLE ONLY groups
    ADD CONSTRAINT grou_pers_fk_leader FOREIGN KEY (pers_id_leader) REFERENCES persons(id);
ALTER TABLE ONLY groups
    ADD CONSTRAINT grou_pers_fk_registerer FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY invalidations
    ADD CONSTRAINT inva_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_cvte_fk FOREIGN KEY (cvte_id) REFERENCES controlled_vocabulary_terms(id);
ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES materials(id);
ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_mate_fk FOREIGN KEY (mate_id) REFERENCES materials(id);
ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_mtpt_fk FOREIGN KEY (mtpt_id) REFERENCES material_type_property_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY material_properties
    ADD CONSTRAINT mapr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);
ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_maty_fk FOREIGN KEY (maty_id) REFERENCES material_types(id);
ALTER TABLE ONLY materials
    ADD CONSTRAINT mate_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY material_types
    ADD CONSTRAINT maty_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);
ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_maty_fk FOREIGN KEY (maty_id) REFERENCES material_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY material_type_property_types
    ADD CONSTRAINT mtpt_prty_fk FOREIGN KEY (prty_id) REFERENCES property_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY persons
    ADD CONSTRAINT pers_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);
ALTER TABLE ONLY persons
    ADD CONSTRAINT pers_grou_fk FOREIGN KEY (grou_id) REFERENCES groups(id);
ALTER TABLE ONLY persons
    ADD CONSTRAINT pers_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_grou_fk FOREIGN KEY (grou_id) REFERENCES groups(id);
ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_pers_fk_leader FOREIGN KEY (pers_id_leader) REFERENCES persons(id);
ALTER TABLE ONLY projects
    ADD CONSTRAINT proj_pers_fk_registerer FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_covo_fk FOREIGN KEY (covo_id) REFERENCES controlled_vocabularies(id);
ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_daty_fk FOREIGN KEY (daty_id) REFERENCES data_types(id);
ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);
ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_maty_fk FOREIGN KEY (maty_prop_id) REFERENCES material_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY property_types
    ADD CONSTRAINT prty_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);
ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_grou_fk FOREIGN KEY (grou_id) REFERENCES groups(id);
ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_pers_fk_grantee FOREIGN KEY (pers_id_grantee) REFERENCES persons(id);
ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT roas_pers_fk_registerer FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);
ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments(id);
ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_grou_fk FOREIGN KEY (grou_id) REFERENCES groups(id);
ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_inva_fk FOREIGN KEY (inva_id) REFERENCES invalidations(id);
ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_samp_fk_control_layout FOREIGN KEY (samp_id_control_layout) REFERENCES samples(id);
ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_samp_fk_generated_from FOREIGN KEY (samp_id_generated_from) REFERENCES samples(id);
ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_samp_fk_part_of FOREIGN KEY (samp_id_part_of) REFERENCES samples(id);
ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_samp_fk_top FOREIGN KEY (samp_id_top) REFERENCES samples(id);
ALTER TABLE ONLY samples
    ADD CONSTRAINT samp_saty_fk FOREIGN KEY (saty_id) REFERENCES sample_types(id);
ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_cvte_fk FOREIGN KEY (cvte_id) REFERENCES controlled_vocabulary_terms(id);
ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES materials(id);
ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_samp_fk FOREIGN KEY (samp_id) REFERENCES samples(id);
ALTER TABLE ONLY sample_properties
    ADD CONSTRAINT sapr_stpt_fk FOREIGN KEY (stpt_id) REFERENCES sample_type_property_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY sample_types
    ADD CONSTRAINT saty_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);
ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_prty_fk FOREIGN KEY (prty_id) REFERENCES property_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY sample_type_property_types
    ADD CONSTRAINT stpt_saty_fk FOREIGN KEY (saty_id) REFERENCES sample_types(id) ON DELETE CASCADE;
REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;
REVOKE ALL ON SEQUENCE attachment_content_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE attachment_content_id_seq FROM postgres;
GRANT ALL ON SEQUENCE attachment_content_id_seq TO buczekp;
GRANT ALL ON SEQUENCE attachment_content_id_seq TO postgres;
GRANT SELECT ON SEQUENCE attachment_content_id_seq TO openbis_readonly;
REVOKE ALL ON TABLE attachment_contents FROM PUBLIC;
REVOKE ALL ON TABLE attachment_contents FROM buczekp;
GRANT ALL ON TABLE attachment_contents TO buczekp;
GRANT ALL ON TABLE attachment_contents TO postgres;
GRANT SELECT ON TABLE attachment_contents TO openbis_readonly;
REVOKE ALL ON SEQUENCE attachment_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE attachment_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE attachment_id_seq TO buczekp;
GRANT ALL ON SEQUENCE attachment_id_seq TO postgres;
GRANT SELECT ON SEQUENCE attachment_id_seq TO openbis_readonly;
REVOKE ALL ON TABLE attachments FROM PUBLIC;
REVOKE ALL ON TABLE attachments FROM buczekp;
GRANT ALL ON TABLE attachments TO buczekp;
GRANT ALL ON TABLE attachments TO postgres;
GRANT SELECT ON TABLE attachments TO openbis_readonly;
REVOKE ALL ON SEQUENCE code_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE code_seq FROM buczekp;
GRANT ALL ON SEQUENCE code_seq TO buczekp;
GRANT ALL ON SEQUENCE code_seq TO postgres;
GRANT SELECT ON SEQUENCE code_seq TO openbis_readonly;
REVOKE ALL ON TABLE controlled_vocabularies FROM PUBLIC;
REVOKE ALL ON TABLE controlled_vocabularies FROM buczekp;
GRANT ALL ON TABLE controlled_vocabularies TO buczekp;
GRANT ALL ON TABLE controlled_vocabularies TO postgres;
GRANT SELECT ON TABLE controlled_vocabularies TO openbis_readonly;
REVOKE ALL ON SEQUENCE controlled_vocabulary_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE controlled_vocabulary_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE controlled_vocabulary_id_seq TO buczekp;
GRANT ALL ON SEQUENCE controlled_vocabulary_id_seq TO postgres;
GRANT SELECT ON SEQUENCE controlled_vocabulary_id_seq TO openbis_readonly;
REVOKE ALL ON TABLE controlled_vocabulary_terms FROM PUBLIC;
REVOKE ALL ON TABLE controlled_vocabulary_terms FROM buczekp;
GRANT ALL ON TABLE controlled_vocabulary_terms TO buczekp;
GRANT ALL ON TABLE controlled_vocabulary_terms TO postgres;
GRANT SELECT ON TABLE controlled_vocabulary_terms TO openbis_readonly;
REVOKE ALL ON SEQUENCE cvte_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE cvte_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE cvte_id_seq TO buczekp;
GRANT ALL ON SEQUENCE cvte_id_seq TO postgres;
GRANT SELECT ON SEQUENCE cvte_id_seq TO openbis_readonly;
REVOKE ALL ON TABLE data FROM PUBLIC;
REVOKE ALL ON TABLE data FROM buczekp;
GRANT ALL ON TABLE data TO buczekp;
GRANT ALL ON TABLE data TO postgres;
GRANT SELECT ON TABLE data TO openbis_readonly;
REVOKE ALL ON SEQUENCE data_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE data_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE data_id_seq TO buczekp;
GRANT ALL ON SEQUENCE data_id_seq TO postgres;
GRANT SELECT ON SEQUENCE data_id_seq TO openbis_readonly;
REVOKE ALL ON TABLE data_set_properties FROM PUBLIC;
REVOKE ALL ON TABLE data_set_properties FROM buczekp;
GRANT ALL ON TABLE data_set_properties TO buczekp;
GRANT ALL ON TABLE data_set_properties TO postgres;
GRANT SELECT ON TABLE data_set_properties TO openbis_readonly;
REVOKE ALL ON SEQUENCE data_set_property_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE data_set_property_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE data_set_property_id_seq TO buczekp;
GRANT ALL ON SEQUENCE data_set_property_id_seq TO postgres;
GRANT SELECT ON SEQUENCE data_set_property_id_seq TO openbis_readonly;
REVOKE ALL ON SEQUENCE data_set_relationship_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE data_set_relationship_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE data_set_relationship_id_seq TO buczekp;
GRANT ALL ON SEQUENCE data_set_relationship_id_seq TO postgres;
GRANT SELECT ON SEQUENCE data_set_relationship_id_seq TO openbis_readonly;
REVOKE ALL ON TABLE data_set_relationships FROM PUBLIC;
REVOKE ALL ON TABLE data_set_relationships FROM buczekp;
GRANT ALL ON TABLE data_set_relationships TO buczekp;
GRANT ALL ON TABLE data_set_relationships TO postgres;
GRANT SELECT ON TABLE data_set_relationships TO openbis_readonly;
REVOKE ALL ON SEQUENCE data_set_type_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE data_set_type_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE data_set_type_id_seq TO buczekp;
GRANT ALL ON SEQUENCE data_set_type_id_seq TO postgres;
GRANT SELECT ON SEQUENCE data_set_type_id_seq TO openbis_readonly;
REVOKE ALL ON TABLE data_set_type_property_types FROM PUBLIC;
REVOKE ALL ON TABLE data_set_type_property_types FROM buczekp;
GRANT ALL ON TABLE data_set_type_property_types TO buczekp;
GRANT ALL ON TABLE data_set_type_property_types TO postgres;
GRANT SELECT ON TABLE data_set_type_property_types TO openbis_readonly;
REVOKE ALL ON TABLE data_set_types FROM PUBLIC;
REVOKE ALL ON TABLE data_set_types FROM buczekp;
GRANT ALL ON TABLE data_set_types TO buczekp;
GRANT ALL ON TABLE data_set_types TO postgres;
GRANT SELECT ON TABLE data_set_types TO openbis_readonly;
REVOKE ALL ON SEQUENCE data_store_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE data_store_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE data_store_id_seq TO buczekp;
GRANT ALL ON SEQUENCE data_store_id_seq TO postgres;
GRANT SELECT ON SEQUENCE data_store_id_seq TO openbis_readonly;
REVOKE ALL ON TABLE data_stores FROM PUBLIC;
REVOKE ALL ON TABLE data_stores FROM buczekp;
GRANT ALL ON TABLE data_stores TO buczekp;
GRANT ALL ON TABLE data_stores TO postgres;
GRANT SELECT ON TABLE data_stores TO openbis_readonly;
REVOKE ALL ON SEQUENCE data_type_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE data_type_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE data_type_id_seq TO buczekp;
GRANT ALL ON SEQUENCE data_type_id_seq TO postgres;
GRANT SELECT ON SEQUENCE data_type_id_seq TO openbis_readonly;
REVOKE ALL ON TABLE data_types FROM PUBLIC;
REVOKE ALL ON TABLE data_types FROM buczekp;
GRANT ALL ON TABLE data_types TO buczekp;
GRANT ALL ON TABLE data_types TO postgres;
GRANT SELECT ON TABLE data_types TO openbis_readonly;
REVOKE ALL ON SEQUENCE database_instance_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE database_instance_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE database_instance_id_seq TO buczekp;
GRANT ALL ON SEQUENCE database_instance_id_seq TO postgres;
GRANT SELECT ON SEQUENCE database_instance_id_seq TO openbis_readonly;
REVOKE ALL ON TABLE database_instances FROM PUBLIC;
REVOKE ALL ON TABLE database_instances FROM buczekp;
GRANT ALL ON TABLE database_instances TO buczekp;
GRANT ALL ON TABLE database_instances TO postgres;
GRANT SELECT ON TABLE database_instances TO openbis_readonly;
REVOKE ALL ON TABLE database_version_logs FROM PUBLIC;
REVOKE ALL ON TABLE database_version_logs FROM buczekp;
GRANT ALL ON TABLE database_version_logs TO buczekp;
GRANT ALL ON TABLE database_version_logs TO postgres;
GRANT SELECT ON TABLE database_version_logs TO openbis_readonly;
REVOKE ALL ON SEQUENCE dstpt_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE dstpt_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE dstpt_id_seq TO buczekp;
GRANT ALL ON SEQUENCE dstpt_id_seq TO postgres;
GRANT SELECT ON SEQUENCE dstpt_id_seq TO openbis_readonly;
REVOKE ALL ON SEQUENCE etpt_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE etpt_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE etpt_id_seq TO buczekp;
GRANT ALL ON SEQUENCE etpt_id_seq TO postgres;
GRANT SELECT ON SEQUENCE etpt_id_seq TO openbis_readonly;
REVOKE ALL ON SEQUENCE event_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE event_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE event_id_seq TO buczekp;
GRANT ALL ON SEQUENCE event_id_seq TO postgres;
GRANT SELECT ON SEQUENCE event_id_seq TO openbis_readonly;
REVOKE ALL ON TABLE events FROM PUBLIC;
REVOKE ALL ON TABLE events FROM buczekp;
GRANT ALL ON TABLE events TO buczekp;
GRANT ALL ON TABLE events TO postgres;
GRANT SELECT ON TABLE events TO openbis_readonly;
REVOKE ALL ON SEQUENCE experiment_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE experiment_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE experiment_id_seq TO buczekp;
GRANT ALL ON SEQUENCE experiment_id_seq TO postgres;
GRANT SELECT ON SEQUENCE experiment_id_seq TO openbis_readonly;
REVOKE ALL ON TABLE experiment_properties FROM PUBLIC;
REVOKE ALL ON TABLE experiment_properties FROM buczekp;
GRANT ALL ON TABLE experiment_properties TO buczekp;
GRANT ALL ON TABLE experiment_properties TO postgres;
GRANT SELECT ON TABLE experiment_properties TO openbis_readonly;
REVOKE ALL ON SEQUENCE experiment_property_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE experiment_property_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE experiment_property_id_seq TO buczekp;
GRANT ALL ON SEQUENCE experiment_property_id_seq TO postgres;
GRANT SELECT ON SEQUENCE experiment_property_id_seq TO openbis_readonly;
REVOKE ALL ON SEQUENCE experiment_type_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE experiment_type_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE experiment_type_id_seq TO buczekp;
GRANT ALL ON SEQUENCE experiment_type_id_seq TO postgres;
GRANT SELECT ON SEQUENCE experiment_type_id_seq TO openbis_readonly;
REVOKE ALL ON TABLE experiment_type_property_types FROM PUBLIC;
REVOKE ALL ON TABLE experiment_type_property_types FROM buczekp;
GRANT ALL ON TABLE experiment_type_property_types TO buczekp;
GRANT ALL ON TABLE experiment_type_property_types TO postgres;
GRANT SELECT ON TABLE experiment_type_property_types TO openbis_readonly;
REVOKE ALL ON TABLE experiment_types FROM PUBLIC;
REVOKE ALL ON TABLE experiment_types FROM buczekp;
GRANT ALL ON TABLE experiment_types TO buczekp;
GRANT ALL ON TABLE experiment_types TO postgres;
GRANT SELECT ON TABLE experiment_types TO openbis_readonly;
REVOKE ALL ON TABLE experiments FROM PUBLIC;
REVOKE ALL ON TABLE experiments FROM buczekp;
GRANT ALL ON TABLE experiments TO buczekp;
GRANT ALL ON TABLE experiments TO postgres;
GRANT SELECT ON TABLE experiments TO openbis_readonly;
REVOKE ALL ON TABLE external_data FROM PUBLIC;
REVOKE ALL ON TABLE external_data FROM buczekp;
GRANT ALL ON TABLE external_data TO buczekp;
GRANT ALL ON TABLE external_data TO postgres;
GRANT SELECT ON TABLE external_data TO openbis_readonly;
REVOKE ALL ON SEQUENCE file_format_type_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE file_format_type_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE file_format_type_id_seq TO buczekp;
GRANT ALL ON SEQUENCE file_format_type_id_seq TO postgres;
GRANT SELECT ON SEQUENCE file_format_type_id_seq TO openbis_readonly;
REVOKE ALL ON TABLE file_format_types FROM PUBLIC;
REVOKE ALL ON TABLE file_format_types FROM buczekp;
GRANT ALL ON TABLE file_format_types TO buczekp;
GRANT ALL ON TABLE file_format_types TO postgres;
GRANT SELECT ON TABLE file_format_types TO openbis_readonly;
REVOKE ALL ON SEQUENCE group_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE group_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE group_id_seq TO buczekp;
GRANT ALL ON SEQUENCE group_id_seq TO postgres;
GRANT SELECT ON SEQUENCE group_id_seq TO openbis_readonly;
REVOKE ALL ON TABLE groups FROM PUBLIC;
REVOKE ALL ON TABLE groups FROM buczekp;
GRANT ALL ON TABLE groups TO buczekp;
GRANT ALL ON TABLE groups TO postgres;
GRANT SELECT ON TABLE groups TO openbis_readonly;
REVOKE ALL ON SEQUENCE invalidation_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE invalidation_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE invalidation_id_seq TO buczekp;
GRANT ALL ON SEQUENCE invalidation_id_seq TO postgres;
GRANT SELECT ON SEQUENCE invalidation_id_seq TO openbis_readonly;
REVOKE ALL ON TABLE invalidations FROM PUBLIC;
REVOKE ALL ON TABLE invalidations FROM buczekp;
GRANT ALL ON TABLE invalidations TO buczekp;
GRANT ALL ON TABLE invalidations TO postgres;
GRANT SELECT ON TABLE invalidations TO openbis_readonly;
REVOKE ALL ON SEQUENCE locator_type_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE locator_type_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE locator_type_id_seq TO buczekp;
GRANT ALL ON SEQUENCE locator_type_id_seq TO postgres;
GRANT SELECT ON SEQUENCE locator_type_id_seq TO openbis_readonly;
REVOKE ALL ON TABLE locator_types FROM PUBLIC;
REVOKE ALL ON TABLE locator_types FROM buczekp;
GRANT ALL ON TABLE locator_types TO buczekp;
GRANT ALL ON TABLE locator_types TO postgres;
GRANT SELECT ON TABLE locator_types TO openbis_readonly;
REVOKE ALL ON SEQUENCE material_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE material_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE material_id_seq TO buczekp;
GRANT ALL ON SEQUENCE material_id_seq TO postgres;
GRANT SELECT ON SEQUENCE material_id_seq TO openbis_readonly;
REVOKE ALL ON TABLE material_properties FROM PUBLIC;
REVOKE ALL ON TABLE material_properties FROM buczekp;
GRANT ALL ON TABLE material_properties TO buczekp;
GRANT ALL ON TABLE material_properties TO postgres;
GRANT SELECT ON TABLE material_properties TO openbis_readonly;
REVOKE ALL ON SEQUENCE material_property_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE material_property_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE material_property_id_seq TO buczekp;
GRANT ALL ON SEQUENCE material_property_id_seq TO postgres;
GRANT SELECT ON SEQUENCE material_property_id_seq TO openbis_readonly;
REVOKE ALL ON SEQUENCE material_type_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE material_type_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE material_type_id_seq TO buczekp;
GRANT ALL ON SEQUENCE material_type_id_seq TO postgres;
GRANT SELECT ON SEQUENCE material_type_id_seq TO openbis_readonly;
REVOKE ALL ON TABLE material_type_property_types FROM PUBLIC;
REVOKE ALL ON TABLE material_type_property_types FROM buczekp;
GRANT ALL ON TABLE material_type_property_types TO buczekp;
GRANT ALL ON TABLE material_type_property_types TO postgres;
GRANT SELECT ON TABLE material_type_property_types TO openbis_readonly;
REVOKE ALL ON TABLE material_types FROM PUBLIC;
REVOKE ALL ON TABLE material_types FROM buczekp;
GRANT ALL ON TABLE material_types TO buczekp;
GRANT ALL ON TABLE material_types TO postgres;
GRANT SELECT ON TABLE material_types TO openbis_readonly;
REVOKE ALL ON TABLE materials FROM PUBLIC;
REVOKE ALL ON TABLE materials FROM buczekp;
GRANT ALL ON TABLE materials TO buczekp;
GRANT ALL ON TABLE materials TO postgres;
GRANT SELECT ON TABLE materials TO openbis_readonly;
REVOKE ALL ON SEQUENCE mtpt_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE mtpt_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE mtpt_id_seq TO buczekp;
GRANT ALL ON SEQUENCE mtpt_id_seq TO postgres;
GRANT SELECT ON SEQUENCE mtpt_id_seq TO openbis_readonly;
REVOKE ALL ON SEQUENCE perm_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE perm_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE perm_id_seq TO buczekp;
GRANT ALL ON SEQUENCE perm_id_seq TO postgres;
GRANT SELECT ON SEQUENCE perm_id_seq TO openbis_readonly;
REVOKE ALL ON SEQUENCE person_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE person_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE person_id_seq TO buczekp;
GRANT ALL ON SEQUENCE person_id_seq TO postgres;
GRANT SELECT ON SEQUENCE person_id_seq TO openbis_readonly;
REVOKE ALL ON TABLE persons FROM PUBLIC;
REVOKE ALL ON TABLE persons FROM buczekp;
GRANT ALL ON TABLE persons TO buczekp;
GRANT ALL ON TABLE persons TO postgres;
GRANT SELECT ON TABLE persons TO openbis_readonly;
REVOKE ALL ON SEQUENCE project_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE project_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE project_id_seq TO buczekp;
GRANT ALL ON SEQUENCE project_id_seq TO postgres;
GRANT SELECT ON SEQUENCE project_id_seq TO openbis_readonly;
REVOKE ALL ON TABLE projects FROM PUBLIC;
REVOKE ALL ON TABLE projects FROM buczekp;
GRANT ALL ON TABLE projects TO buczekp;
GRANT ALL ON TABLE projects TO postgres;
GRANT SELECT ON TABLE projects TO openbis_readonly;
REVOKE ALL ON SEQUENCE property_type_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE property_type_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE property_type_id_seq TO buczekp;
GRANT ALL ON SEQUENCE property_type_id_seq TO postgres;
GRANT SELECT ON SEQUENCE property_type_id_seq TO openbis_readonly;
REVOKE ALL ON TABLE property_types FROM PUBLIC;
REVOKE ALL ON TABLE property_types FROM buczekp;
GRANT ALL ON TABLE property_types TO buczekp;
GRANT ALL ON TABLE property_types TO postgres;
GRANT SELECT ON TABLE property_types TO openbis_readonly;
REVOKE ALL ON SEQUENCE role_assignment_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE role_assignment_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE role_assignment_id_seq TO buczekp;
GRANT ALL ON SEQUENCE role_assignment_id_seq TO postgres;
GRANT SELECT ON SEQUENCE role_assignment_id_seq TO openbis_readonly;
REVOKE ALL ON TABLE role_assignments FROM PUBLIC;
REVOKE ALL ON TABLE role_assignments FROM buczekp;
GRANT ALL ON TABLE role_assignments TO buczekp;
GRANT ALL ON TABLE role_assignments TO postgres;
GRANT SELECT ON TABLE role_assignments TO openbis_readonly;
REVOKE ALL ON SEQUENCE sample_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE sample_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE sample_id_seq TO buczekp;
GRANT ALL ON SEQUENCE sample_id_seq TO postgres;
GRANT SELECT ON SEQUENCE sample_id_seq TO openbis_readonly;
REVOKE ALL ON TABLE sample_properties FROM PUBLIC;
REVOKE ALL ON TABLE sample_properties FROM buczekp;
GRANT ALL ON TABLE sample_properties TO buczekp;
GRANT ALL ON TABLE sample_properties TO postgres;
GRANT SELECT ON TABLE sample_properties TO openbis_readonly;
REVOKE ALL ON SEQUENCE sample_property_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE sample_property_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE sample_property_id_seq TO buczekp;
GRANT ALL ON SEQUENCE sample_property_id_seq TO postgres;
GRANT SELECT ON SEQUENCE sample_property_id_seq TO openbis_readonly;
REVOKE ALL ON SEQUENCE sample_type_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE sample_type_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE sample_type_id_seq TO buczekp;
GRANT ALL ON SEQUENCE sample_type_id_seq TO postgres;
GRANT SELECT ON SEQUENCE sample_type_id_seq TO openbis_readonly;
REVOKE ALL ON TABLE sample_type_property_types FROM PUBLIC;
REVOKE ALL ON TABLE sample_type_property_types FROM buczekp;
GRANT ALL ON TABLE sample_type_property_types TO buczekp;
GRANT ALL ON TABLE sample_type_property_types TO postgres;
GRANT SELECT ON TABLE sample_type_property_types TO openbis_readonly;
REVOKE ALL ON TABLE sample_types FROM PUBLIC;
REVOKE ALL ON TABLE sample_types FROM buczekp;
GRANT ALL ON TABLE sample_types TO buczekp;
GRANT ALL ON TABLE sample_types TO postgres;
GRANT SELECT ON TABLE sample_types TO openbis_readonly;
REVOKE ALL ON TABLE samples FROM PUBLIC;
REVOKE ALL ON TABLE samples FROM buczekp;
GRANT ALL ON TABLE samples TO buczekp;
GRANT ALL ON TABLE samples TO postgres;
GRANT SELECT ON TABLE samples TO openbis_readonly;
REVOKE ALL ON SEQUENCE stpt_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE stpt_id_seq FROM buczekp;
GRANT ALL ON SEQUENCE stpt_id_seq TO buczekp;
GRANT ALL ON SEQUENCE stpt_id_seq TO postgres;
GRANT SELECT ON SEQUENCE stpt_id_seq TO openbis_readonly;

