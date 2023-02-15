/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperationResult" ], function(stjs, SearchObjectsOperationResult) {
	var SearchCustomASServicesOperationResult = function(searchResult) {
		SearchObjectsOperationResult.call(this, searchResult);
	};
	stjs.extend(SearchCustomASServicesOperationResult, SearchObjectsOperationResult, [ SearchObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.search.SearchCustomASServicesOperationResult';
		prototype.getMessage = function() {
			return "SearchCustomASServicesOperationResult";
		};
	}, {});
	return SearchCustomASServicesOperationResult;
})