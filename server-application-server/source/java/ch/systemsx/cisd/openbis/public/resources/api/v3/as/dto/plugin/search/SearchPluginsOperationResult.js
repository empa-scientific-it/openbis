define([ "stjs", "as/dto/common/search/SearchObjectsOperationResult" ], function(stjs, SearchObjectsOperationResult) {
	var SearchPluginsOperationResult = function(searchResult) {
		SearchObjectsOperationResult.call(this, searchResult);
	};
	stjs.extend(SearchPluginsOperationResult, SearchObjectsOperationResult, [ SearchObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.plugin.search.SearchPluginsOperationResult';
		prototype.getMessage = function() {
			return "SearchPluginsOperationResult";
		};
	}, {});
	return SearchPluginsOperationResult;
})
