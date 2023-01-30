define([ "stjs", "as/dto/common/search/SearchObjectsOperation" ], function(stjs, SearchObjectsOperation) {
	var SearchRoleAssignmentsOperation = function(criteria, fetchOptions) {
		SearchObjectsOperation.call(this, criteria, fetchOptions);
	};
	stjs.extend(SearchRoleAssignmentsOperation, SearchObjectsOperation, [ SearchObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.roleassignment.search.SearchRoleAssignmentsOperation';
		prototype.getMessage = function() {
			return "SearchRoleAssignmentsOperation";
		};
	}, {});
	return SearchRoleAssignmentsOperation;
})
