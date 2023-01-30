define([ "stjs", "as/dto/common/search/AbstractDateValue" ], function(stjs, AbstractDateValue) {
	var DateEarlierThanValue = function(value) {
		AbstractDateValue.call(this, value);
	};
	stjs.extend(DateEarlierThanValue, AbstractDateValue, [ AbstractDateValue ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.DateEarlierThanValue';
		constructor.serialVersionUID = 1;
		prototype.toString = function() {
			return "earlier than '" + this.getValue() + "'";
		};
	}, {});
	return DateEarlierThanValue;
})