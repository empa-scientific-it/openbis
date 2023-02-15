define([ "require", "stjs", "as/dto/common/fetchoptions/EntityWithPropertiesSortOptions" ], function(require, stjs, EntityWithPropertiesSortOptions) {
	var MaterialSortOptions = function() {
		EntityWithPropertiesSortOptions.call(this);
	};
	stjs.extend(MaterialSortOptions, EntityWithPropertiesSortOptions, [ EntityWithPropertiesSortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.material.fetchoptions.MaterialSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return MaterialSortOptions;
})