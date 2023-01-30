define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/webapp/fetchoptions/WebAppSettingsSortOptions" ], function(require, stjs, FetchOptions, WebAppSettingsSortOptions) {
	var WebAppSettingsFetchOptions = function() {
	};
	stjs.extend(WebAppSettingsFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.webapp.fetchoptions.WebAppSettingsFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.settings = null;
		prototype.allSettings = false;
		prototype.sort = null;

		prototype.withSetting = function(setting) {
			if (this.settings == null) {
				this.settings = [];
			}

			this.settings.push(setting);
		};
		prototype.hasSetting = function(setting) {
			return this.settings != null && this.settings.indexOf(setting) != -1;
		};
		prototype.withSettingsUsing = function(settings) {
			return this.settings = settings;
		};
		prototype.getSettings = function() {
			return this.settings;
		};
		prototype.setSettings = function(settings) {
			this.settings = settings;
		};
		prototype.withAllSettings = function() {
			this.allSettings = true;
		};
		prototype.hasAllSettings = function() {
			return this.allSettings;
		};
		prototype.setAllSettings = function(allSettings) {
			this.allSettings = allSettings;
		};
		prototype.withAllSettingsUsing = function(allSettings) {
			this.allSettings = allSettings;
		};
		prototype.sortBy = function() {
			if (this.sort == null) {
				this.sort = new WebAppSettingsSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		settings : {
			name : "Collection",
			arguments : [ "String" ]
		},
		sort : "WebAppSettingsSortOptions"
	});
	return WebAppSettingsFetchOptions;
})