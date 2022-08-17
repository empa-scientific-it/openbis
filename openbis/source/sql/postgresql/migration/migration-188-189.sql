-- migration from 188 to 189

-- fix project authors (set non-existent to system)

UPDATE PROJECT_RELATIONSHIPS_HISTORY
SET PERS_ID_AUTHOR =
	(SELECT ID
		FROM PERSONS
		WHERE USER_ID = 'system')
WHERE ID IN
		(SELECT PRH.ID
			FROM PROJECT_RELATIONSHIPS_HISTORY PRH
			LEFT OUTER JOIN PERSONS P ON PRH.PERS_ID_AUTHOR = P.ID
			WHERE PRH.PERS_ID_AUTHOR IS NOT NULL
				AND P.ID IS NULL);

-- fix experiment authors (set non-existent to system)

UPDATE EXPERIMENT_PROPERTIES
SET PERS_ID_AUTHOR =
	(SELECT ID
		FROM PERSONS
		WHERE USER_ID = 'system')
WHERE ID IN
		(SELECT EP.ID
			FROM EXPERIMENT_PROPERTIES EP
			LEFT OUTER JOIN PERSONS P ON EP.PERS_ID_AUTHOR = P.ID
			WHERE EP.PERS_ID_AUTHOR IS NOT NULL
				AND P.ID IS NULL);

UPDATE EXPERIMENT_PROPERTIES_HISTORY
SET PERS_ID_AUTHOR =
	(SELECT ID
		FROM PERSONS
		WHERE USER_ID = 'system')
WHERE ID IN
		(SELECT EPH.ID
			FROM EXPERIMENT_PROPERTIES_HISTORY EPH
			LEFT OUTER JOIN PERSONS P ON EPH.PERS_ID_AUTHOR = P.ID
			WHERE EPH.PERS_ID_AUTHOR IS NOT NULL
				AND P.ID IS NULL);

UPDATE EXPERIMENT_RELATIONSHIPS_HISTORY
SET PERS_ID_AUTHOR =
	(SELECT ID
		FROM PERSONS
		WHERE USER_ID = 'system')
WHERE ID IN
		(SELECT ERH.ID
			FROM EXPERIMENT_RELATIONSHIPS_HISTORY ERH
			LEFT OUTER JOIN PERSONS P ON ERH.PERS_ID_AUTHOR = P.ID
			WHERE ERH.PERS_ID_AUTHOR IS NOT NULL
				AND P.ID IS NULL);

-- fix sample authors (set non-existent to system)

UPDATE SAMPLE_PROPERTIES
SET PERS_ID_AUTHOR =
	(SELECT ID
		FROM PERSONS
		WHERE USER_ID = 'system')
WHERE ID IN
		(SELECT SP.ID
			FROM SAMPLE_PROPERTIES SP
			LEFT OUTER JOIN PERSONS P ON SP.PERS_ID_AUTHOR = P.ID
			WHERE SP.PERS_ID_AUTHOR IS NOT NULL
				AND P.ID IS NULL);

UPDATE SAMPLE_PROPERTIES_HISTORY
SET PERS_ID_AUTHOR =
	(SELECT ID
		FROM PERSONS
		WHERE USER_ID = 'system')
WHERE ID IN
		(SELECT SPH.ID
			FROM SAMPLE_PROPERTIES_HISTORY SPH
			LEFT OUTER JOIN PERSONS P ON SPH.PERS_ID_AUTHOR = P.ID
			WHERE SPH.PERS_ID_AUTHOR IS NOT NULL
				AND P.ID IS NULL);

UPDATE SAMPLE_RELATIONSHIPS_HISTORY
SET PERS_ID_AUTHOR =
	(SELECT ID
		FROM PERSONS
		WHERE USER_ID = 'system')
WHERE ID IN
		(SELECT SRH.ID
			FROM SAMPLE_RELATIONSHIPS_HISTORY SRH
			LEFT OUTER JOIN PERSONS P ON SRH.PERS_ID_AUTHOR = P.ID
			WHERE SRH.PERS_ID_AUTHOR IS NOT NULL
				AND P.ID IS NULL);

-- fix dataset authors (set non-existent to system)

UPDATE DATA_SET_PROPERTIES
SET PERS_ID_AUTHOR =
	(SELECT ID
		FROM PERSONS
		WHERE USER_ID = 'system')
WHERE ID IN
		(SELECT DP.ID
			FROM DATA_SET_PROPERTIES DP
			LEFT OUTER JOIN PERSONS P ON DP.PERS_ID_AUTHOR = P.ID
			WHERE DP.PERS_ID_AUTHOR IS NOT NULL
				AND P.ID IS NULL);

UPDATE DATA_SET_PROPERTIES_HISTORY
SET PERS_ID_AUTHOR =
	(SELECT ID
		FROM PERSONS
		WHERE USER_ID = 'system')
WHERE ID IN
		(SELECT DPH.ID
			FROM DATA_SET_PROPERTIES_HISTORY DPH
			LEFT OUTER JOIN PERSONS P ON DPH.PERS_ID_AUTHOR = P.ID
			WHERE DPH.PERS_ID_AUTHOR IS NOT NULL
				AND P.ID IS NULL);

UPDATE DATA_SET_RELATIONSHIPS_HISTORY
SET PERS_ID_AUTHOR =
	(SELECT ID
		FROM PERSONS
		WHERE USER_ID = 'system')
WHERE ID IN
		(SELECT DRH.ID
			FROM DATA_SET_RELATIONSHIPS_HISTORY DRH
			LEFT OUTER JOIN PERSONS P ON DRH.PERS_ID_AUTHOR = P.ID
			WHERE DRH.PERS_ID_AUTHOR IS NOT NULL
				AND P.ID IS NULL);

-- fix material authors (set non-existent to system)

UPDATE MATERIAL_PROPERTIES
SET PERS_ID_AUTHOR =
	(SELECT ID
		FROM PERSONS
		WHERE USER_ID = 'system')
WHERE ID IN
		(SELECT MP.ID
			FROM MATERIAL_PROPERTIES MP
			LEFT OUTER JOIN PERSONS P ON MP.PERS_ID_AUTHOR = P.ID
			WHERE MP.PERS_ID_AUTHOR IS NOT NULL
				AND P.ID IS NULL);

UPDATE MATERIAL_PROPERTIES_HISTORY
SET PERS_ID_AUTHOR =
	(SELECT ID
		FROM PERSONS
		WHERE USER_ID = 'system')
WHERE ID IN
		(SELECT MPH.ID
			FROM MATERIAL_PROPERTIES_HISTORY MPH
			LEFT OUTER JOIN PERSONS P ON MPH.PERS_ID_AUTHOR = P.ID
			WHERE MPH.PERS_ID_AUTHOR IS NOT NULL
				AND P.ID IS NULL);

-- add author FKs to project tables

ALTER TABLE project_relationships_history
ADD CONSTRAINT prrelh_auth_fk FOREIGN KEY (pers_id_author) REFERENCES persons(id)
ON UPDATE NO ACTION
ON DELETE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

-- add author FKs to experiment tables

ALTER TABLE experiment_properties
ADD CONSTRAINT expr_auth_fk FOREIGN KEY (pers_id_author) REFERENCES persons(id)
ON UPDATE NO ACTION
ON DELETE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE experiment_properties_history
ADD CONSTRAINT exprh_auth_fk FOREIGN KEY (pers_id_author) REFERENCES persons(id)
ON UPDATE NO ACTION
ON DELETE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE experiment_relationships_history
ADD CONSTRAINT exrelh_auth_fk FOREIGN KEY (pers_id_author) REFERENCES persons(id)
ON UPDATE NO ACTION
ON DELETE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

-- add author FKs to sample tables

ALTER TABLE sample_properties
ADD CONSTRAINT sapr_auth_fk FOREIGN KEY (pers_id_author) REFERENCES persons(id)
ON UPDATE NO ACTION
ON DELETE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE sample_properties_history
ADD CONSTRAINT saprh_auth_fk FOREIGN KEY (pers_id_author) REFERENCES persons(id)
ON UPDATE NO ACTION
ON DELETE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE sample_relationships_history
ADD CONSTRAINT samprelh_auth_fk FOREIGN KEY (pers_id_author) REFERENCES persons(id)
ON UPDATE NO ACTION
ON DELETE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

-- add author FKs to dataset tables

ALTER TABLE data_set_properties
ADD CONSTRAINT dspr_auth_fk FOREIGN KEY (pers_id_author) REFERENCES persons(id)
ON UPDATE NO ACTION
ON DELETE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE data_set_properties_history
ADD CONSTRAINT dsprh_auth_fk FOREIGN KEY (pers_id_author) REFERENCES persons(id)
ON UPDATE NO ACTION
ON DELETE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE data_set_relationships_history
ADD CONSTRAINT datarelh_auth_fk FOREIGN KEY (pers_id_author) REFERENCES persons(id)
ON UPDATE NO ACTION
ON DELETE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

-- add author FKs to material tables

ALTER TABLE material_properties
ADD CONSTRAINT mapr_auth_fk FOREIGN KEY (pers_id_author) REFERENCES persons(id)
ON UPDATE NO ACTION
ON DELETE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE material_properties_history
ADD CONSTRAINT maprh_auth_fk FOREIGN KEY (pers_id_author) REFERENCES persons(id)
ON UPDATE NO ACTION
ON DELETE NO ACTION
DEFERRABLE INITIALLY DEFERRED;
