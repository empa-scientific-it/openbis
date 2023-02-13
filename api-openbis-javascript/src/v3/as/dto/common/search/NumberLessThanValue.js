/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/AbstractNumberValue" ], function(stjs, AbstractNumberValue) {
	var NumberLessThanValue = function(number) {
		AbstractNumberValue.call(this, number);
	};
	stjs.extend(NumberLessThanValue, AbstractNumberValue, [ AbstractNumberValue ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.NumberLessThanValue';
		constructor.serialVersionUID = 1;
		prototype.toString = function() {
			return "less than '" + this.getValue() + "'";
		};
	}, {});
	return NumberLessThanValue;
})