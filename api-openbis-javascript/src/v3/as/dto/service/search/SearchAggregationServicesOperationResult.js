define([ "stjs", "as/dto/common/search/SearchObjectsOperationResult" ], function(stjs, SearchObjectsOperationResult) {
	var SearchAggregationServicesOperationResult = function(searchResult) {
		SearchObjectsOperationResult.call(this, searchResult);
	};
	stjs.extend(SearchAggregationServicesOperationResult, SearchObjectsOperationResult, [ SearchObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.search.SearchAggregationServicesOperationResult';
		prototype.getMessage = function() {
			return "SearchAggregationServicesOperationResult";
		};
	}, {});
	return SearchAggregationServicesOperationResult;
})
