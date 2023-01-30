/**
 * External data management system perm id.
 * 
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/ObjectPermId", "as/dto/externaldms/id/IExternalDmsId" ], function(stjs, ObjectPermId, IExternalDmsId) {

	/**
	 * @param permId
	 *            External data management system perm id, e.g. "DMS1".
	 */
	var ExternalDmsPermId = function(permId) {
		ObjectPermId.call(this, permId);
	};
	stjs.extend(ExternalDmsPermId, ObjectPermId, [ ObjectPermId, IExternalDmsId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.externaldms.id.ExternalDmsPermId';
		constructor.serialVersionUID = 1;
	}, {});
	return ExternalDmsPermId;
})