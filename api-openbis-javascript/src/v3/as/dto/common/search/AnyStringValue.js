define([ "stjs", "as/dto/common/search/AbstractStringValue" ], function(stjs, AbstractStringValue) {
	var AnyStringValue = function() {
		AbstractStringValue.call(this, null);
	};
	stjs.extend(AnyStringValue, AbstractStringValue, [ AbstractStringValue ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.AnyStringValue';
		constructor.serialVersionUID = 1;
		prototype.toString = function() {
			return "any value";
		};
	}, {});
	return AnyStringValue;
})