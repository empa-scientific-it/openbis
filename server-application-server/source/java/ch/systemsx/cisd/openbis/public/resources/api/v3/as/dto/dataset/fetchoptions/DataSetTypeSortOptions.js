define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var DataSetTypeSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(DataSetTypeSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.fetchoptions.DataSetTypeSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return DataSetTypeSortOptions;
})