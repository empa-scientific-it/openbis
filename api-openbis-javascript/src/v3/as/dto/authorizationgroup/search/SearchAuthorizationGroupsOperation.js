define([ "stjs", "as/dto/common/search/SearchObjectsOperation" ], function(stjs, SearchObjectsOperation) {
	var SearchAuthorizationGroupsOperation = function(criteria, fetchOptions) {
		SearchObjectsOperation.call(this, criteria, fetchOptions);
	};
	stjs.extend(SearchAuthorizationGroupsOperation, SearchObjectsOperation, [ SearchObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.authorizationgroup.search.SearchAuthorizationGroupsOperation';
		prototype.getMessage = function() {
			return "SearchAuthorizationGroupsOperation";
		};
	}, {});
	return SearchAuthorizationGroupsOperation;
})
