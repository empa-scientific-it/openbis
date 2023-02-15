define([ "stjs", "as/dto/common/search/SearchObjectsOperationResult" ], function(stjs, SearchObjectsOperationResult) {
	var SearchReportingServicesOperationResult = function(searchResult) {
		SearchObjectsOperationResult.call(this, searchResult);
	};
	stjs.extend(SearchReportingServicesOperationResult, SearchObjectsOperationResult, [ SearchObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.search.SearchReportingServicesOperationResult';
		prototype.getMessage = function() {
			return "SearchReportingServicesOperationResult";
		};
	}, {});
	return SearchReportingServicesOperationResult;
})
