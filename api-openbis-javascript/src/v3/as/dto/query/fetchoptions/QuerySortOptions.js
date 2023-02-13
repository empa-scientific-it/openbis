define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var QuerySortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(QuerySortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.query.fetchoptions.QuerySortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return QuerySortOptions;
})