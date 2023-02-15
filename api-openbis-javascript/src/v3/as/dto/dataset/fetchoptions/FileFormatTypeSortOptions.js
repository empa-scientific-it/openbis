/*
 * @Deprecated
 */
define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var FileFormatTypeSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(FileFormatTypeSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.fetchoptions.FileFormatTypeSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return FileFormatTypeSortOptions;
})