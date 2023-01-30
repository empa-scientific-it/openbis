ALTER TABLE ONLY authorization_groups
    ADD CONSTRAINT ag_bk_uk UNIQUE (code, dbin_id);
ALTER TABLE ONLY authorization_groups
    ADD CONSTRAINT ag_pk PRIMARY KEY (id);
ALTER TABLE ONLY authorization_group_persons
    ADD CONSTRAINT agp_pk PRIMARY KEY (pers_id, ag_id);
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
ALTER TABLE ONLY data_store_service_data_set_types
    ADD CONSTRAINT dssdst_bk_uk UNIQUE (data_store_service_id, data_set_type_id);
ALTER TABLE ONLY data_store_services
    ADD CONSTRAINT dsse_bk_uk UNIQUE (key, data_store_id);
ALTER TABLE ONLY data_store_services
    ADD CONSTRAINT dsse_pk PRIMARY KEY (id);
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
ALTER TABLE ONLY filters
    ADD CONSTRAINT filt_bk_uk UNIQUE (name, dbin_id, grid_id);
ALTER TABLE ONLY filters
    ADD CONSTRAINT filt_pk PRIMARY KEY (id);
ALTER TABLE ONLY grid_custom_columns
    ADD CONSTRAINT grid_custom_columns_bk_uk UNIQUE (code, dbin_id, grid_id);
ALTER TABLE ONLY grid_custom_columns
    ADD CONSTRAINT grid_custom_columns_pk PRIMARY KEY (id);
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
CREATE INDEX dssdst_ds_fk_i ON data_store_service_data_set_types USING btree (data_store_service_id);
CREATE INDEX dssdst_dst_fk_i ON data_store_service_data_set_types USING btree (data_set_type_id);
CREATE INDEX dsse_ds_fk_i ON data_store_services USING btree (data_store_id);
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
CREATE INDEX filt_dbin_fk_i ON filters USING btree (dbin_id);
CREATE INDEX filt_pers_fk_i ON filters USING btree (pers_id_registerer);
CREATE INDEX grid_custom_columns_dbin_fk_i ON grid_custom_columns USING btree (dbin_id);
CREATE INDEX grid_custom_columns_pers_fk_i ON grid_custom_columns USING btree (pers_id_registerer);
CREATE INDEX grou_dbin_fk_i ON groups USING btree (dbin_id);
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
CREATE INDEX roas_ag_fk_i_grantee ON role_assignments USING btree (ag_id_grantee);
CREATE INDEX roas_dbin_fk_i ON role_assignments USING btree (dbin_id);
CREATE INDEX roas_grou_fk_i ON role_assignments USING btree (grou_id);
CREATE INDEX roas_pers_fk_i_grantee ON role_assignments USING btree (pers_id_grantee);
CREATE INDEX roas_pers_fk_i_registerer ON role_assignments USING btree (pers_id_registerer);
CREATE UNIQUE INDEX roas_role_group_dbi_ag_pers_uq_i ON role_assignments USING btree (role_code, (COALESCE((pers_id_grantee)::bigint, ((-1))::bigint)), (COALESCE((ag_id_grantee)::bigint, ((-1))::bigint)), (COALESCE((grou_id)::bigint, ((-1))::bigint)), (COALESCE((dbin_id)::bigint, ((-1))::bigint)));
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
CREATE CONSTRAINT TRIGGER check_dataset_relationships_on_data_table_modification
    AFTER INSERT OR UPDATE ON data
DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW
    EXECUTE PROCEDURE check_dataset_relationships_on_data_table_modification();
CREATE CONSTRAINT TRIGGER check_dataset_relationships_on_relationships_table_modification
    AFTER INSERT OR UPDATE ON data_set_relationships
DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW
    EXECUTE PROCEDURE check_dataset_relationships_on_relationships_table_modification();
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
ALTER TABLE ONLY authorization_groups
    ADD CONSTRAINT ag_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);
ALTER TABLE ONLY authorization_groups
    ADD CONSTRAINT ag_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY authorization_group_persons
    ADD CONSTRAINT agp_ag_fk FOREIGN KEY (ag_id) REFERENCES authorization_groups(id);
ALTER TABLE ONLY authorization_group_persons
    ADD CONSTRAINT agp_pers_fk FOREIGN KEY (pers_id) REFERENCES persons(id);
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
ALTER TABLE ONLY data_store_service_data_set_types
    ADD CONSTRAINT dssdst_ds_fk FOREIGN KEY (data_store_service_id) REFERENCES data_store_services(id) ON DELETE CASCADE;
ALTER TABLE ONLY data_store_service_data_set_types
    ADD CONSTRAINT dssdst_dst_fk FOREIGN KEY (data_set_type_id) REFERENCES data_set_types(id) ON DELETE CASCADE;
ALTER TABLE ONLY data_store_services
    ADD CONSTRAINT dsse_ds_fk FOREIGN KEY (data_store_id) REFERENCES data_stores(id) ON DELETE CASCADE;
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
ALTER TABLE ONLY filters
    ADD CONSTRAINT filt_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);
ALTER TABLE ONLY filters
    ADD CONSTRAINT filt_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY grid_custom_columns
    ADD CONSTRAINT grid_custom_columns_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);
ALTER TABLE ONLY grid_custom_columns
    ADD CONSTRAINT grid_custom_columns_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
ALTER TABLE ONLY groups
    ADD CONSTRAINT grou_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);
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
    ADD CONSTRAINT roas_ag_fk_grantee FOREIGN KEY (ag_id_grantee) REFERENCES authorization_groups(id);
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

