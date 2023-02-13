/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperationResult" ], function(stjs, SearchObjectsOperationResult) {
	var SearchExternalDmsOperationResult = function(searchResult) {
		SearchObjectsOperationResult.call(this, searchResult);
	};
	stjs.extend(SearchExternalDmsOperationResult, SearchObjectsOperationResult, [ SearchObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.externaldms.search.SearchExternalDmsOperationResult';
		prototype.getMessage = function() {
			return "SearchExternalDmsOperationResult";
		};
	}, {});
	return SearchExternalDmsOperationResult;
})