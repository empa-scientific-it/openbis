/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/ListUpdateValue" ], function(stjs, ListUpdateValue) {
	var WebAppSettingsUpdateValue = function() {
		ListUpdateValue.call(this);
	};
	stjs.extend(WebAppSettingsUpdateValue, ListUpdateValue, [ ListUpdateValue ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.webapp.update.WebAppSettingsUpdateValue';
		constructor.serialVersionUID = 1;
	}, {
		actions : {
			name : "List",
			arguments : [ {
				name : "ListUpdateValue.ListUpdateAction",
				arguments : [ "ACTION" ]
			} ]
		}
	});
	return WebAppSettingsUpdateValue;
})