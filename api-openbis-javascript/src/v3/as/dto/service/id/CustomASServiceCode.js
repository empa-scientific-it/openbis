/**
 * Custom AS service code. This is the name of an AS core plugin of type 'services'.
 *
 * @author Franz-Josef Elmer
 */
define([ "stjs", "as/dto/common/id/ObjectPermId", "as/dto/service/id/ICustomASServiceId" ], function(stjs, ObjectPermId, ICustomASServiceId) {
	var CustomASServiceCode = function(code) {
		ObjectPermId.call(this, code);
	};
	stjs.extend(CustomASServiceCode, ObjectPermId, [ ObjectPermId, ICustomASServiceId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.id.CustomASServiceCode';
		constructor.serialVersionUID = 1;
	}, {});
	return CustomASServiceCode;
})