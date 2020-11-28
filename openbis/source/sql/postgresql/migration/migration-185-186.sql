-- Properties

CREATE OR REPLACE FUNCTION properties_tsvector_document_trigger() RETURNS trigger AS $$
DECLARE cvt RECORD;
BEGIN
    IF NEW.cvte_id IS NOT NULL THEN
        SELECT code, label INTO STRICT cvt FROM controlled_vocabulary_terms WHERE id = NEW.cvte_id;
        NEW.tsvector_document := setweight(to_tsvector('english', escape_tsvector_string(cvt.code)), 'C') ||
                setweight(to_tsvector('english', escape_tsvector_string(coalesce(cvt.label, ''))), 'C');
    ELSE
        NEW.tsvector_document := setweight(to_tsvector('english', escape_tsvector_string(coalesce(NEW.value, ''))), 'D');
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS SAMPLE_FROZEN_CHECK_ON_CHANGE_PROPERTY ON SAMPLE_PROPERTIES;
UPDATE sample_properties SET value = value;
CREATE TRIGGER SAMPLE_FROZEN_CHECK_ON_CHANGE_PROPERTY BEFORE UPDATE ON SAMPLE_PROPERTIES
    FOR EACH ROW WHEN (OLD.SAMP_FROZEN AND NEW.SAMP_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_SAMPLE('PROPERTY');

DROP TRIGGER IF EXISTS EXPERIMENT_FROZEN_CHECK_ON_CHANGE_PROPERTY ON EXPERIMENT_PROPERTIES;
UPDATE experiment_properties SET value = value;
CREATE TRIGGER EXPERIMENT_FROZEN_CHECK_ON_CHANGE_PROPERTY BEFORE UPDATE ON EXPERIMENT_PROPERTIES
    FOR EACH ROW WHEN (OLD.EXPE_FROZEN AND NEW.EXPE_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_EXPERIMENT('PROPERTY');

DROP TRIGGER IF EXISTS DATA_SET_FROZEN_CHECK_ON_CHANGE_PROPERTY ON DATA_SET_PROPERTIES;
UPDATE data_set_properties SET value = value;
CREATE TRIGGER DATA_SET_FROZEN_CHECK_ON_CHANGE_PROPERTY BEFORE UPDATE ON DATA_SET_PROPERTIES
    FOR EACH ROW WHEN (OLD.DASE_FROZEN AND NEW.DASE_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_DATA_SET('PROPERTY');

UPDATE material_properties SET value = value;

-- Samples

CREATE OR REPLACE FUNCTION samples_all_tsvector_document_trigger() RETURNS trigger AS $$
DECLARE proj_code VARCHAR;
        space_code VARCHAR;
        container_code VARCHAR;
        identifier VARCHAR := '/';
BEGIN
    IF TG_OP != 'DELETE' AND (TG_OP = 'INSERT' OR
            (NEW.space_id IS DISTINCT FROM OLD.space_id OR NEW.proj_id IS DISTINCT FROM OLD.proj_id OR
            NEW.samp_id_part_of IS DISTINCT FROM OLD.samp_id_part_of)) THEN
        IF NEW.space_id IS NOT NULL THEN
            SELECT code INTO STRICT space_code FROM spaces WHERE id = NEW.space_id;
            identifier := identifier || space_code || '/';
        END IF;

        IF NEW.proj_id IS NOT NULL THEN
            SELECT code INTO STRICT proj_code FROM projects WHERE id = NEW.proj_id;
            identifier := identifier || proj_code || '/';
        END IF;

        IF NEW.samp_id_part_of IS NOT NULL THEN
            SELECT code INTO STRICT container_code FROM samples_all WHERE id = NEW.samp_id_part_of;
            identifier := identifier || container_code || ':' || NEW.code;
        ELSE
            identifier := identifier || NEW.code;
        END IF;

        NEW.sample_identifier := identifier;
        NEW.tsvector_document := setweight((escape_tsvector_string(NEW.perm_id) || ':1')::tsvector, 'A') ||
                setweight((escape_tsvector_string(identifier) || ':1')::tsvector, 'A') ||
                setweight((escape_tsvector_string(NEW.code) || ':1')::tsvector, 'B');
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

BEGIN;
    DROP TRIGGER samples_all_tsvector_document ON samples_all;

    CREATE TRIGGER samples_all_tsvector_document BEFORE INSERT OR UPDATE
        ON samples_all FOR EACH ROW EXECUTE PROCEDURE
        samples_all_tsvector_document_trigger();

    UPDATE samples_all SET code = code;
COMMIT;

-- Experiments

CREATE OR REPLACE FUNCTION experiments_all_tsvector_document_trigger() RETURNS trigger AS $$
DECLARE proj_code VARCHAR;
        space_code VARCHAR;
BEGIN
    SELECT p.code, s.code INTO STRICT proj_code, space_code FROM projects p
            INNER JOIN spaces s ON p.space_id = s.id WHERE p.id = NEW.proj_id;
    NEW.tsvector_document := setweight((escape_tsvector_string(NEW.perm_id) || ':1')::tsvector, 'A') ||
            setweight((escape_tsvector_string('/' || space_code || '/' || proj_code || '/' || NEW.code)
                    || ':1')::tsvector, 'A') ||
            setweight((escape_tsvector_string(NEW.code) || ':1')::tsvector, 'B');
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

BEGIN;
    DROP TRIGGER experiments_all_tsvector_document ON experiments_all;

    CREATE TRIGGER experiments_all_tsvector_document BEFORE INSERT OR UPDATE
        ON experiments_all FOR EACH ROW EXECUTE PROCEDURE
        experiments_all_tsvector_document_trigger();

    UPDATE experiments_all SET code = code;
COMMIT;

-- Data sets

CREATE OR REPLACE FUNCTION data_all_tsvector_document_trigger() RETURNS trigger AS $$
BEGIN
    NEW.tsvector_document := setweight(('/' || escape_tsvector_string(NEW.code) || ':1')::tsvector, 'A') ||
            setweight((escape_tsvector_string(NEW.code) || ':1')::tsvector, 'B');
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

BEGIN;
    DROP TRIGGER data_all_tsvector_document ON data_all;

    CREATE TRIGGER data_all_tsvector_document BEFORE INSERT OR UPDATE
        ON data_all FOR EACH ROW EXECUTE PROCEDURE
        data_all_tsvector_document_trigger();

    UPDATE data_all SET code = code;
COMMIT;

-- Materials

CREATE OR REPLACE FUNCTION materials_tsvector_document_trigger() RETURNS trigger AS $$
DECLARE material_type_code VARCHAR;
BEGIN
    SELECT code INTO STRICT material_type_code FROM material_types WHERE id = NEW.maty_id;
    NEW.tsvector_document := setweight((escape_tsvector_string(
            NEW.code || ' (' || material_type_code || ')') || ':1')::tsvector, 'A') ||
            setweight((escape_tsvector_string(NEW.code) || ':1')::tsvector, 'B');
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

BEGIN;
    DROP TRIGGER materials_tsvector_document ON materials;

    CREATE TRIGGER materials_tsvector_document BEFORE INSERT OR UPDATE
        ON materials FOR EACH ROW EXECUTE PROCEDURE
        materials_tsvector_document_trigger();

    UPDATE materials SET code = code;
COMMIT;