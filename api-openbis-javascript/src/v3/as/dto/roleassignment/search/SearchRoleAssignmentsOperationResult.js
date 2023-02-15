define([ "stjs", "as/dto/common/search/SearchObjectsOperationResult" ], function(stjs, SearchObjectsOperationResult) {
	var SearchRoleAssignmentsOperationResult = function(searchResult) {
		SearchObjectsOperationResult.call(this, searchResult);
	};
	stjs.extend(SearchRoleAssignmentsOperationResult, SearchObjectsOperationResult, [ SearchObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.roleassignment.search.SearchRoleAssignmentsOperationResult';
		prototype.getMessage = function() {
			return "SearchRoleAssignmentsOperationResult";
		};
	}, {});
	return SearchRoleAssignmentsOperationResult;
})
