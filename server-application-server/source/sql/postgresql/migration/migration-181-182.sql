DROP FUNCTION IF EXISTS escape_tsvector_string(VARCHAR) RESTRICT;

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

DROP FUNCTION IF EXISTS text_to_ts_vector(text_to_index TEXT, weight "char") CASCADE;
DROP FUNCTION IF EXISTS properties_tsvector_document_trigger() CASCADE;

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

CREATE TRIGGER sample_properties_tsvector_document BEFORE INSERT OR UPDATE
    ON sample_properties FOR EACH ROW EXECUTE PROCEDURE
    properties_tsvector_document_trigger();

DROP TRIGGER IF EXISTS SAMPLE_FROZEN_CHECK_ON_CHANGE_PROPERTY ON SAMPLE_PROPERTIES;
UPDATE sample_properties SET value = value;
CREATE TRIGGER SAMPLE_FROZEN_CHECK_ON_CHANGE_PROPERTY BEFORE UPDATE ON SAMPLE_PROPERTIES
    FOR EACH ROW WHEN (OLD.SAMP_FROZEN AND NEW.SAMP_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_SAMPLE('PROPERTY');

CREATE TRIGGER experiment_properties_tsvector_document BEFORE INSERT OR UPDATE
    ON experiment_properties FOR EACH ROW EXECUTE PROCEDURE
    properties_tsvector_document_trigger();

DROP TRIGGER IF EXISTS EXPERIMENT_FROZEN_CHECK_ON_CHANGE_PROPERTY ON EXPERIMENT_PROPERTIES;
UPDATE experiment_properties SET value = value;
CREATE TRIGGER EXPERIMENT_FROZEN_CHECK_ON_CHANGE_PROPERTY BEFORE UPDATE ON EXPERIMENT_PROPERTIES
    FOR EACH ROW WHEN (OLD.EXPE_FROZEN AND NEW.EXPE_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_EXPERIMENT('PROPERTY');

CREATE TRIGGER data_set_properties_tsvector_document BEFORE INSERT OR UPDATE
    ON data_set_properties FOR EACH ROW EXECUTE PROCEDURE
    properties_tsvector_document_trigger();

DROP TRIGGER IF EXISTS DATA_SET_FROZEN_CHECK_ON_CHANGE_PROPERTY ON DATA_SET_PROPERTIES;
UPDATE data_set_properties SET value = value;
CREATE TRIGGER DATA_SET_FROZEN_CHECK_ON_CHANGE_PROPERTY BEFORE UPDATE ON DATA_SET_PROPERTIES
    FOR EACH ROW WHEN (OLD.DASE_FROZEN AND NEW.DASE_FROZEN)
    EXECUTE PROCEDURE RAISE_EXCEPTION_FROZEN_DATA_SET('PROPERTY');

CREATE TRIGGER material_properties_tsvector_document BEFORE INSERT OR UPDATE
    ON material_properties FOR EACH ROW EXECUTE PROCEDURE
    properties_tsvector_document_trigger();

UPDATE material_properties SET value = value;

UPDATE samples_all SET code = code;
UPDATE experiments_all SET code = code;
UPDATE data_all SET code = code;
UPDATE materials SET code = code;

ALTER TABLE persons
    DROP CONSTRAINT pers_space_fk;

ALTER TABLE persons
    ADD CONSTRAINT pers_space_fk FOREIGN KEY (space_id) REFERENCES spaces(id) ON DELETE SET NULL;
