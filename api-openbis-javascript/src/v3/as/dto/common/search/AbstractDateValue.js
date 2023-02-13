/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/AbstractValue", "as/dto/common/search/IDate" ], function(stjs, AbstractValue, IDate) {
	var AbstractDateValue = function(value) {
		AbstractValue.call(this, value);
	};
	stjs.extend(AbstractDateValue, AbstractValue, [ AbstractValue, IDate ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.AbstractDateValue';
		constructor.serialVersionUID = 1;
	}, {});
	return AbstractDateValue;
})