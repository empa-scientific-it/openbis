/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperationResult" ], function(stjs, SearchObjectsOperationResult) {
	var SearchDeletionsOperationResult = function(searchResult) {
		SearchObjectsOperationResult.call(this, searchResult);
	};
	stjs.extend(SearchDeletionsOperationResult, SearchObjectsOperationResult, [ SearchObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.deletion.search.SearchDeletionsOperationResult';
		prototype.getMessage = function() {
			return "SearchDeletionsOperationResult";
		};
	}, {});
	return SearchDeletionsOperationResult;
})