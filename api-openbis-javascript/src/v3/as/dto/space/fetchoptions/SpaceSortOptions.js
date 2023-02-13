define([ "require", "stjs", "as/dto/common/fetchoptions/EntitySortOptions" ], function(require, stjs, EntitySortOptions) {
	var SpaceSortOptions = function() {
		EntitySortOptions.call(this);
	};
	stjs.extend(SpaceSortOptions, EntitySortOptions, [ EntitySortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.space.fetchoptions.SpaceSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return SpaceSortOptions;
})