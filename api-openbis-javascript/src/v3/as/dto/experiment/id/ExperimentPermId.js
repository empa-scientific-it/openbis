/**
 * Experiment perm id.
 * 
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/ObjectPermId", "as/dto/experiment/id/IExperimentId" ], function(stjs, ObjectPermId, IExperimentId) {
	/**
	 * @param permId
	 *            Experiment perm id, e.g. "201108050937246-1031".
	 */
	var ExperimentPermId = function(permId) {
		ObjectPermId.call(this, permId);
	};
	stjs.extend(ExperimentPermId, ObjectPermId, [ ObjectPermId, IExperimentId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.id.ExperimentPermId';
		constructor.serialVersionUID = 1;
	}, {});
	return ExperimentPermId;
})