-- Migration from 049 to 050

-- add data set status 
ALTER TABLE external_data ADD COLUMN status VARCHAR(100) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE external_data ADD CONSTRAINT exda_status_enum_ck 
      CHECK (status IN ('LOCKED', 'ACTIVE', 'ARCHIVED', 'ACTIVATION_IN_PROGRESS', 'ARCHIVIZATION_IN_PROGRESS')); 
-- 