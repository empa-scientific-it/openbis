-- Migration from 191 to 192

-- schema

ALTER TABLE IF EXISTS SAMPLES_ALL
    ADD COLUMN META_DATA jsonb;

ALTER TABLE IF EXISTS SAMPLE_TYPES
    ADD COLUMN META_DATA jsonb;

CREATE OR REPLACE VIEW samples AS
     SELECT id, perm_id, code, proj_id, proj_frozen, expe_id, expe_frozen, saty_id, registration_timestamp,
            modification_timestamp, pers_id_registerer, pers_id_modifier, del_id, orig_del, space_id, space_frozen,
            samp_id_part_of, cont_frozen, version, frozen, frozen_for_comp, frozen_for_children, frozen_for_parents, frozen_for_data, tsvector_document, sample_identifier
       FROM samples_all
     WHERE del_id IS NULL;


ALTER TABLE IF EXISTS EXPERIMENTS_ALL
    ADD COLUMN META_DATA jsonb;

ALTER TABLE IF EXISTS EXPERIMENT_TYPES
    ADD COLUMN META_DATA jsonb;

CREATE OR REPLACE VIEW experiments AS
     SELECT id, perm_id, code, exty_id, pers_id_registerer, pers_id_modifier, registration_timestamp, modification_timestamp,
            proj_id, proj_frozen, del_id, orig_del, is_public, version, frozen, frozen_for_samp, frozen_for_data, tsvector_document, meta_data
       FROM experiments_all
      WHERE del_id IS NULL;


ALTER TABLE IF EXISTS DATA_ALL
    ADD COLUMN META_DATA jsonb;

ALTER TABLE IF EXISTS DATA_SET_TYPES
    ADD COLUMN META_DATA jsonb;

CREATE OR REPLACE VIEW data AS
   SELECT id, code, dsty_id, dast_id, expe_id, expe_frozen, data_producer_code, production_timestamp, samp_id, samp_frozen,
          registration_timestamp, access_timestamp, pers_id_registerer, pers_id_modifier, is_valid, modification_timestamp,
          is_derived, del_id, orig_del, version, data_set_kind,
          frozen, frozen_for_children, frozen_for_parents, frozen_for_comps, frozen_for_conts, tsvector_document, meta_data
     FROM data_all
    WHERE del_id IS NULL;


-- function

CREATE OR REPLACE RULE sample_insert AS
    ON INSERT TO samples DO INSTEAD
       INSERT INTO samples_all (
         id,
         frozen,
         frozen_for_comp,
         frozen_for_children,
         frozen_for_parents,
         frozen_for_data,
         code,
         del_id,
         orig_del,
         expe_id,
         expe_frozen,
         proj_id,
         proj_frozen,
         modification_timestamp,
         perm_id,
         pers_id_registerer,
         pers_id_modifier,
         registration_timestamp,
         samp_id_part_of,
         cont_frozen,
         saty_id,
         space_id,
         space_frozen,
         version,
         meta_data
       ) VALUES (
         NEW.id,
         NEW.frozen,
         NEW.frozen_for_comp,
         NEW.frozen_for_children,
         NEW.frozen_for_parents,
         NEW.frozen_for_data,
         NEW.code,
         NEW.del_id,
         NEW.orig_del,
         NEW.expe_id,
         NEW.expe_frozen,
         NEW.proj_id,
         NEW.proj_frozen,
         NEW.modification_timestamp,
         NEW.perm_id,
         NEW.pers_id_registerer,
         NEW.pers_id_modifier,
         NEW.registration_timestamp,
         NEW.samp_id_part_of,
         NEW.cont_frozen,
         NEW.saty_id,
         NEW.space_id,
         NEW.space_frozen,
         NEW.version,
         NEW.meta_data
       );

CREATE OR REPLACE RULE sample_update AS
    ON UPDATE TO samples DO INSTEAD
       UPDATE samples_all
          SET code = NEW.code,
              frozen = NEW.frozen,
              frozen_for_comp = NEW.frozen_for_comp,
              frozen_for_children = NEW.frozen_for_children,
              frozen_for_parents = NEW.frozen_for_parents,
              frozen_for_data = NEW.frozen_for_data,
              del_id = NEW.del_id,
              orig_del = NEW.orig_del,
              expe_id = NEW.expe_id,
              expe_frozen = NEW.expe_frozen,
              proj_id = NEW.proj_id,
              proj_frozen = NEW.proj_frozen,
              modification_timestamp = NEW.modification_timestamp,
              perm_id = NEW.perm_id,
              pers_id_registerer = NEW.pers_id_registerer,
              pers_id_modifier = NEW.pers_id_modifier,
              registration_timestamp = NEW.registration_timestamp,
              samp_id_part_of = NEW.samp_id_part_of,
              cont_frozen = NEW.cont_frozen,
              saty_id = NEW.saty_id,
              space_id = NEW.space_id,
              space_frozen = NEW.space_frozen,
              version = NEW.version,
              meta_data = NEW.meta_data
          WHERE id = NEW.id;


CREATE OR REPLACE RULE experiment_insert AS
  ON INSERT TO experiments DO INSTEAD
     INSERT INTO experiments_all (
       id,
       frozen,
       frozen_for_samp,
       frozen_for_data,
       code,
       del_id,
       orig_del,
       exty_id,
       is_public,
       modification_timestamp,
       perm_id,
       pers_id_registerer,
       pers_id_modifier,
       proj_id,
       proj_frozen,
       registration_timestamp,
       version,
       meta_data
     ) VALUES (
       NEW.id,
       NEW.frozen,
       NEW.frozen_for_samp,
       NEW.frozen_for_data,
       NEW.code,
       NEW.del_id,
       NEW.orig_del,
       NEW.exty_id,
       NEW.is_public,
       NEW.modification_timestamp,
       NEW.perm_id,
       NEW.pers_id_registerer,
       NEW.pers_id_modifier,
       NEW.proj_id,
       NEW.proj_frozen,
       NEW.registration_timestamp,
       NEW.version,
       NEW.meta_data
     );

CREATE OR REPLACE RULE experiment_update AS
    ON UPDATE TO experiments DO INSTEAD
       UPDATE experiments_all
          SET code = NEW.code,
              frozen = NEW.frozen,
              frozen_for_samp = NEW.frozen_for_samp,
              frozen_for_data = NEW.frozen_for_data,
              del_id = NEW.del_id,
              orig_del = NEW.orig_del,
              exty_id = NEW.exty_id,
              is_public = NEW.is_public,
              modification_timestamp = NEW.modification_timestamp,
              perm_id = NEW.perm_id,
              pers_id_registerer = NEW.pers_id_registerer,
              pers_id_modifier = NEW.pers_id_modifier,
              proj_id = NEW.proj_id,
              proj_frozen = NEW.proj_frozen,
              registration_timestamp = NEW.registration_timestamp,
              version = NEW.version,
              meta_data = NEW.meta_data
          WHERE id = NEW.id;


CREATE OR REPLACE RULE data_insert AS
  ON INSERT TO data DO INSTEAD
     INSERT INTO data_all (
       id,
       frozen,
       frozen_for_children,
       frozen_for_parents,
       frozen_for_comps,
       frozen_for_conts,
       code,
       del_id,
       orig_del,
       expe_id,
       expe_frozen,
       dast_id,
       data_producer_code,
       dsty_id,
       is_derived,
       is_valid,
       modification_timestamp,
       access_timestamp,
       pers_id_registerer,
       pers_id_modifier,
       production_timestamp,
       registration_timestamp,
       samp_id,
       samp_frozen,
       version,
       data_set_kind,
       meta_data
     ) VALUES (
       NEW.id,
       NEW.frozen,
       NEW.frozen_for_children,
       NEW.frozen_for_parents,
       NEW.frozen_for_comps,
       NEW.frozen_for_conts,
       NEW.code,
       NEW.del_id,
       NEW.orig_del,
       NEW.expe_id,
       NEW.expe_frozen,
       NEW.dast_id,
       NEW.data_producer_code,
       NEW.dsty_id,
       NEW.is_derived,
       NEW.is_valid,
       NEW.modification_timestamp,
       NEW.access_timestamp,
       NEW.pers_id_registerer,
       NEW.pers_id_modifier,
       NEW.production_timestamp,
       NEW.registration_timestamp,
       NEW.samp_id,
       NEW.samp_frozen,
       NEW.version,
       NEW.data_set_kind,
       NEW.meta_data
     );

CREATE OR REPLACE RULE data_update AS
    ON UPDATE TO data DO INSTEAD
       UPDATE data_all
          SET code = NEW.code,
              frozen = NEW.frozen,
              frozen_for_children = NEW.frozen_for_children,
              frozen_for_parents = NEW.frozen_for_parents,
              frozen_for_comps = NEW.frozen_for_comps,
              frozen_for_conts = NEW.frozen_for_conts,
              del_id = NEW.del_id,
              orig_del = NEW.orig_del,
              expe_id = NEW.expe_id,
              expe_frozen = NEW.expe_frozen,
              dast_id = NEW.dast_id,
              data_producer_code = NEW.data_producer_code,
              dsty_id = NEW.dsty_id,
              is_derived = NEW.is_derived,
              is_valid = NEW.is_valid,
              modification_timestamp = NEW.modification_timestamp,
              access_timestamp = NEW.access_timestamp,
              pers_id_registerer = NEW.pers_id_registerer,
              pers_id_modifier = NEW.pers_id_modifier,
              production_timestamp = NEW.production_timestamp,
              registration_timestamp = NEW.registration_timestamp,
              samp_id = NEW.samp_id,
              samp_frozen = NEW.samp_frozen,
              version = NEW.version,
              data_set_kind = NEW.data_set_kind,
              meta_data = NEW.meta_data
       WHERE id = NEW.id;