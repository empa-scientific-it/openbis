/**
 * Custom DSS service code
 *
 */
define([ "stjs", "as/dto/common/id/ObjectPermId", "dss/dto/service/id/ICustomDSSServiceId" ], function(stjs, ObjectPermId, ICustomDSSServiceId) {
	var CustomDSSServiceCode = function(code) {
		ObjectPermId.call(this, code);
	};
	stjs.extend(CustomDSSServiceCode, ObjectPermId, [ ObjectPermId, ICustomDSSServiceId ], function(constructor, prototype) {
		prototype['@type'] = 'dss.dto.service.id.CustomDssServiceCode';
		constructor.serialVersionUID = 1;
	}, {});
	return CustomDSSServiceCode;
})