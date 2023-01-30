-- Change events table check constraint (add 'AUTHORIZATION_GROUP')

ALTER TABLE events DROP CONSTRAINT evnt_et_enum_ck;
ALTER TABLE events ADD CONSTRAINT evnt_et_enum_ck CHECK 
		(entity_type IN ('ATTACHMENT', 'DATASET', 'EXPERIMENT', 'GROUP', 'MATERIAL', 'PROJECT', 'PROPERTY_TYPE', 'SAMPLE', 'VOCABULARY', 'AUTHORIZATION_GROUP'));
