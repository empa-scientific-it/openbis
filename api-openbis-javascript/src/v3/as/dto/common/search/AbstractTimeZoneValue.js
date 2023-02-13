/**
 * @author pkupczyk
 */
define([ "stjs" ], function(stjs) {
	var AbstractTimeZoneValue = function() {
	};
	stjs.extend(AbstractTimeZoneValue, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.AbstractTimeZoneValue';
	}, {});
	return AbstractTimeZoneValue;
})