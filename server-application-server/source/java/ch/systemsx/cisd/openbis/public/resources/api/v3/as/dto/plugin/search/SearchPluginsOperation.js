define([ "stjs", "as/dto/common/search/SearchObjectsOperation" ], function(stjs, SearchObjectsOperation) {
	var SearchPluginsOperation = function(criteria, fetchOptions) {
		SearchObjectsOperation.call(this, criteria, fetchOptions);
	};
	stjs.extend(SearchPluginsOperation, SearchObjectsOperation, [ SearchObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.plugin.search.SearchPluginsOperation';
		prototype.getMessage = function() {
			return "SearchPluginsOperation";
		};
	}, {});
	return SearchPluginsOperation;
})
