define([ "require", "stjs", "dto/fetchoptions/sort/EntityWithPropertiesSortOptions" ], function(require, stjs, EntityWithPropertiesSortOptions) {
	var DataSetSortOptions = function() {
		EntityWithPropertiesSortOptions.call(this);
	};
	stjs.extend(DataSetSortOptions, EntityWithPropertiesSortOptions, [ EntityWithPropertiesSortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.dataset.DataSetSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return DataSetSortOptions;
})