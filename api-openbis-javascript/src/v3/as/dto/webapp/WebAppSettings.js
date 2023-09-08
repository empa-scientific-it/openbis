define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var WebAppSettings = function() {
	};
	stjs.extend(WebAppSettings, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.webapp.WebAppSettings';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.webAppId = null;
		prototype.settings = null;

		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};

		prototype.getWebAppId = function() {
			return this.webAppId;
		};
		prototype.setWebAppId = function(webAppId) {
			this.webAppId = webAppId;
		};

		prototype.getSetting = function(setting) {
			if (this.getFetchOptions() && (this.getFetchOptions().hasAllSettings() || this.getFetchOptions().hasSetting(setting))) {
				return this.settings ? this.settings[setting] : null;
			} else {
				throw new exceptions.NotFetchedException("Setting '" + setting + "' has not been fetched.");
			}
		};

		prototype.getSettings = function() {
			if (this.getFetchOptions() && (this.getFetchOptions().hasAllSettings() || (this.getFetchOptions().getSettings() && Object.keys(this.getFetchOptions().getSettings()).length > 0))) {
				return this.settings;
			} else {
				throw new exceptions.NotFetchedException("Settings have not been fetched.");
			}
		};

		prototype.setSettings = function(settings) {
			this.settings = settings;
		};
	}, {
		fetchOptions : "WebAppSettingsFetchOptions",
		settings : {
			name : "Map",
			arguments : [ "String", "WebAppSetting" ]
		}
	});
	return WebAppSettings;
})