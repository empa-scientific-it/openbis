define([ "stjs", "as/dto/common/search/SearchObjectsOperation" ], function(stjs, SearchObjectsOperation) {
	var SearchProcessingServicesOperation = function(criteria, fetchOptions) {
		SearchObjectsOperation.call(this, criteria, fetchOptions);
	};
	stjs.extend(SearchProcessingServicesOperation, SearchObjectsOperation, [ SearchObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.search.SearchProcessingServicesOperation';
		prototype.getMessage = function() {
			return "SearchProcessingServicesOperation";
		};
	}, {});
	return SearchProcessingServicesOperation;
})
