define([ "stjs", "as/dto/common/search/AbstractStringValue" ], function(stjs, AbstractStringValue) {
	var StringContainsExactlyValue = function(value) {
		AbstractStringValue.call(this, value);
	};
	stjs.extend(StringContainsExactlyValue, AbstractStringValue, [ AbstractStringValue ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.StringContainsExactlyValue';
		constructor.serialVersionUID = 1;
		prototype.toString = function() {
			return "contains exactly '" + this.getValue() + "'";
		};
	}, {});
	return StringContainsExactlyValue;
})