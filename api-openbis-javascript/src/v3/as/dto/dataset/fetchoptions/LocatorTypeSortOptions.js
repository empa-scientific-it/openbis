define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var LocatorTypeSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(LocatorTypeSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.fetchoptions.LocatorTypeSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return LocatorTypeSortOptions;
})