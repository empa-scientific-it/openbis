DROP FUNCTION unfreeze;

CREATE OR REPLACE FUNCTION unfreeze(entityType IN TEXT, permId IN TEXT) RETURNS INT LANGUAGE plpgsql AS $$
DECLARE
	numberOfUpdates INT := 0;
BEGIN
	CASE entityType
		WHEN 'SPACE' THEN
			UPDATE spaces SET frozen = FALSE, frozen_for_proj = FALSE, frozen_for_samp = FALSE WHERE code = permId;
		WHEN 'PROJECT' THEN
			UPDATE projects SET frozen = FALSE, frozen_for_exp = FALSE, frozen_for_samp = FALSE WHERE perm_id = permId;
		WHEN 'EXPERIMENT' THEN
			UPDATE experiments_all SET frozen = FALSE, frozen_for_samp = FALSE, frozen_for_data = FALSE WHERE perm_id = permId;
		WHEN 'SAMPLE' THEN
			UPDATE samples_all SET frozen = FALSE, frozen_for_comp = FALSE, frozen_for_children = FALSE, frozen_for_parents = FALSE, frozen_for_data = FALSE WHERE perm_id = permId;
		WHEN 'DATA_SET' THEN
			UPDATE data_all SET frozen = FALSE, frozen_for_children = FALSE, frozen_for_parents = FALSE, frozen_for_comps = FALSE, frozen_for_conts = FALSE WHERE code = permId;
		ELSE
			RAISE EXCEPTION 'Nonexistent entityType --> %', entityType
			USING HINT = 'Please choose one of: SPACE, PROJECT, EXPERIMENT, SAMPLE, DATA_SET';
	END CASE;

	GET DIAGNOSTICS numberOfUpdates = ROW_COUNT;
	RETURN numberOfUpdates;
END
$$;

-- Examples
SELECT unfreeze('SPACE', 'STORAGE');
SELECT unfreeze('PROJECT', '20190315142144019-2');
SELECT unfreeze('EXPERIMENT', '20190315142144019-8');
SELECT unfreeze('SAMPLE', '20190315142144019-15');
SELECT unfreeze('DATA_SET', '20190315150656866-20');


CREATE OR REPLACE FUNCTION unfreeze_all() RETURNS INT LANGUAGE plpgsql AS $$
DECLARE
	numberOfUpdates INT := 0;
	partialNumberOfUpdates INT := 0;
BEGIN
	UPDATE spaces SET frozen = FALSE, frozen_for_proj = FALSE, frozen_for_samp = FALSE;
	GET DIAGNOSTICS partialNumberOfUpdates = ROW_COUNT;
	numberOfUpdates := numberOfUpdates + partialNumberOfUpdates;
	
	UPDATE projects SET frozen = FALSE, frozen_for_exp = FALSE, frozen_for_samp = FALSE;
	GET DIAGNOSTICS partialNumberOfUpdates = ROW_COUNT;
	numberOfUpdates := numberOfUpdates + partialNumberOfUpdates;
	
	UPDATE experiments_all SET frozen = FALSE, frozen_for_samp = FALSE, frozen_for_data = FALSE;
	GET DIAGNOSTICS partialNumberOfUpdates = ROW_COUNT;
	numberOfUpdates := numberOfUpdates + partialNumberOfUpdates;
	
	UPDATE samples_all SET frozen = FALSE, frozen_for_comp = FALSE, frozen_for_children = FALSE, frozen_for_parents = FALSE, frozen_for_data = FALSE;
	GET DIAGNOSTICS partialNumberOfUpdates = ROW_COUNT;
	numberOfUpdates := numberOfUpdates + partialNumberOfUpdates;
	
	UPDATE data_all SET frozen = FALSE, frozen_for_children = FALSE, frozen_for_parents = FALSE, frozen_for_comps = FALSE, frozen_for_conts = FALSE;
	GET DIAGNOSTICS partialNumberOfUpdates = ROW_COUNT;
	numberOfUpdates := numberOfUpdates + partialNumberOfUpdates;
	
	RETURN numberOfUpdates;
END
$$;

SELECT unfreeze_all();