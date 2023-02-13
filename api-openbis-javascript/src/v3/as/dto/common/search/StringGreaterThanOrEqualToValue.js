/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/AbstractStringValue" ], function(stjs, AbstractStringValue) {
	var StringGreaterThanOrEqualToValue = function(string) {
		AbstractStringValue.call(this, string);
	};
	stjs.extend(StringGreaterThanOrEqualToValue, AbstractStringValue, [ AbstractStringValue ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.StringGreaterThanOrEqualToValue';
		constructor.serialVersionUID = 1;
		prototype.toString = function() {
			return "greater than or equal to '" + this.getValue() + "'";
		};
	}, {});
	return StringGreaterThanOrEqualToValue;
})