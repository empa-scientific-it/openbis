define([ "stjs", "as/dto/common/search/AbstractDateObjectValue" ], function(stjs, AbstractDateObjectValue) {
	var DateObjectLaterThanOrEqualToValue = function(value) {
		AbstractDateObjectValue.call(this, value);
	};
	stjs.extend(DateObjectLaterThanOrEqualToValue, AbstractDateObjectValue, [ AbstractDateObjectValue ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.DateObjectLaterThanOrEqualToValue';
		constructor.serialVersionUID = 1;
		prototype.toString = function() {
			return "later than or equal to '" + this.getFormattedValue() + "'";
		};
	}, {});
	return DateObjectLaterThanOrEqualToValue;
})