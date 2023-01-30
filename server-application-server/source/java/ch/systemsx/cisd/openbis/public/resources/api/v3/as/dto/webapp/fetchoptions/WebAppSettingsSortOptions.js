define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var WebAppSettingsSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(WebAppSettingsSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.webapp.fetchoptions.WebAppSettingsSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return WebAppSettingsSortOptions;
})