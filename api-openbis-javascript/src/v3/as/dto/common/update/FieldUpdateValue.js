/**
 * Represents value together with {@code isModified} flag
 * 
 * @author Jakub Straszewski
 */
define([ "stjs" ], function(stjs) {
	var FieldUpdateValue = function() {
	};
	stjs.extend(FieldUpdateValue, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.update.FieldUpdateValue';
		constructor.serialVersionUID = 1;
		prototype.isModified = false;
		prototype.value = null;
		/**
		 * value for update
		 */
		prototype.setValue = function(value) {
			this.value = value;
			this.isModified = true;
		};
		/**
		 * @return {@code true} if the value has been set for update.
		 */
		prototype.isModified = function() {
			return this.isModified;
		};
		/**
		 * @return value for update
		 */
		prototype.getValue = function() {
			return this.value;
		};
	}, {
		value: function(jsonTypeArguments){
		    if(jsonTypeArguments && jsonTypeArguments.length > 0){
		        return jsonTypeArguments[0];
		    } else {
		        return "Object"
		    }
		}
	});
	return FieldUpdateValue;
})