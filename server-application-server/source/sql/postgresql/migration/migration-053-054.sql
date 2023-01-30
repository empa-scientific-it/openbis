-- Migration from 053 to 054


-- Add RELATIONSHIP_TYPES table
CREATE TABLE relationship_types (id TECH_ID NOT NULL, code CODE NOT NULL, label COLUMN_LABEL, parent_label COLUMN_LABEL, child_label COLUMN_LABEL, description DESCRIPTION_2000, registration_timestamp TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP, pers_id_registerer TECH_ID NOT NULL, is_managed_internally BOOLEAN_CHAR NOT NULL DEFAULT 'F', is_internal_namespace BOOLEAN_CHAR NOT NULL DEFAULT 'F', dbin_id TECH_ID NOT NULL);

-- Add SAMPLE_RELATIONSHIPS table
CREATE TABLE sample_relationships (id TECH_ID NOT NULL, sample_id_parent TECH_ID NOT NULL, relationship_id TECH_ID NOT NULL, sample_id_child TECH_ID NOT NULL);

-- Add/update constraints
ALTER TABLE relationship_types ADD CONSTRAINT rety_pk PRIMARY KEY (id);
ALTER TABLE relationship_types ADD CONSTRAINT rety_uk UNIQUE(code,dbin_id);
ALTER TABLE sample_relationships ADD CONSTRAINT sare_pk PRIMARY KEY (id);
ALTER TABLE sample_relationships ADD CONSTRAINT sare_bk_uk UNIQUE(sample_id_child,sample_id_parent,relationship_id);
ALTER TABLE sample_relationships ADD CONSTRAINT sare_data_fk_child FOREIGN KEY (sample_id_child) REFERENCES samples(id) ON DELETE CASCADE;
ALTER TABLE sample_relationships ADD CONSTRAINT sare_data_fk_parent FOREIGN KEY (sample_id_parent) REFERENCES samples(id) ON DELETE CASCADE;
ALTER TABLE sample_relationships ADD CONSTRAINT sare_data_fk_relationship FOREIGN KEY (relationship_id) REFERENCES relationship_types(id);

-- Create index
CREATE INDEX sare_data_fk_i_child ON sample_relationships (sample_id_child);
CREATE INDEX sare_data_fk_i_parent ON sample_relationships (sample_id_parent);
CREATE INDEX sare_data_fk_i_relationship ON sample_relationships (relationship_id);

-- Create sequence for RELATIONSHIP_TYPES
CREATE SEQUENCE RELATIONSHIP_TYPE_ID_SEQ;
CREATE SEQUENCE SAMPLE_RELATIONSHIP_ID_SEQ;

-- Create initial relationships
insert into relationship_types
(id, 
code, 
label, 
parent_label, 
child_label, 
description, 
pers_id_registerer, 
is_managed_internally, 
is_internal_namespace, 
dbin_id) 
values
(
nextval('RELATIONSHIP_TYPE_ID_SEQ'),
'PARENT_CHILD',
'Parent - Child', 
'Parent', 
'Child', 
'Parent - Child relationship', 
(select id from persons where user_id ='system'), 
'T', 
'T', 
(select id from database_instances where is_original_source = 'T')
);

insert into relationship_types
(id, 
code, 
label, 
parent_label, 
child_label, 
description, 
pers_id_registerer, 
is_managed_internally, 
is_internal_namespace, 
dbin_id) 
values
(
nextval('RELATIONSHIP_TYPE_ID_SEQ'),
'PLATE_CONTROL_LAYOUT',
'Plate - Control Layout', 
'Plate', 
'Control Layout', 
'Plate - Control Layout relationship', 
(select id from persons where user_id ='system'), 
'T', 
'T', 
(select id from database_instances where is_original_source = 'T')
); 


-- Migrate sample relationships to new schema
INSERT INTO sample_relationships (id, sample_id_parent,sample_id_child,relationship_id) (select distinct nextval('SAMPLE_RELATIONSHIP_ID_SEQ') as id, s.SAMP_ID_GENERATED_FROM as parent_id, s.ID as child_id, rt.id as relationship_id from samples s, relationship_types rt  WHERE rt.code = 'PARENT_CHILD' and s.SAMP_ID_GENERATED_FROM is not null); 
INSERT INTO sample_relationships (id, sample_id_parent,sample_id_child,relationship_id) (select distinct nextval('SAMPLE_RELATIONSHIP_ID_SEQ') as id, s.SAMP_ID_CONTROL_LAYOUT as parent_id, s.ID as child_id, rt.id as relationship_id from samples s, relationship_types rt  WHERE rt.code = 'PLATE_CONTROL_LAYOUT' and s.SAMP_ID_CONTROL_LAYOUT is not null);

-- Drop old sample relations
ALTER TABLE SAMPLES DROP COLUMN SAMP_ID_TOP;
ALTER TABLE SAMPLES DROP COLUMN SAMP_ID_GENERATED_FROM;
ALTER TABLE SAMPLES DROP COLUMN SAMP_ID_CONTROL_LAYOUT;

--------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------
-- This is a screening specific migration. Nothing will be performed on openBIS databases 
-- which are not screening specific.
-- 
-- This migration for each existing connection between oligo well, oligo material and gene material
-- creates a direct connection between the well and the gene. 
--------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------
 
	
CREATE OR REPLACE FUNCTION CONNECT_WELLS_WITH_GENES() RETURNS void AS $$
DECLARE
	counter  int;
	oligo_well_exists bool;
BEGIN
	--------------------------------------------------
	-- create a gene property and assign it to oligo well
	--------------------------------------------------
	
	select true 
	into oligo_well_exists
	from sample_types 
	where code = 'OLIGO_WELL';
	
	if oligo_well_exists IS NULL then 
		-- skip migration if there are no oligo wells
		return;
	end if;   
	
	insert into property_types(
		id, 
		code, description, label, 
		daty_id,
		pers_id_registerer,
		dbin_id,
		maty_prop_id) 
	values(
			nextval('PROPERTY_TYPE_ID_SEQ'), 
			'GENE','Inhibited gene','Gene',
			(select id from data_types where code = 'MATERIAL'), 
			(select id from persons where user_id ='system'), 
			(select id from database_instances where is_original_source = 'T'), 
			(select id from material_types where code = 'GENE')
		);
		
	insert into sample_type_property_types( 
	  id,
	  saty_id,
	  prty_id,
	  is_mandatory,
	  pers_id_registerer,
	  ordinal
	) values(
			nextval('stpt_id_seq'), 
			(select id from sample_types where code = 'OLIGO_WELL'),
			(select id from property_types where code = 'GENE'),
			false,
			(select id from persons where user_id ='system'),
			(select max(ordinal)+1 from sample_type_property_types 
				where saty_id = (select id from sample_types where code = 'OLIGO_WELL'))
		);

	--------------------------------------------------
	-- create a gene material property for each oligo well
	--------------------------------------------------
		
	select 	count(*)
	into counter
	from
		samples well, sample_types well_type, sample_properties well_props, 
		materials well_material, material_properties well_material_props,
		materials nested_well_material
	where
		well_type.code = 'OLIGO_WELL' and
		-- find 'well_material' assigned to the well
		well_props.samp_id = well.id and well_material.id = well_props.mate_prop_id and 
		-- additional joins to entity type tables
		well_type.id = well.saty_id and
		-- well content material property
		well_material_props.mate_id = well_material.id and 
		-- material connected to the material in the well (e.g. gene)
		well_material_props.mate_prop_id = nested_well_material.id and
		nested_well_material.maty_id = (select id from material_types where code = 'GENE');
	
	if counter = 0 then 
		-- skip migration if there are no genes indirectly connected to oligo wells
		return;
	end if;   

	insert into sample_properties(id, samp_id, stpt_id, mate_prop_id, pers_id_registerer) (
		select 	nextval('sample_property_id_seq') id, 
			well.id samp_id, 
			(select stpt.id from sample_type_property_types stpt, property_types props where stpt.prty_id = props.id and props.code='GENE') stpt_id,
			nested_well_material.id mate_prop_id,
			(select id from persons where user_id ='system') pers_id_registerer 
		from
			samples well, sample_types well_type, sample_properties well_props, 
			materials well_material, material_properties well_material_props,
			materials nested_well_material
		where
			well_type.code = 'OLIGO_WELL' and
			-- find 'well_material' assigned to the well
			well_props.samp_id = well.id and well_material.id = well_props.mate_prop_id and 
			-- additional joins to entity type tables
			well_type.id = well.saty_id and
			-- well content material property
			well_material_props.mate_id = well_material.id and 
			-- material connected to the material in the well (e.g. gene)
			well_material_props.mate_prop_id = nested_well_material.id and
			nested_well_material.maty_id = (select id from material_types where code = 'GENE')
	);

END;
$$ LANGUAGE 'plpgsql';

select CONNECT_WELLS_WITH_GENES();
drop function CONNECT_WELLS_WITH_GENES();

--------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------

-- drop dataset triggers

DROP TRIGGER check_dataset_relationships_on_data_table_modification ON data;
DROP FUNCTION check_dataset_relationships_on_data_table_modification();
DROP TRIGGER check_dataset_relationships_on_relationships_table_modification ON data_set_relationships;
DROP FUNCTION check_dataset_relationships_on_relationships_table_modification();
 







