/**
 * @author pkupczyk
 */
define([ "stjs" ], function(stjs) {
	var WebAppSettingCreation = function(name, value) {
		this.setName(name);
		this.setValue(value);
	};
	stjs.extend(WebAppSettingCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.webapp.create.WebAppSettingCreation';
		constructor.serialVersionUID = 1;
		prototype.name = null;
		prototype.value = null;

		prototype.getName = function() {
			return this.name;
		};
		prototype.setName = function(name) {
			this.name = name;
		};
		prototype.getValue = function() {
			return this.value;
		};
		prototype.setValue = function(value) {
			this.value = value;
		};
	}, {});
	return WebAppSettingCreation;
})