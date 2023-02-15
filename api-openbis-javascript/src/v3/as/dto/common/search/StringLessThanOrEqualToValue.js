/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/AbstractStringValue" ], function(stjs, AbstractStringValue) {
	var StringLessThanOrEqualToValue = function(string) {
		AbstractStringValue.call(this, string);
	};
	stjs.extend(StringLessThanOrEqualToValue, AbstractStringValue, [ AbstractStringValue ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.StringLessThanOrEqualToValue';
		constructor.serialVersionUID = 1;
		prototype.toString = function() {
			return "less than or equal to '" + this.getValue() + "'";
		};
	}, {});
	return StringLessThanOrEqualToValue;
})