define([ "stjs", "as/dto/common/search/AbstractValue" ], function(stjs, AbstractValue) {
	var AbstractStringValue = function(value) {
		AbstractValue.call(this, value);
	};
	stjs.extend(AbstractStringValue, AbstractValue, [ AbstractValue ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.AbstractStringValue';
		constructor.serialVersionUID = 1;
	}, {});
	return AbstractStringValue;
})