define([ "stjs", "as/dto/common/search/SearchObjectsOperation" ], function(stjs, SearchObjectsOperation) {
	var SearchSearchDomainServicesOperation = function(criteria, fetchOptions) {
		SearchObjectsOperation.call(this, criteria, fetchOptions);
	};
	stjs.extend(SearchSearchDomainServicesOperation, SearchObjectsOperation, [ SearchObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.search.SearchSearchDomainServicesOperation';
		prototype.getMessage = function() {
			return "SearchSearchDomainServicesOperation";
		};
	}, {});
	return SearchSearchDomainServicesOperation;
})
