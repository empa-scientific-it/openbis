define([ "stjs", "as/dto/common/search/AbstractDateValue" ], function(stjs, AbstractDateValue) {
	var DateLaterThanValue = function(value) {
		AbstractDateValue.call(this, value);
	};
	stjs.extend(DateLaterThanValue, AbstractDateValue, [ AbstractDateValue ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.DateLaterThanValue';
		constructor.serialVersionUID = 1;
		prototype.toString = function() {
			return "later than '" + this.getValue() + "'";
		};
	}, {});
	return DateLaterThanValue;
})