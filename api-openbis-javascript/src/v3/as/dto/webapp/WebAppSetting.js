define([ "stjs" ], function(stjs) {
	var WebAppSetting = function(name, value) {
		this.setName(name);
		this.setValue(value);
	};
	stjs.extend(WebAppSetting, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.webapp.WebAppSetting';
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
	return WebAppSetting;
})