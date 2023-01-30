define([ "stjs", "as/dto/common/search/SearchObjectsOperation" ], function(stjs, SearchObjectsOperation) {
	var SearchReportingServicesOperation = function(criteria, fetchOptions) {
		SearchObjectsOperation.call(this, criteria, fetchOptions);
	};
	stjs.extend(SearchReportingServicesOperation, SearchObjectsOperation, [ SearchObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.search.SearchReportingServicesOperation';
		prototype.getMessage = function() {
			return "SearchReportingServicesOperation";
		};
	}, {});
	return SearchReportingServicesOperation;
})
