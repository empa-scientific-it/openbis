-- Migration from 098 to 099
DO $$
BEGIN
   execute 'alter database '||current_database()||' set join_collapse_limit = ''32''';
   execute 'alter database '||current_database()||' set from_collapse_limit = ''32''';
END;
$$