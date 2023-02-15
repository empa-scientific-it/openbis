/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/dataset/fetchoptions/StorageFormatSortOptions" ], function(stjs, FetchOptions) {
	var StorageFormatFetchOptions = function() {
	};
	stjs.extend(StorageFormatFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.fetchoptions.StorageFormatFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.sort = null;
		prototype.sortBy = function() {
			if (this.sort == null) {
				var StorageFormatSortOptions = require("as/dto/dataset/fetchoptions/StorageFormatSortOptions");
				this.sort = new StorageFormatSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		sort : "StorageFormatSortOptions"
	});
	return StorageFormatFetchOptions;
})