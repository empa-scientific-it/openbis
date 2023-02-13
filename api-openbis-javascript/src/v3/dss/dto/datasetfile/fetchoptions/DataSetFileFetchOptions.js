define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", "dss/dto/datasetfile/fetchoptions/DataSetFileSortOptions" ], function(require, stjs, FetchOptions) {
	var DataSetFileFetchOptions = function() {
	};
	stjs.extend(DataSetFileFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dss.dto.datasetfile.fetchoptions.DataSetFileFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.sort = null;
		prototype.sortBy = function() {
			if (this.sort == null) {
				var DataSetFileSortOptions = require("dss/dto/datasetfile/fetchoptions/DataSetFileSortOptions");
				this.sort = new DataSetFileSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		sort : "DataSetFileSortOptions"
	});
	return DataSetFileFetchOptions;
})