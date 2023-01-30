-- Migration from 165 to 166

ALTER TABLE data_all ADD COLUMN data_set_kind data_set_kind DEFAULT 'PHYSICAL' NOT NULL;

UPDATE data_all
	SET data_set_kind = data_set_types.data_set_kind
	FROM data_set_types
	WHERE data_all.dsty_id = data_set_types.id;

UPDATE data_all
	SET data_set_kind = 'PHYSICAL'
	FROM external_data
	WHERE external_data.data_id = data_all.id;

UPDATE data_all
	SET data_set_kind = 'LINK'
	FROM link_data
	WHERE link_data.data_id = data_all.id;

ALTER TABLE data_set_types DROP COLUMN data_set_kind;

CREATE OR REPLACE VIEW data AS
     SELECT id, code, dsty_id, dast_id, expe_id, data_producer_code, production_timestamp, samp_id, registration_timestamp, access_timestamp, pers_id_registerer, pers_id_modifier, is_valid, modification_timestamp, is_derived, del_id, orig_del, version, data_set_kind 
       FROM data_all 
      WHERE del_id IS NULL;

CREATE OR REPLACE VIEW data_deleted AS
     SELECT id, code, dsty_id, dast_id, expe_id, data_producer_code, production_timestamp, samp_id, registration_timestamp, access_timestamp, pers_id_registerer, pers_id_modifier, is_valid, modification_timestamp, is_derived, del_id, orig_del, version, data_set_kind 
       FROM data_all 
      WHERE del_id IS NOT NULL;

CREATE OR REPLACE RULE data_insert AS
  ON INSERT TO data DO INSTEAD 
     INSERT INTO data_all (
       id, 
       code, 
       del_id,
       orig_del,
       expe_id,
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
       version,
       data_set_kind
     ) VALUES (
       NEW.id, 
       NEW.code, 
       NEW.del_id, 
       NEW.orig_del,
       NEW.expe_id,
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
       NEW.version,
       NEW.data_set_kind
     );
     
CREATE OR REPLACE RULE data_update AS
    ON UPDATE TO data DO INSTEAD 
       UPDATE data_all
          SET code = NEW.code,
              del_id = NEW.del_id,
              orig_del = NEW.orig_del,
              expe_id = NEW.expe_id,
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
              version = NEW.version,
              data_set_kind = NEW.data_set_kind
       WHERE id = NEW.id;

-- link_data must refer to a data set of kind LINK
CREATE OR REPLACE FUNCTION check_data_set_kind_link() RETURNS trigger AS $$
DECLARE
    kind DATA_SET_KIND;
BEGIN
    SELECT data_set_kind INTO kind
        FROM data_all 
        WHERE id = NEW.data_id;
        IF (kind <> 'LINK') THEN 
            RAISE EXCEPTION 'Link data (Data Set Code: %) must reference a data set of kind LINK (is %).', 
                            NEW.data_id, kind;
        END IF;
    RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE CONSTRAINT TRIGGER check_data_set_kind_link 
    AFTER INSERT OR UPDATE ON link_data
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW
    EXECUTE PROCEDURE check_data_set_kind_link();

-- external_data must refer to a data set of kind PHYSICAL
CREATE OR REPLACE FUNCTION check_data_set_kind_physical() RETURNS trigger AS $$
DECLARE
    kind DATA_SET_KIND;
BEGIN
    SELECT data_set_kind INTO kind
        FROM data_all 
        WHERE id = NEW.data_id;
        IF (kind <> 'PHYSICAL') THEN 
            RAISE EXCEPTION 'External data (Data Set Code: %) must reference a data set of kind PHYSICAL (is %).', 
                            NEW.data_id, kind;
        END IF;
    RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE CONSTRAINT TRIGGER check_data_set_kind_physical 
    AFTER INSERT OR UPDATE ON external_data
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW
    EXECUTE PROCEDURE check_data_set_kind_physical();

