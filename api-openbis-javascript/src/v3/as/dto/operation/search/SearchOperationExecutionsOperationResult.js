/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperationResult" ], function(stjs, SearchObjectsOperationResult) {
	var SearchOperationExecutionsOperationResult = function(searchResult) {
		SearchObjectsOperationResult.call(this, searchResult);
	};
	stjs.extend(SearchOperationExecutionsOperationResult, SearchObjectsOperationResult, [ SearchObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.operation.search.SearchOperationExecutionsOperationResult';
		prototype.getMessage = function() {
			return "SearchOperationExecutionsOperationResult";
		};
	}, {});
	return SearchOperationExecutionsOperationResult;
})