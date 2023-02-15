/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/ITimeZone" ], function(stjs, ITimeZone) {
	var TimeZone = function(hourOffset) {
		this.hourOffset = hourOffset;
	};
	stjs.extend(TimeZone, null, [ ITimeZone ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.TimeZone';
		prototype.hourOffset = 0;
		prototype.getHourOffset = function() {
			return this.hourOffset;
		};
	}, {});
	return TimeZone;
})