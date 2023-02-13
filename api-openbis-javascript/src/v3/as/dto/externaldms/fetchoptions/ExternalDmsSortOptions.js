define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var ExternalDmsSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(ExternalDmsSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.externaldms.fetchoptions.ExternalDmsSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return ExternalDmsSortOptions;
})