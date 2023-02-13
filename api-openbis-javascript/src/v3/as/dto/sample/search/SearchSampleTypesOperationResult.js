/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperationResult" ], function(stjs, SearchObjectsOperationResult) {
	var SearchSampleTypesOperationResult = function(searchResult) {
		SearchObjectsOperationResult.call(this, searchResult);
	};
	stjs.extend(SearchSampleTypesOperationResult, SearchObjectsOperationResult, [ SearchObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.search.SearchSampleTypesOperationResult';
		prototype.getMessage = function() {
			return "SearchSampleTypesOperationResult";
		};
	}, {});
	return SearchSampleTypesOperationResult;
})