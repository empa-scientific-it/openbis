-- Full text search

CREATE FUNCTION escape_tsvector_string(value VARCHAR) RETURNS VARCHAR AS $$
BEGIN
    RETURN REPLACE(
            REPLACE(
                    REPLACE(
                            REPLACE(
                                    REPLACE(
                                            REPLACE(
                                                    REPLACE(
                                                            REPLACE(
                                                                    REPLACE(value, '<', '\<'),
                                                                    '!', '\!'),
                                                            '*', '\*'),
                                                    '&', '\&'),
                                            '|', '\|'),
                                    ')', '\)'),
                            '(', '\('),
                    ':', '\:'),
            ' ', '\ ');
END
$$ LANGUAGE plpgsql;

-- Controlled Vocabularies

CREATE FUNCTION text_to_ts_vector(text_to_index TEXT, weight "char") RETURNS tsvector LANGUAGE plpgsql AS $$
DECLARE indexed BOOLEAN;
    DECLARE result tsvector;
BEGIN
    indexed := FALSE;
    text_to_index := regexp_replace(coalesce(text_to_index, ''), E'<[^>]+>', '', 'gi'); -- Remove XML Tags
    text_to_index := escape_tsvector_string(text_to_index); -- Escape characters used by ts_vector
    WHILE NOT INDEXED LOOP
            BEGIN
                result = setweight(to_tsvector('english', text_to_index), weight)::TEXT;
                indexed := TRUE;
            EXCEPTION WHEN sqlstate '54000' THEN
                text_to_index := left(text_to_index, LENGTH(text_to_index) / 2); -- If the index is too big reduce the size of the text to half
            END;
        END LOOP;
    RETURN result;
END $$;

CREATE FUNCTION properties_tsvector_document_trigger() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE cvt RECORD;
BEGIN
    IF NEW.cvte_id IS NOT NULL THEN
        SELECT code, label INTO STRICT cvt FROM controlled_vocabulary_terms WHERE id = NEW.cvte_id;
        NEW.tsvector_document := text_to_ts_vector(cvt.code, 'C') || text_to_ts_vector(cvt.label, 'C');
    ELSE
        NEW.tsvector_document := text_to_ts_vector(NEW.value, 'D');
    END IF;
    RETURN NEW;
END $$;

-- Samples

CREATE FUNCTION samples_all_tsvector_document_trigger() RETURNS trigger AS $$
DECLARE proj_code VARCHAR;
    space_code VARCHAR;
    container_code VARCHAR;
    identifier VARCHAR := '/';
BEGIN
    IF NEW.space_id IS NOT NULL THEN
        SELECT code INTO STRICT space_code FROM spaces WHERE id = NEW.space_id;
        identifier := identifier || space_code || '/';
    END IF;

    IF NEW.proj_id IS NOT NULL THEN
        IF NEW.space_id IS NOT NULL THEN
            SELECT code INTO STRICT proj_code FROM projects WHERE id = NEW.proj_id;
        ELSE
            SELECT p.code, s.code INTO STRICT proj_code, space_code FROM projects p
                INNER JOIN spaces s ON p.space_id = s.id WHERE id = NEW.proj_id;
            identifier := identifier || space_code || '/';
        END IF;

        identifier := identifier || proj_code || '/';
    END IF;

    IF NEW.samp_id_part_of IS NOT NULL THEN
        SELECT code INTO STRICT container_code FROM samples_all WHERE id = NEW.samp_id_part_of;
        identifier := identifier || container_code || ':' || NEW.code;
    ELSE
        identifier := identifier || NEW.code;
    END IF;

    NEW.sample_identifier := identifier;
    NEW.tsvector_document := (escape_tsvector_string(NEW.perm_id) || ':1')::tsvector ||
            (escape_tsvector_string(NEW.code) || ':1')::tsvector ||
            (escape_tsvector_string(identifier) || ':1')::tsvector;
    RETURN NEW;
END
$$ LANGUAGE plpgsql;


CREATE DOMAIN SAMPLE_IDENTIFIER AS VARCHAR(404); -- /CODE/CODE/CODE:CODE

ALTER TABLE samples_all ADD COLUMN sample_identifier sample_identifier;
ALTER TABLE samples_all ADD COLUMN tsvector_document TSVECTOR;
ALTER TABLE samples_all ADD CONSTRAINT samp_identifier_uk UNIQUE (sample_identifier);

CREATE INDEX SAMP_IDENTIFIER_I ON SAMPLES_ALL (SAMPLE_IDENTIFIER);

CREATE OR REPLACE VIEW samples AS
    SELECT id, perm_id, code, proj_id, proj_frozen, expe_id, expe_frozen, saty_id, registration_timestamp,
           modification_timestamp, pers_id_registerer, pers_id_modifier, del_id, orig_del, space_id, space_frozen,
           samp_id_part_of, cont_frozen, version, frozen, frozen_for_comp, frozen_for_children, frozen_for_parents,
           frozen_for_data, tsvector_document, sample_identifier
    FROM samples_all
    WHERE del_id IS NULL;

BEGIN;
    CREATE TRIGGER samples_all_tsvector_document BEFORE INSERT OR UPDATE
        ON samples_all FOR EACH ROW EXECUTE PROCEDURE
        samples_all_tsvector_document_trigger();

    UPDATE samples_all SET code = code;
COMMIT;

ALTER TABLE samples_all
    ALTER COLUMN tsvector_document SET NOT NULL;

ALTER TABLE sample_properties
    ADD COLUMN tsvector_document TSVECTOR;

CREATE TRIGGER sample_properties_tsvector_document BEFORE INSERT OR UPDATE
    ON sample_properties FOR EACH ROW EXECUTE PROCEDURE
    properties_tsvector_document_trigger();

DROP TRIGGER IF EXISTS SAMPLE_FROZEN_CHECK_ON_CHANGE_PROPERTY ON SAMPLE_PROPERTIES;
UPDATE sample_properties SET value = value;
CREATE TRIGGER SAMPLE_FROZEN_CHECK_ON_CHANGE_PROPERTY BEFORE UPDATE ON SAMPLE_PROPERTIES
    FOR EACH ROW WHEN (OLD.SAMP_FROZEN AND NEW.SAMP_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_SAMPLE('PROPERTY');

ALTER TABLE sample_properties
    ALTER COLUMN tsvector_document SET NOT NULL;

CREATE INDEX sample_properties_search_index ON sample_properties USING gin(tsvector_document);

-- Experiments

CREATE FUNCTION experiments_all_tsvector_document_trigger() RETURNS trigger AS $$
DECLARE proj_code VARCHAR;
        space_code VARCHAR;
BEGIN
    SELECT p.code, s.code INTO STRICT proj_code, space_code FROM projects p
        INNER JOIN spaces s ON p.space_id = s.id WHERE p.id = NEW.proj_id;
    NEW.tsvector_document := (escape_tsvector_string(NEW.perm_id) || ':1')::tsvector ||
            (escape_tsvector_string(NEW.code) || ':1')::tsvector ||
            (escape_tsvector_string('/' || space_code || '/' || proj_code || '/' || NEW.code) || ':1')::tsvector;
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

ALTER TABLE experiments_all
    ADD COLUMN tsvector_document TSVECTOR;

CREATE OR REPLACE VIEW experiments AS
    SELECT id, perm_id, code, exty_id, pers_id_registerer, pers_id_modifier, registration_timestamp, modification_timestamp,
           proj_id, proj_frozen, del_id, orig_del, is_public, version, frozen, frozen_for_samp, frozen_for_data, tsvector_document
    FROM experiments_all
    WHERE del_id IS NULL;

BEGIN;
    CREATE TRIGGER experiments_all_tsvector_document BEFORE INSERT OR UPDATE
        ON experiments_all FOR EACH ROW EXECUTE PROCEDURE
        experiments_all_tsvector_document_trigger();

    UPDATE experiments_all SET code = code;
COMMIT;

ALTER TABLE experiments_all
    ALTER COLUMN tsvector_document SET NOT NULL;

ALTER TABLE experiment_properties
    ADD COLUMN tsvector_document TSVECTOR;

CREATE TRIGGER experiment_properties_tsvector_document BEFORE INSERT OR UPDATE
    ON experiment_properties FOR EACH ROW EXECUTE PROCEDURE
    properties_tsvector_document_trigger();

DROP TRIGGER IF EXISTS EXPERIMENT_FROZEN_CHECK_ON_CHANGE_PROPERTY ON EXPERIMENT_PROPERTIES;
UPDATE experiment_properties SET value = value;
CREATE TRIGGER EXPERIMENT_FROZEN_CHECK_ON_CHANGE_PROPERTY BEFORE UPDATE ON EXPERIMENT_PROPERTIES
    FOR EACH ROW WHEN (OLD.EXPE_FROZEN AND NEW.EXPE_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_EXPERIMENT('PROPERTY');

ALTER TABLE experiment_properties
    ALTER COLUMN tsvector_document SET NOT NULL;

CREATE INDEX experiment_properties_search_index ON experiment_properties USING gin(tsvector_document);

-- Data sets

CREATE FUNCTION data_all_tsvector_document_trigger() RETURNS trigger AS $$
BEGIN
    NEW.tsvector_document := (escape_tsvector_string(NEW.data_set_kind) || ':1')::tsvector ||
        (escape_tsvector_string(NEW.code) || ':1')::tsvector ||
        ('/' || escape_tsvector_string(NEW.code) || ':1')::tsvector;
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

ALTER TABLE data_all
    ADD COLUMN tsvector_document TSVECTOR;

CREATE OR REPLACE VIEW data AS
    SELECT id, code, dsty_id, dast_id, expe_id, expe_frozen, data_producer_code, production_timestamp, samp_id,
           samp_frozen, registration_timestamp, access_timestamp, pers_id_registerer, pers_id_modifier, is_valid,
           modification_timestamp,is_derived, del_id, orig_del, version, data_set_kind,
           frozen, frozen_for_children, frozen_for_parents, frozen_for_comps, frozen_for_conts, tsvector_document
    FROM data_all
    WHERE del_id IS NULL;

BEGIN;
    CREATE TRIGGER data_all_tsvector_document BEFORE INSERT OR UPDATE
        ON data_all FOR EACH ROW EXECUTE PROCEDURE
        data_all_tsvector_document_trigger();

    UPDATE data_all SET code = code;
COMMIT;

ALTER TABLE data_all
    ALTER COLUMN tsvector_document SET NOT NULL;

ALTER TABLE data_set_properties
    ADD COLUMN tsvector_document TSVECTOR;

CREATE TRIGGER data_set_properties_tsvector_document BEFORE INSERT OR UPDATE
    ON data_set_properties FOR EACH ROW EXECUTE PROCEDURE
    properties_tsvector_document_trigger();

DROP TRIGGER IF EXISTS DATA_SET_FROZEN_CHECK_ON_CHANGE_PROPERTY ON DATA_SET_PROPERTIES;
UPDATE data_set_properties SET value = value;
CREATE TRIGGER DATA_SET_FROZEN_CHECK_ON_CHANGE_PROPERTY BEFORE UPDATE ON DATA_SET_PROPERTIES
    FOR EACH ROW WHEN (OLD.DASE_FROZEN AND NEW.DASE_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_DATA_SET('PROPERTY');

ALTER TABLE data_set_properties
    ALTER COLUMN tsvector_document SET NOT NULL;

CREATE INDEX data_set_properties_search_index ON data_set_properties USING gin(tsvector_document);

-- Materials

CREATE FUNCTION materials_tsvector_document_trigger() RETURNS trigger AS $$
DECLARE material_type_code VARCHAR;
BEGIN
    SELECT code INTO STRICT material_type_code FROM material_types WHERE id = NEW.maty_id;
    NEW.tsvector_document := (escape_tsvector_string(NEW.code) || ':1')::tsvector ||
            (escape_tsvector_string(NEW.code || ' (' || material_type_code || ')') || ':1')::tsvector;
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

ALTER TABLE materials ADD COLUMN tsvector_document TSVECTOR;

BEGIN;
    CREATE TRIGGER materials_tsvector_document BEFORE INSERT OR UPDATE
        ON materials FOR EACH ROW EXECUTE PROCEDURE
        materials_tsvector_document_trigger();

    UPDATE materials SET code = code;
COMMIT;

ALTER TABLE materials
    ALTER COLUMN tsvector_document SET NOT NULL;

ALTER TABLE material_properties ADD COLUMN tsvector_document TSVECTOR;

CREATE TRIGGER material_properties_tsvector_document BEFORE INSERT OR UPDATE
    ON material_properties FOR EACH ROW EXECUTE PROCEDURE
    properties_tsvector_document_trigger();

UPDATE material_properties SET value = value;

ALTER TABLE material_properties
    ALTER COLUMN tsvector_document SET NOT NULL;

CREATE INDEX material_properties_search_index ON material_properties USING gin(tsvector_document);