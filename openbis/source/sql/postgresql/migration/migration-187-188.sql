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