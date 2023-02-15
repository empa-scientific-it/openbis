/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperationResult" ], function(stjs, SearchObjectsOperationResult) {
	var SearchDataSetsOperationResult = function(searchResult) {
		SearchObjectsOperationResult.call(this, searchResult);
	};
	stjs.extend(SearchDataSetsOperationResult, SearchObjectsOperationResult, [ SearchObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.search.SearchDataSetsOperationResult';
		prototype.getMessage = function() {
			return "SearchDataSetsOperationResult";
		};
	}, {});
	return SearchDataSetsOperationResult;
})