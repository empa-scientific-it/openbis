/**
 * @author pkupczyk
 */
define([ "stjs" ], function(stjs) {
	var AbstractValue = function(value) {
		this.value = value;
	};
	stjs.extend(AbstractValue, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.AbstractValue';
		constructor.serialVersionUID = 1;
		prototype.value = null;
		prototype.getValue = function() {
			return this.value;
		};
		prototype.setValue = function(value) {
			this.value = value;
		};
		prototype.toString = function() {
			if (this.getValue() != null) {
				return this.getValue().toString();
			} else {
				return null;
			}
		};
	}, {});
	return AbstractValue;
})