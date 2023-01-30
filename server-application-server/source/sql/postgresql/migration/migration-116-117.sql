-- Migration from 116 to 117

-- add nullable perm id column
ALTER TABLE projects ADD COLUMN perm_id code;

-- generate perm ids for existing projects

CREATE OR REPLACE FUNCTION GENERATE_PROJECT_PERM_IDS() RETURNS void AS $$
DECLARE
  project_record RECORD;
BEGIN
	
    FOR project_record IN SELECT * FROM projects LOOP
        UPDATE projects SET perm_id = to_char(current_timestamp, 'YYYYMMDDHH24MISSMS') || '-' || (select nextval('PERM_ID_SEQ')) WHERE id = project_record.id;
    END LOOP;
  
END;
$$ LANGUAGE 'plpgsql';

SELECT GENERATE_PROJECT_PERM_IDS();
DROP function GENERATE_PROJECT_PERM_IDS();

-- change perm id column to not null
ALTER TABLE projects ALTER COLUMN perm_id SET NOT NULL;
ALTER TABLE PROJECTS ADD CONSTRAINT PROJ_PI_UK UNIQUE(PERM_ID);
