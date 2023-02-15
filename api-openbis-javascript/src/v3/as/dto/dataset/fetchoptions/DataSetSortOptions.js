define([ "require", "stjs", "as/dto/common/fetchoptions/EntityWithPropertiesSortOptions" ], function(require, stjs, EntityWithPropertiesSortOptions) {
	var DataSetSortOptions = function() {
		EntityWithPropertiesSortOptions.call(this);
	};
	stjs.extend(DataSetSortOptions, EntityWithPropertiesSortOptions, [ EntityWithPropertiesSortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.fetchoptions.DataSetSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return DataSetSortOptions;
})