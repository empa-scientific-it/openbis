-- Migration from 126 to 127

update data_all set version = 0 where version is null;
