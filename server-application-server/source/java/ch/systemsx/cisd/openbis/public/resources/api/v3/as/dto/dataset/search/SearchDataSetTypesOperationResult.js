/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperationResult" ], function(stjs, SearchObjectsOperationResult) {
	var SearchDataSetTypesOperationResult = function(searchResult) {
		SearchObjectsOperationResult.call(this, searchResult);
	};
	stjs.extend(SearchDataSetTypesOperationResult, SearchObjectsOperationResult, [ SearchObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.search.SearchDataSetTypesOperationResult';
		prototype.getMessage = function() {
			return "SearchDataSetTypesOperationResult";
		};
	}, {});
	return SearchDataSetTypesOperationResult;
})