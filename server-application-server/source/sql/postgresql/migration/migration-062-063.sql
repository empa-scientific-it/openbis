-- Migration from 062 to 063

--------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------
-- Screening specific migration. Nothing will be performed on openBIS databases 
-- which are not screening specific.
--------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------

-- connect wells to experiments of their plates
UPDATE samples 
	SET expe_id = sc.expe_id
	FROM samples sc, sample_types sct
	WHERE samples.expe_id IS NULL
	AND sc.id = samples.samp_id_part_of
	AND sct.id = sc.saty_id AND sct.code = 'PLATE';

