/**
 * Sample perm id.
 * 
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/ObjectPermId", "as/dto/sample/id/ISampleId" ], function(stjs, ObjectPermId, ISampleId) {
	/**
	 * @param permId
	 *            Sample perm id, e.g. "201108050937246-1031".
	 */
	var SamplePermId = function(permId) {
		ObjectPermId.call(this, permId);
	};
	stjs.extend(SamplePermId, ObjectPermId, [ ObjectPermId, ISampleId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.id.SamplePermId';
		constructor.serialVersionUID = 1;
	}, {});
	return SamplePermId;
})