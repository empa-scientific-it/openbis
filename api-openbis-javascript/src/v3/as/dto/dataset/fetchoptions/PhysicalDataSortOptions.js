define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var PhysicalDataSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(PhysicalDataSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.fetchoptions.PhysicalDataSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return PhysicalDataSortOptions;
})