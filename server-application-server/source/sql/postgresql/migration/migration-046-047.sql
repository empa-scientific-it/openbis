-- JAVA ch.systemsx.cisd.openbis.generic.server.dataaccess.migration.MigrationStepFrom046To047

------------------------------------------------------------------------------------
--  Add "main data set pattern" to data set types 
------------------------------------------------------------------------------------

ALTER TABLE data_set_types ADD COLUMN main_ds_pattern VARCHAR(300);
ALTER TABLE data_set_types ADD COLUMN main_ds_path VARCHAR(1000);