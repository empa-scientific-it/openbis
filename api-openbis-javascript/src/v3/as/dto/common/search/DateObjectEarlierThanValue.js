define([ "stjs", "as/dto/common/search/AbstractDateObjectValue" ], function(stjs, AbstractDateObjectValue) {
	var DateObjectEarlierThanValue = function(value) {
		AbstractDateObjectValue.call(this, value);
	};
	stjs.extend(DateObjectEarlierThanValue, AbstractDateObjectValue, [ AbstractDateObjectValue ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.DateObjectEarlierThanValue';
		constructor.serialVersionUID = 1;
		prototype.toString = function() {
			return "earlier than '" + this.getFormattedValue() + "'";
		};
	}, {});
	return DateObjectEarlierThanValue;
})