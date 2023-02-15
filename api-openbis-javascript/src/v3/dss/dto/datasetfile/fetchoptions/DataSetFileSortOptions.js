define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var DataSetFileSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(DataSetFileSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dss.dto.datasetfile.fetchoptions.DataSetFileSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return DataSetFileSortOptions;
})