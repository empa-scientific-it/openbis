/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/AbstractDateValue" ], function(stjs, AbstractDateValue) {
	var DateEarlierThanOrEqualToValue = function(value) {
		AbstractDateValue.call(this, value);
	};
	stjs.extend(DateEarlierThanOrEqualToValue, AbstractDateValue, [ AbstractDateValue ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.DateEarlierThanOrEqualToValue';
		constructor.serialVersionUID = 1;
		prototype.toString = function() {
			return "earlier than or equal to '" + this.getValue() + "'";
		};
	}, {});
	return DateEarlierThanOrEqualToValue;
})