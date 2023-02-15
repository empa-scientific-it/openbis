define([ "stjs", "as/dto/common/search/SearchObjectsOperation" ], function(stjs, SearchObjectsOperation) {
	var SearchAggregationServicesOperation = function(criteria, fetchOptions) {
		SearchObjectsOperation.call(this, criteria, fetchOptions);
	};
	stjs.extend(SearchAggregationServicesOperation, SearchObjectsOperation, [ SearchObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.search.SearchAggregationServicesOperation';
		prototype.getMessage = function() {
			return "SearchAggregationServicesOperation";
		};
	}, {});
	return SearchAggregationServicesOperation;
})
