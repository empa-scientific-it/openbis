/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
/*
 * @Deprecated
 */
define([ "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/dataset/fetchoptions/FileFormatTypeSortOptions" ], function(stjs, FetchOptions) {
	var FileFormatTypeFetchOptions = function() {
	};
	stjs.extend(FileFormatTypeFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.fetchoptions.FileFormatTypeFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.sort = null;
		prototype.sortBy = function() {
			if (this.sort == null) {
				var FileFormatTypeSortOptions = require("as/dto/dataset/fetchoptions/FileFormatTypeSortOptions");
				this.sort = new FileFormatTypeSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		sort : "FileFormatTypeSortOptions"
	});
	return FileFormatTypeFetchOptions;
})