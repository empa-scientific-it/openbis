define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var EntityTypeSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(EntityTypeSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.entitytype.fetchoptions.EntityTypeSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return EntityTypeSortOptions;
})