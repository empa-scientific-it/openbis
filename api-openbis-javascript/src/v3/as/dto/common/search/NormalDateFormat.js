/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/IDateFormat" ], function(stjs, IDateFormat) {
	var NormalDateFormat = function() {
	};
	stjs.extend(NormalDateFormat, null, [ IDateFormat ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.NormalDateFormat';
		prototype.getFormat = function() {
			return "YYYY-MM-DD HH:mm";
		};
		prototype.toString = function() {
			return this.getFormat();
		};
	}, {});
	return NormalDateFormat;
})