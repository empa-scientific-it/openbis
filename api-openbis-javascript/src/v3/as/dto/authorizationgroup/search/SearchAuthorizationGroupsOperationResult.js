define([ "stjs", "as/dto/common/search/SearchObjectsOperationResult" ], function(stjs, SearchObjectsOperationResult) {
	var SearchAuthorizationGroupsOperationResult = function(searchResult) {
		SearchObjectsOperationResult.call(this, searchResult);
	};
	stjs.extend(SearchAuthorizationGroupsOperationResult, SearchObjectsOperationResult, [ SearchObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.authorizationgroup.search.SearchAuthorizationGroupsOperationResult';
		prototype.getMessage = function() {
			return "SearchAuthorizationGroupsOperationResult";
		};
	}, {});
	return SearchAuthorizationGroupsOperationResult;
})