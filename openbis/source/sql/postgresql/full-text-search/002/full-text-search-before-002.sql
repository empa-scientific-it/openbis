-- Disabling all available triggers

CREATE OR REPLACE FUNCTION toggle_all_available_triggers(enable boolean) RETURNS void AS $$
DECLARE trig RECORD;
        modification varchar;
BEGIN
    IF enable THEN
        modification := 'ENABLE';
    ELSE
        modification := 'DISABLE';
    END IF;

    FOR trig IN
        SELECT DISTINCT event_object_table table_name, trigger_name
        FROM information_schema.triggers
    LOOP
        EXECUTE 'ALTER TABLE ' || quote_ident(trig.table_name) || ' ' || modification || ' TRIGGER '
                || quote_ident(trig.trigger_name);
    END LOOP;
END
$$ LANGUAGE plpgsql;

SELECT toggle_all_available_triggers(false);
