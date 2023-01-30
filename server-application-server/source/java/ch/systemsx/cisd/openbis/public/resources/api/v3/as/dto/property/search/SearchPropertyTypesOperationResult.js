/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperationResult" ], function(stjs, SearchObjectsOperationResult) {
	var SearchPropertyTypesOperationResult = function(searchResult) {
		SearchObjectsOperationResult.call(this, searchResult);
	};
	stjs.extend(SearchPropertyTypesOperationResult, SearchObjectsOperationResult, [ SearchObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.search.SearchPropertyTypesOperationResult';
		prototype.getMessage = function() {
			return "SearchPropertyTypesOperationResult";
		};
	}, {});
	return SearchPropertyTypesOperationResult;
})