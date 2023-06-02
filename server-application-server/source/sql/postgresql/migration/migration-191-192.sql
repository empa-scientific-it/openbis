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
