CREATE FUNCTION experiments_all_in_project_tsvector_document_trigger() RETURNS trigger AS $$
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

CREATE TRIGGER experiments_all_in_project_tsvector_document AFTER UPDATE
    ON projects FOR EACH ROW EXECUTE PROCEDURE experiments_all_in_project_tsvector_document_trigger();
