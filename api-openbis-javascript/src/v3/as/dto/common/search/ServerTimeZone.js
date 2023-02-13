/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/ITimeZone" ], function(stjs, ITimeZone) {
	var ServerTimeZone = function() {
	};
	stjs.extend(ServerTimeZone, null, [ ITimeZone ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.ServerTimeZone';
	}, {});
	return ServerTimeZone;
})