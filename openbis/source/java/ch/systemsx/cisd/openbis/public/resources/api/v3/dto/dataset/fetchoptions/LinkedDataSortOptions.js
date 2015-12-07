define([ "require", "stjs", "dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var LinkedDataSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(LinkedDataSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.dataset.fetchoptions.LinkedDataSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return LinkedDataSortOptions;
})