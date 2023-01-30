/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/AbstractNumberValue" ], function(stjs, AbstractNumberValue) {
	var NumberEqualToValue = function(number) {
		AbstractNumberValue.call(this, number);
	};
	stjs.extend(NumberEqualToValue, AbstractNumberValue, [ AbstractNumberValue ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.NumberEqualToValue';
		constructor.serialVersionUID = 1;
		prototype.toString = function() {
			return "equal to '" + this.getValue() + "'";
		};
	}, {});
	return NumberEqualToValue;
})