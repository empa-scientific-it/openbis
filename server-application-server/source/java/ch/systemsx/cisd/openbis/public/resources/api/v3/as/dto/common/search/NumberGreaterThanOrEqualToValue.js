/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/AbstractNumberValue" ], function(stjs, AbstractNumberValue) {
	var NumberGreaterOrEqualThanValue = function(number) {
		AbstractNumberValue.call(this, number);
	};
	stjs.extend(NumberGreaterOrEqualThanValue, AbstractNumberValue, [ AbstractNumberValue ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.NumberGreaterThanOrEqualToValue';
		constructor.serialVersionUID = 1;
		prototype.toString = function() {
			return "greater or equal to '" + this.getValue() + "'";
		};
	}, {});
	return NumberGreaterOrEqualThanValue;
})