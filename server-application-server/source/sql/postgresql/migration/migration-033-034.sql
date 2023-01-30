------------------------------------------------------------------------------------
-- Create sequence for generating permanent identifiers starting with nextval of existing dataset sequence.
------------------------------------------------------------------------------------
CREATE FUNCTION CREATE_SEQUENCE(EXISTING_SEQUENCE VARCHAR, NEW_SEQUENCE VARCHAR) RETURNS VOID AS $$
DECLARE
  CURR_SEQ_VAL   INTEGER;
BEGIN
  SELECT INTO CURR_SEQ_VAL NEXTVAL(EXISTING_SEQUENCE);
  EXECUTE 'CREATE SEQUENCE ' || NEW_SEQUENCE || ' START WITH ' || CURR_SEQ_VAL;
  RETURN;
END;
$$ LANGUAGE 'plpgsql';

SELECT CREATE_SEQUENCE('DATA_ID_SEQ', 'PERM_ID_SEQ');
DROP FUNCTION CREATE_SEQUENCE(VARCHAR, VARCHAR);

------------------------------------------------------------------------------------
-- Create sequence data_set_relationship_id_seq if it doesn't exist
------------------------------------------------------------------------------------

create function create_data_set_relationship_id_seq() returns void AS $$
begin
   perform *
     FROM information_schema.sequences WHERE sequence_name='data_set_relationship_id_seq';
   if not found
   then
     CREATE SEQUENCE DATA_SET_RELATIONSHIP_ID_SEQ;
   end if;
end;
$$ language 'plpgsql';
select create_data_set_relationship_id_seq();
drop function create_data_set_relationship_id_seq();

------------------------------------------------------------------------------------
-- Add perm_id columns to samples and experiments.
------------------------------------------------------------------------------------
ALTER TABLE SAMPLES ADD COLUMN PERM_ID CODE;
ALTER TABLE EXPERIMENTS ADD COLUMN PERM_ID CODE;

UPDATE SAMPLES SET PERM_ID = to_char(registration_timestamp,'YYYYMMDDHHSSMS') || '-' || NEXTVAL('PERM_ID_SEQ');
UPDATE EXPERIMENTS SET PERM_ID = to_char(registration_timestamp,'YYYYMMDDHHSSMS') || '-' || NEXTVAL('PERM_ID_SEQ');
ALTER TABLE SAMPLES ALTER COLUMN PERM_ID SET NOT NULL;
ALTER TABLE EXPERIMENTS ALTER COLUMN PERM_ID SET NOT NULL;
ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_PI_UK UNIQUE(PERM_ID);
ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_PI_UK UNIQUE(PERM_ID);

------------------------------------------------------------------------------------
-- Add column is_chosen_from_list to controlled_vocabularies.
------------------------------------------------------------------------------------
ALTER TABLE controlled_vocabularies ADD COLUMN is_chosen_from_list BOOLEAN_CHAR NOT NULL DEFAULT TRUE;
ALTER TABLE controlled_vocabularies ADD COLUMN source_uri CHARACTER VARYING(250);
------------------------------------------------------------------------------------
-- Modify Data table - remove arc connection between Sample and Data table, use a flag instead
------------------------------------------------------------------------------------

-- add new columns

ALTER TABLE data
    ADD COLUMN is_derived boolean_char;
ALTER TABLE data
    ADD COLUMN samp_id tech_id;

-- update values in new columns

UPDATE data
    SET is_derived = (samp_id_derived_from IS NOT NULL); 
UPDATE data
    SET samp_id = samp_id_derived_from 
    WHERE is_derived = 'TRUE';
UPDATE data
    SET samp_id = samp_id_acquired_from 
    WHERE is_derived = 'FALSE';

-- remove old columns

ALTER TABLE data
    DROP COLUMN samp_id_acquired_from;
ALTER TABLE data
    DROP COLUMN samp_id_derived_from;
    
-- add constraints and indexes to new columns

ALTER TABLE data
    ALTER COLUMN is_derived SET NOT NULL;
ALTER TABLE data
    ALTER COLUMN samp_id SET NOT NULL;   

ALTER TABLE data
    ADD CONSTRAINT data_samp_fk FOREIGN KEY (samp_id) REFERENCES samples (id);
CREATE INDEX data_samp_fk_i ON data USING btree (samp_id);

------------------------------------------------------------------------------------
-- Granting SELECT privilege to group openbis_readonly
------------------------------------------------------------------------------------

GRANT SELECT ON SEQUENCE attachment_content_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE attachment_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE code_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE controlled_vocabulary_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE cvte_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE data_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE data_set_property_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE data_set_relationship_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE data_set_type_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE data_store_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE data_type_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE database_instance_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE dstpt_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE etpt_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE event_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE experiment_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE experiment_property_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE experiment_type_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE file_format_type_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE group_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE invalidation_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE locator_type_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE material_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE material_property_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE material_type_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE mtpt_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE perm_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE person_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE project_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE property_type_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE role_assignment_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE sample_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE sample_property_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE sample_type_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE stpt_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE attachment_contents TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE attachments TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE controlled_vocabularies TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE controlled_vocabulary_terms TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE data TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE data_set_properties TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE data_set_relationships TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE data_set_type_property_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE data_set_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE data_stores TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE data_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE database_instances TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE database_version_logs TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE events TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE experiment_properties TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE experiment_type_property_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE experiment_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE experiments TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE external_data TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE file_format_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE groups TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE invalidations TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE locator_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE material_properties TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE material_type_property_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE material_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE materials TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE persons TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE projects TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE property_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE role_assignments TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE sample_properties TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE sample_type_property_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE sample_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE samples TO GROUP OPENBIS_READONLY;

------------------------------------------------------------------------------------
-- Correct sequencers (fix bug from migration 026-027)
------------------------------------------------------------------------------------

SELECT setval('material_property_id_seq', max(id)) FROM material_properties;
SELECT setval('sample_property_id_seq', max(id)) FROM sample_properties;
SELECT setval('group_id_seq', max(id)) FROM groups;
SELECT setval('material_id_seq', max(id)) FROM materials;
SELECT setval('project_id_seq', max(id)) FROM projects;
SELECT setval('sample_id_seq', max(id)) FROM samples;

------------------------------------------------------------------------------------
-- Migrate MATERIAL_BATCHES and INHIBITOR_OF
------------------------------------------------------------------------------------

-- Create property type with specified code, description, label, data_type and material type
create or replace function create_property_type(prop_code varchar, prop_description varchar, prop_label varchar, data_type_code varchar, material_type_code varchar) returns void as $$
declare
  material_type_id varchar;
begin
  select into material_type_id id from material_types where code = material_type_code;
  if material_type_id is null
  then
    material_type_id = 'null';
  end if;
  execute 'insert into property_types (id, code,  description, label, daty_id,registration_timestamp, pers_id_registerer, covo_id,  is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id) values('
    || 'nextval(''PROPERTY_TYPE_ID_SEQ'')'
    || ',' || quote_literal(prop_code)
    || ',' || quote_literal(prop_description)
    || ',' || quote_literal(prop_label)
    || ',' || '(select id from data_types where code = ' || quote_literal(data_type_code)|| ')'
    || ',' || 'now()'
    || ',' || '(select id from persons where user_id =''system'')'
    || ',' || 'null'
    || ',' || 'false'
    || ',' || 'true'
    || ',' || '(select id from database_instances where is_original_source = true)'
    || ',' || material_type_id
    || ')';
end;
$$ language 'plpgsql';

-- Create property type without specifing material type
create or replace function create_property_type(prop_code varchar, prop_description varchar, prop_label varchar, data_type_code varchar) returns void as $$
begin
	perform create_property_type(prop_code, prop_description, prop_label, data_type_code, null);
end;
$$ language 'plpgsql';

create or replace function assign_property_type(entity varchar, prop_code varchar, type_id integer, mandatory boolean) returns void as $$
declare 
  etpt varchar;
  enty varchar;
begin
  etpt := substring(entity from 1 for 1) || 'tpt';
  enty := substring(entity from 1 for 2) || 'ty';
  execute 'insert into ' || entity || '_type_property_types (id, ' || enty || '_id, prty_id, pers_id_registerer, is_mandatory) values('
  || 'nextval('''|| etpt ||'_id_seq'')'
  || ',' || type_id
  || ',' || '(select id from property_types where code = ' || quote_literal(prop_code) || ')'
  || ',' || '(select id from persons where user_id =''system'')'
  || ',' || quote_literal(mandatory)
  || ')';
end;
$$ language 'plpgsql';

-- Assign INHIBITOR_OF property to materials of type OLIGO
create or replace function assign_inhibitor_property() returns void as $$
declare 
  material_type_id integer;
begin
  select into material_type_id id from material_types where code = 'OLIGO';
  execute assign_property_type('MATERIAL', 'INHIBITOR_OF', material_type_id, true);
end;
$$ language 'plpgsql';

-- Create property values for INHIBITOR_OF 
create or replace function create_inhibitor_values() returns void as $$
declare 
  material_with_inhibitor record;
  material_type_id integer;
  material_id integer;
  property_type_id integer;
  mtpt_id integer;
  inhibitor_id integer;
begin
  for material_with_inhibitor in select distinct id, maty_id, mate_id_inhibitor_of  from materials where mate_id_inhibitor_of is not null loop
    material_id := material_with_inhibitor.id;
    material_type_id := material_with_inhibitor.maty_id;
    inhibitor_id = material_with_inhibitor.mate_id_inhibitor_of;
    select into property_type_id id from property_types where code = 'INHIBITOR_OF'; 
    select into mtpt_id id from material_type_property_types where maty_id = material_type_id and prty_id = property_type_id;
    execute 'insert into material_properties (id, mate_id, mtpt_id, pers_id_registerer, mate_prop_id) values('
      || 'nextval(''material_property_id_seq'')'
      || ',' || material_id
      || ',' || mtpt_id
      || ',' || '(select id from persons where user_id =''system'')'
      || ',' || inhibitor_id
      || ')';
  end loop;
end;
$$ language 'plpgsql';

-- Assign specified property to samples with defined batches
create or replace function assign_material_batch_properties(prop_code varchar) returns void as $$
declare 
  sample_type_id integer;
begin
  for sample_type_id in select distinct s.saty_id from samples s, sample_material_batches b where b.samp_id = s.id loop
    execute assign_property_type('SAMPLE', prop_code, sample_type_id, false);
  end loop;
end;
$$ language 'plpgsql';

-- Create property values for given batch property
create or replace function create_batch_values(prop_code varchar) returns void as $$
declare 
  sample_with_batches record;
  stpt_id integer;
  sample_id integer;
  registrator_id integer;
  registration_timestamp timestamp;
  prop_col_name varchar;
  prop_value varchar;
begin
  if prop_code = 'MATERIAL' then
    prop_col_name := 'mate_prop_id';
  else
    prop_col_name := 'value';
  end if;
  for sample_with_batches in select distinct mb.code, mb.amount, mb.mate_id, mb.registration_timestamp, mb.pers_id_registerer, s.saty_id, s.id from material_batches mb, sample_material_batches smb, samples s where mb.id = smb.maba_id and s.id = smb.samp_id loop
    select into stpt_id id from sample_type_property_types where saty_id = sample_with_batches.saty_id and prty_id = (select id from property_types where code = prop_code);
    sample_id := sample_with_batches.id;
    registrator_id := sample_with_batches.pers_id_registerer;
    registration_timestamp := sample_with_batches.registration_timestamp;
    if prop_code = 'MATERIAL' then
      prop_value := sample_with_batches.mate_id;
    elsif prop_code = 'MATERIAL_BATCH' then
      prop_value := quote_literal(sample_with_batches.code);
    else 
    	prop_value := quote_literal(sample_with_batches.amount);
    end if;
    if prop_value is not null then
      execute 'insert into sample_properties (id, samp_id, stpt_id, pers_id_registerer, ' || prop_col_name || ' , registration_timestamp) values('
        || 'nextval(''sample_property_id_seq'')'
        || ',' || sample_id
        || ',' || stpt_id
        || ',' || registrator_id
        || ',' || prop_value
        || ',' || quote_literal(registration_timestamp)
        || ')';
    end if;
  end loop;
end;
$$ language 'plpgsql';

-- Perform full INHIBITOR migration
create or replace function migrate_inhibitor() returns void as $$
begin
  perform create_property_type('INHIBITOR_OF','Inhibitor Of','Inhibitor Of','MATERIAL', 'GENE');
  perform assign_inhibitor_property();
  perform create_inhibitor_values();
  alter table materials drop column mate_id_inhibitor_of;
end;
$$ language 'plpgsql';

-- Perform full material batch migration
create or replace function migrate_material_batches() returns void as $$
begin
	perform create_property_type('MATERIAL','Material','Material','MATERIAL');
	perform create_property_type('MATERIAL_BATCH','Material Batch','Material Batch','VARCHAR');
	perform create_property_type('MATERIAL_AMOUNT','Amount Of Material','Material Amount','REAL');
	
	perform assign_material_batch_properties('MATERIAL');
	perform assign_material_batch_properties('MATERIAL_BATCH');
	perform assign_material_batch_properties('MATERIAL_AMOUNT');
	
	perform create_batch_values('MATERIAL');
	perform create_batch_values('MATERIAL_BATCH');
	perform create_batch_values('MATERIAL_AMOUNT');
  
  drop table sample_material_batches;
  drop table material_batches;
  drop sequence material_batch_id_seq;
end;
$$ language 'plpgsql';

-- Perform migration
select migrate_inhibitor();
select migrate_material_batches();

-- Clean up
drop function create_property_type(prop_code varchar, prop_description varchar, prop_label varchar, data_type_code varchar);
drop function create_property_type(prop_code varchar, prop_description varchar, prop_label varchar, data_type_code varchar, material_type varchar);
drop function assign_property_type(entity varchar, prop_code varchar, type_id integer, mandatory boolean);
drop function assign_inhibitor_property();
drop function create_inhibitor_values();
drop function assign_material_batch_properties(prop_code varchar);
drop function create_batch_values(prop_code varchar);
drop function migrate_inhibitor();
drop function migrate_material_batches();

