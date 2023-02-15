define([ "stjs", "as/dto/common/search/SearchObjectsOperationResult" ], function(stjs, SearchObjectsOperationResult) {
	var SearchProcessingServicesOperationResult = function(searchResult) {
		SearchObjectsOperationResult.call(this, searchResult);
	};
	stjs.extend(SearchProcessingServicesOperationResult, SearchObjectsOperationResult, [ SearchObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.search.SearchProcessingServicesOperationResult';
		prototype.getMessage = function() {
			return "SearchProcessingServicesOperationResult";
		};
	}, {});
	return SearchProcessingServicesOperationResult;
})
