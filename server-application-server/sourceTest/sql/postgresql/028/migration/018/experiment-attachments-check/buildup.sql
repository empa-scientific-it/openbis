-------------------------------------------------------
--  Create function to test with, namely: ASSERT_EQUALS 
-------------------------------------------------------

CREATE OR REPLACE FUNCTION assert_equals(message VARCHAR, expected BIGINT, actual BIGINT) RETURNS INTEGER AS $$
   BEGIN
      IF expected != actual THEN
         RAISE EXCEPTION '%: expected:<%> but actual:<%>', message, expected, actual;
      END IF;
      RETURN NULL;
   END
$$ LANGUAGE 'plpgsql';

