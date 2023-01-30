CREATE OR REPLACE FUNCTION content_copies_uniqueness_check()
  RETURNS trigger AS
$BODY$
BEGIN
  NEW.location_unique_check = NEW.data_id || ',' ||
                              NEW.edms_id || ',' ||
                              coalesce(NEW.path, '') || ',' || 
                              coalesce(NEW.git_commit_hash, '') || ',' || 
                              coalesce(NEW.external_code, '');
  RETURN NEW;
END;
$BODY$
  LANGUAGE 'plpgsql';

UPDATE content_copies SET location_unique_check = 
  data_id || ',' || 
  edms_id || ',' || 
  coalesce(path, '') || ',' || 
  coalesce(git_commit_hash, '') || ',' || 
  coalesce(external_code, '');
