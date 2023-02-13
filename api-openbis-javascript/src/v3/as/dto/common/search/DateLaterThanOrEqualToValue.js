/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/AbstractDateValue" ], function(stjs, AbstractDateValue) {
	var DateLaterThanOrEqualToValue = function(value) {
		AbstractDateValue.call(this, value);
	};
	stjs.extend(DateLaterThanOrEqualToValue, AbstractDateValue, [ AbstractDateValue ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.DateLaterThanOrEqualToValue';
		constructor.serialVersionUID = 1;
		prototype.toString = function() {
			return "later than or equal to '" + this.getValue() + "'";
		};
	}, {});
	return DateLaterThanOrEqualToValue;
})