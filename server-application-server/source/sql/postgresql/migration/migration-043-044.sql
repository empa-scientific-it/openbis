-- Migration from 043 to 044

--------------------------------------------------------------------------------------------------
-- Add ordinal and section columns to ETPT tables.
-- 1. Ordinal column is a positive and not null integer to entity type property type tables. 
-- Initially ordinal values of property types of one entity type start from 1 
-- and increase with increase of ETPT id (so initial order will depend on registration order). 
-- Ordinals should be unique inside one ETPT table but because we use bulk update we can't
-- easily create this constraint.
-- 2. Section - string (can be null, don't have to create uniquely named blocks with order)
--------------------------------------------------------------------------------------------------

-- add a common domain
CREATE DOMAIN ordinal_int AS bigint CHECK (VALUE > 0);

ALTER TABLE controlled_vocabulary_terms ALTER COLUMN ordinal TYPE ORDINAL_INT;

-- samples
ALTER TABLE sample_type_property_types ADD COLUMN section DESCRIPTION_1000;
ALTER TABLE sample_type_property_types ADD COLUMN ordinal ORDINAL_INT;
UPDATE sample_type_property_types SET ordinal = (
	SELECT count(*) FROM sample_type_property_types stpt 
	WHERE stpt.saty_id = sample_type_property_types.saty_id AND stpt.id <= sample_type_property_types.id
);
ALTER TABLE sample_type_property_types ALTER COLUMN ordinal SET NOT NULL;

-- experiments
ALTER TABLE experiment_type_property_types ADD COLUMN section DESCRIPTION_1000;
ALTER TABLE experiment_type_property_types ADD COLUMN ordinal ORDINAL_INT;
UPDATE experiment_type_property_types SET ordinal = (
	SELECT count(*) FROM experiment_type_property_types etpt 
	WHERE etpt.exty_id = experiment_type_property_types.exty_id AND etpt.id <= experiment_type_property_types.id
);
ALTER TABLE experiment_type_property_types ALTER COLUMN ordinal SET NOT NULL;

-- data sets
ALTER TABLE data_set_type_property_types ADD COLUMN section DESCRIPTION_1000;
ALTER TABLE data_set_type_property_types ADD COLUMN ordinal ORDINAL_INT;
UPDATE data_set_type_property_types SET ordinal = (
	SELECT count(*) FROM data_set_type_property_types dstpt 
	WHERE dstpt.dsty_id = data_set_type_property_types.dsty_id AND dstpt.id <= data_set_type_property_types.id
);
ALTER TABLE data_set_type_property_types ALTER COLUMN ordinal SET NOT NULL;

-- materials
ALTER TABLE material_type_property_types ADD COLUMN section DESCRIPTION_1000;
ALTER TABLE material_type_property_types ADD COLUMN ordinal ORDINAL_INT;
UPDATE material_type_property_types SET ordinal = (
	SELECT count(*) FROM material_type_property_types mtpt 
	WHERE mtpt.maty_id = material_type_property_types.maty_id AND mtpt.id <= material_type_property_types.id
);
ALTER TABLE material_type_property_types ALTER COLUMN ordinal SET NOT NULL;
