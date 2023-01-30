CREATE OR REPLACE FUNCTION escape_tsvector_string(value VARCHAR) RETURNS VARCHAR AS $$
BEGIN
    RETURN REPLACE(
            REPLACE(
                    REPLACE(
                            REPLACE(
                                    REPLACE(
                                            REPLACE(
                                                    REPLACE(
                                                            REPLACE(
                                                                    REPLACE(LOWER(value), '<', '\<'),
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

CREATE OR REPLACE FUNCTION text_to_ts_vector(text_to_index TEXT, weight "char") RETURNS tsvector LANGUAGE plpgsql AS $$
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

CREATE OR REPLACE FUNCTION properties_tsvector_document_trigger() RETURNS trigger LANGUAGE plpgsql AS $$
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

CREATE OR REPLACE FUNCTION samples_all_tsvector_document_trigger() RETURNS trigger AS $$
DECLARE proj_code VARCHAR;
        space_code VARCHAR;
        container_code VARCHAR;
        sample_code VARCHAR;
        identifier VARCHAR := '/';
BEGIN
    IF TG_OP != 'DELETE' THEN
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
            sample_code := container_code || ':' || NEW.code;
            NEW.sample_identifier := identifier || sample_code;
            NEW.tsvector_document := setweight((escape_tsvector_string(NEW.perm_id) || ':1')::tsvector, 'A') ||
                                     setweight((escape_tsvector_string(NEW.sample_identifier) || ':1')::tsvector,
                                         'A') ||
                                     setweight((escape_tsvector_string(sample_code) || ':1')::tsvector, 'B') ||
                                     setweight((escape_tsvector_string(container_code) || ':1')::tsvector, 'B') ||
                                     setweight((escape_tsvector_string(NEW.code) || ':1')::tsvector, 'B');
        ELSE
            NEW.sample_identifier := identifier || NEW.code;
            NEW.tsvector_document := setweight((escape_tsvector_string(NEW.perm_id) || ':1')::tsvector, 'A') ||
                                     setweight((escape_tsvector_string(NEW.sample_identifier) || ':1')::tsvector,
                                         'A') ||
                                     setweight((escape_tsvector_string(NEW.code) || ':1')::tsvector, 'B');
        END IF;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS samples_all_tsvector_document ON samples_all;
CREATE TRIGGER samples_all_tsvector_document BEFORE INSERT OR UPDATE
    ON samples_all FOR EACH ROW EXECUTE PROCEDURE
    samples_all_tsvector_document_trigger();

DROP TRIGGER IF EXISTS sample_properties_tsvector_document ON sample_properties;
CREATE TRIGGER sample_properties_tsvector_document BEFORE INSERT OR UPDATE
    ON sample_properties FOR EACH ROW EXECUTE PROCEDURE
    properties_tsvector_document_trigger();

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

CREATE OR REPLACE FUNCTION experiments_all_in_project_tsvector_document_trigger() RETURNS trigger AS $$
DECLARE new_space_code VARCHAR;
        tsv tsvector;
        exp RECORD;
BEGIN
    IF TG_OP = 'UPDATE' AND NEW.space_id IS DISTINCT FROM OLD.space_id THEN
        SELECT code
        INTO new_space_code
        FROM spaces
        WHERE id = NEW.space_id;

        FOR exp IN
            SELECT id, code, perm_id
            FROM experiments_all
            WHERE proj_id = NEW.id
            LOOP
                tsv := setweight((escape_tsvector_string(exp.perm_id) || ':1')::tsvector, 'A') ||
                       setweight((escape_tsvector_string('/' || new_space_code || '/' || NEW.code || '/' || exp.code)
                           || ':1')::tsvector, 'A') ||
                       setweight((escape_tsvector_string(exp.code) || ':1')::tsvector, 'B');
                UPDATE experiments_all
                SET tsvector_document = tsv
                WHERE id = exp.id;
            END LOOP;
    END IF;
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS experiments_all_tsvector_document ON experiments_all;
CREATE TRIGGER experiments_all_tsvector_document BEFORE INSERT OR UPDATE
    ON experiments_all FOR EACH ROW EXECUTE PROCEDURE
    experiments_all_tsvector_document_trigger();

DROP TRIGGER IF EXISTS experiment_properties_tsvector_document ON experiment_properties;
CREATE TRIGGER experiment_properties_tsvector_document BEFORE INSERT OR UPDATE
    ON experiment_properties FOR EACH ROW EXECUTE PROCEDURE
    properties_tsvector_document_trigger();

DROP TRIGGER IF EXISTS experiments_all_in_project_tsvector_document ON projects;
CREATE TRIGGER experiments_all_in_project_tsvector_document AFTER UPDATE
    ON projects FOR EACH ROW EXECUTE PROCEDURE experiments_all_in_project_tsvector_document_trigger();

-- Data sets

CREATE OR REPLACE FUNCTION data_all_tsvector_document_trigger() RETURNS trigger AS $$
BEGIN
    NEW.tsvector_document := setweight(('/' || escape_tsvector_string(NEW.code) || ':1')::tsvector, 'A') ||
            setweight((escape_tsvector_string(NEW.code) || ':1')::tsvector, 'B');
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS data_all_tsvector_document ON data_all;
CREATE TRIGGER data_all_tsvector_document BEFORE INSERT OR UPDATE
    ON data_all FOR EACH ROW EXECUTE PROCEDURE
    data_all_tsvector_document_trigger();

DROP TRIGGER IF EXISTS data_set_properties_tsvector_document ON data_set_properties;
CREATE TRIGGER data_set_properties_tsvector_document BEFORE INSERT OR UPDATE
    ON data_set_properties FOR EACH ROW EXECUTE PROCEDURE
    properties_tsvector_document_trigger();

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

DROP TRIGGER IF EXISTS materials_tsvector_document ON materials;
CREATE TRIGGER materials_tsvector_document BEFORE INSERT OR UPDATE
    ON materials FOR EACH ROW EXECUTE PROCEDURE
    materials_tsvector_document_trigger();

DROP TRIGGER IF EXISTS material_properties_tsvector_document ON material_properties;
CREATE TRIGGER material_properties_tsvector_document BEFORE INSERT OR UPDATE
    ON material_properties FOR EACH ROW EXECUTE PROCEDURE
    properties_tsvector_document_trigger();

-- Updating tables

UPDATE samples_all SET id = id;
UPDATE sample_properties SET id = id;
UPDATE experiments_all SET id = id;
UPDATE experiment_properties SET id = id;
UPDATE data_all SET id = id;
UPDATE data_set_properties SET id = id;
UPDATE materials SET id = id;
UPDATE material_properties SET id = id;
UPDATE projects SET id = id;

-- Non full text search related functions

CREATE OR REPLACE FUNCTION safe_double(s text) RETURNS double precision AS $$
BEGIN
    RETURN s::double precision;
    EXCEPTION WHEN OTHERS THEN
        RETURN NULL;
END; $$ LANGUAGE plpgsql STRICT;

CREATE OR REPLACE FUNCTION safe_timestamp(s text) RETURNS timestamp with time zone AS $$
BEGIN
    RETURN s::timestamp with time zone;
    EXCEPTION WHEN OTHERS THEN
        RETURN NULL;
END; $$ LANGUAGE plpgsql STRICT;