-- Migration from 192 to 193

ALTER TABLE EXPERIMENT_PROPERTIES DROP CONSTRAINT EXPR_BK_UK;
ALTER TABLE SAMPLE_PROPERTIES DROP CONSTRAINT SAPR_BK_UK;
ALTER TABLE DATA_SET_PROPERTIES DROP CONSTRAINT DSPR_BK_UK;


ALTER TABLE IF EXISTS PROPERTY_TYPES
    ADD COLUMN IS_MULTI_VALUE BOOLEAN_CHAR NOT NULL DEFAULT 'F';


CREATE OR REPLACE FUNCTION samples_all_tsvector_document_trigger() RETURNS trigger AS $$
DECLARE proj_code VARCHAR;
    space_code VARCHAR;
    container_code VARCHAR;
    identifier VARCHAR := '/';
BEGIN
    IF NEW.space_id IS NOT NULL THEN
        SELECT code INTO STRICT space_code FROM spaces WHERE spaces.id = NEW.space_id;
        identifier := identifier || space_code || '/';
    END IF;

    IF NEW.proj_id IS NOT NULL THEN
        IF NEW.space_id IS NOT NULL THEN
            SELECT code INTO STRICT proj_code FROM projects WHERE projects.id = NEW.proj_id;
        ELSE
            SELECT p.code, s.code INTO STRICT proj_code, space_code FROM projects p
                INNER JOIN spaces s ON p.space_id = s.id WHERE p.id = NEW.proj_id;
            identifier := identifier || space_code || '/';
        END IF;

        identifier := identifier || proj_code || '/';
    END IF;

    IF NEW.samp_id_part_of IS NOT NULL THEN
        SELECT code INTO STRICT container_code FROM samples_all WHERE samples_all.id = NEW.samp_id_part_of;
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