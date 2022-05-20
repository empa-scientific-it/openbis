CREATE FUNCTION safe_double(s text) RETURNS double precision AS $$
BEGIN
RETURN s::double precision;
EXCEPTION WHEN OTHERS THEN
    RETURN NULL;
END; $$ LANGUAGE plpgsql STRICT;