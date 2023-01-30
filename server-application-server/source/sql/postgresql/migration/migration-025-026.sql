-- JAVA ch.systemsx.cisd.openbis.generic.server.dataaccess.migration.MigrationStepFrom025To026
-- Remove ID column from SAMPLE_INPUTS table
DROP SEQUENCE SAMPLE_INPUT_ID_SEQ;
-- There was a bug in migration to db 23 - the constraint was not created. So we drop it only if it exists.
create function remove_sain_pk_constraint() returns void AS $$
begin
   perform *
     FROM information_schema.table_constraints WHERE constraint_name='sain_pk';
   if found
   then
	ALTER TABLE SAMPLE_INPUTS DROP CONSTRAINT SAIN_PK;
   end if;
end;
$$ language 'plpgsql';
select remove_sain_pk_constraint();
drop function remove_sain_pk_constraint();
ALTER TABLE SAMPLE_INPUTS DROP COLUMN ID;
ALTER TABLE SAMPLE_INPUTS ADD CONSTRAINT SAIN_PK PRIMARY KEY(SAMP_ID,PROC_ID);

-- Remove ID column from SAMPLE_MATERIAL_BATCHES table
DROP SEQUENCE SAMPLE_MATERIAL_BATCH_ID_SEQ;
-- There was a bug in migration to db 23 - the constraint was not created. So we drop it only if it exists.
create function remove_samb_pk_constraint() returns void AS $$
begin
   perform *
     FROM information_schema.table_constraints WHERE constraint_name='SAMB_PK';
   if found
   then
			ALTER TABLE SAMPLE_MATERIAL_BATCHES DROP CONSTRAINT SAMB_PK;
   end if;
end;
$$ language 'plpgsql';
select remove_samb_pk_constraint();
drop function remove_samb_pk_constraint();
ALTER TABLE SAMPLE_MATERIAL_BATCHES DROP COLUMN ID;
ALTER TABLE SAMPLE_MATERIAL_BATCHES ADD CONSTRAINT SAMB_PK PRIMARY KEY(SAMP_ID,MABA_ID);

-- Remove ID and REGISTRATION_TIMESTAMP column from DATA_SET_RELATIONSHIPS table
DROP SEQUENCE DATA_SET_RELATIONSHIP_ID_SEQ;
ALTER TABLE DATA_SET_RELATIONSHIPS DROP CONSTRAINT DSRE_PK;
ALTER TABLE DATA_SET_RELATIONSHIPS DROP CONSTRAINT DSRE_BK_UK;
ALTER TABLE DATA_SET_RELATIONSHIPS DROP COLUMN ID;
ALTER TABLE DATA_SET_RELATIONSHIPS DROP COLUMN REGISTRATION_TIMESTAMP;
ALTER TABLE DATA_SET_RELATIONSHIPS ADD CONSTRAINT DSRE_PK PRIMARY KEY(DATA_ID_PARENT,DATA_ID_CHILD);

-- Rename: 
-- OBSERVABLE_TYPES -> DATA_SET_TYPES
-- OBSERVABLE_TYPE_ID_SEQ -> DATA_SET_TYPE_ID_SEQ
-- DATA.OBTY_ID -> DATA.DSTY_ID
ALTER TABLE OBSERVABLE_TYPES RENAME TO DATA_SET_TYPES;
ALTER TABLE OBSERVABLE_TYPE_ID_SEQ RENAME TO DATA_SET_TYPE_ID_SEQ;
ALTER TABLE DATA RENAME COLUMN OBTY_ID TO DSTY_ID;
