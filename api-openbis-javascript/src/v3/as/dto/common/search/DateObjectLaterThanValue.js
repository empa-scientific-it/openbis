define([ "stjs", "as/dto/common/search/AbstractDateObjectValue" ], function(stjs, AbstractDateObjectValue) {
	var DateObjectLaterThanValue = function(value) {
		AbstractDateObjectValue.call(this, value);
	};
	stjs.extend(DateObjectLaterThanValue, AbstractDateObjectValue, [ AbstractDateObjectValue ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.DateObjectLaterThanValue';
		constructor.serialVersionUID = 1;
		prototype.toString = function() {
			return "later than '" + this.getFormattedValue() + "'";
		};
	}, {});
	return DateObjectLaterThanValue;
})