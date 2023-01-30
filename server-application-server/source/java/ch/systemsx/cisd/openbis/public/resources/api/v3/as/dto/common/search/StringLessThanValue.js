/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/AbstractStringValue" ], function(stjs, AbstractStringValue) {
	var StringLessThanValue = function(string) {
		AbstractStringValue.call(this, string);
	};
	stjs.extend(StringLessThanValue, AbstractStringValue, [ AbstractStringValue ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.StringLessThanValue';
		constructor.serialVersionUID = 1;
		prototype.toString = function() {
			return "less than '" + this.getValue() + "'";
		};
	}, {});
	return StringLessThanValue;
})