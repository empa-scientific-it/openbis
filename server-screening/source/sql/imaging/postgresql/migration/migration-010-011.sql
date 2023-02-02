-- Migration from 010 to 011

ALTER TABLE data_sets ADD COLUMN image_library_name NAME;
ALTER TABLE data_sets ADD COLUMN image_library_reader_name NAME;
