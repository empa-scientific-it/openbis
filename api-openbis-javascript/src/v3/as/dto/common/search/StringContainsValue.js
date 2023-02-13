define([ "stjs", "as/dto/common/search/AbstractStringValue" ], function(stjs, AbstractStringValue) {
	var StringContainsValue = function(value) {
		AbstractStringValue.call(this, value);
	};
	stjs.extend(StringContainsValue, AbstractStringValue, [ AbstractStringValue ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.StringContainsValue';
		constructor.serialVersionUID = 1;
		prototype.toString = function() {
			return "contains '" + this.getValue() + "'";
		};
	}, {});
	return StringContainsValue;
})