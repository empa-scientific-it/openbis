/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperationResult" ], function(stjs, SearchObjectsOperationResult) {
	var SearchPersonalAccessTokensOperationResult = function(searchResult) {
		SearchObjectsOperationResult.call(this, searchResult);
	};
	stjs.extend(SearchPersonalAccessTokensOperationResult, SearchObjectsOperationResult, [ SearchObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.pat.search.SearchPersonalAccessTokensOperationResult';
		prototype.getMessage = function() {
			return "SearchPersonalAccessTokensOperationResult";
		};
	}, {});
	return SearchPersonalAccessTokensOperationResult;
})