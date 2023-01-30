/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperationResult" ], function(stjs, SearchObjectsOperationResult) {
	var SearchDataStoresOperationResult = function(searchResult) {
		SearchObjectsOperationResult.call(this, searchResult);
	};
	stjs.extend(SearchDataStoresOperationResult, SearchObjectsOperationResult, [ SearchObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.datastore.search.SearchDataStoresOperationResult';
		prototype.getMessage = function() {
			return "SearchDataStoresOperationResult";
		};
	}, {});
	return SearchDataStoresOperationResult;
})