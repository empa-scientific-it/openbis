/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/AbstractStringValue" ], function(stjs, AbstractStringValue) {
	var StringGreaterThanValue = function(string) {
		AbstractStringValue.call(this, string);
	};
	stjs.extend(StringGreaterThanValue, AbstractStringValue, [ AbstractStringValue ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.StringGreaterThanValue';
		constructor.serialVersionUID = 1;
		prototype.toString = function() {
			return "greater than '" + this.getValue() + "'";
		};
	}, {});
	return StringGreaterThanValue;
})