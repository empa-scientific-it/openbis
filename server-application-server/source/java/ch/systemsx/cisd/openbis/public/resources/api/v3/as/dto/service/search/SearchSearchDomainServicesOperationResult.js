define([ "stjs", "as/dto/common/search/SearchObjectsOperationResult" ], function(stjs, SearchObjectsOperationResult) {
	var SearchSearchDomainServicesOperationResult = function(searchResult) {
		SearchObjectsOperationResult.call(this, searchResult);
	};
	stjs.extend(SearchSearchDomainServicesOperationResult, SearchObjectsOperationResult, [ SearchObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.search.SearchSearchDomainServicesOperationResult';
		prototype.getMessage = function() {
			return "SearchSearchDomainServicesOperationResult";
		};
	}, {});
	return SearchSearchDomainServicesOperationResult;
})
