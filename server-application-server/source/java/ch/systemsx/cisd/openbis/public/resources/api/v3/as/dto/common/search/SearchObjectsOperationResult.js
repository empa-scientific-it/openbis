/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var SearchObjectsOperationResult = function(searchResult) {
		this.searchResult = searchResult;
	};
	stjs.extend(SearchObjectsOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.SearchObjectsOperationResult';
		prototype.searchResult = null;
		prototype.getSearchResult = function() {
			return this.searchResult;
		};
		prototype.getMessage = function() {
			return "SearchObjectsOperationResult";
		};
	}, {
		searchResult : "SearchResult"
	});
	return SearchObjectsOperationResult;
})