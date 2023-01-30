/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperationResult" ], function(stjs, SearchObjectsOperationResult) {
	var SearchSamplesOperationResult = function(searchResult) {
		SearchObjectsOperationResult.call(this, searchResult);
	};
	stjs.extend(SearchSamplesOperationResult, SearchObjectsOperationResult, [ SearchObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.search.SearchSamplesOperationResult';
		prototype.getMessage = function() {
			return "SearchSamplesOperationResult";
		};
	}, {});
	return SearchSamplesOperationResult;
})