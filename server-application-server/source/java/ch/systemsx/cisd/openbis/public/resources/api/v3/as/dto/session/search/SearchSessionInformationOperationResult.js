/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperationResult" ], function(stjs, SearchObjectsOperationResult) {
	var SearchSessionInformationOperationResult = function(searchResult) {
		SearchObjectsOperationResult.call(this, searchResult);
	};
	stjs.extend(SearchSessionInformationOperationResult, SearchObjectsOperationResult, [ SearchObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.session.search.SearchSessionInformationOperationResult';
		prototype.getMessage = function() {
			return "SearchSessionInformationOperationResult";
		};
	}, {});
	return SearchSessionInformationOperationResult;
})