/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperationResult" ], function(stjs, SearchObjectsOperationResult) {
	var SearchMaterialTypesOperationResult = function(searchResult) {
		SearchObjectsOperationResult.call(this, searchResult);
	};
	stjs.extend(SearchMaterialTypesOperationResult, SearchObjectsOperationResult, [ SearchObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.material.search.SearchMaterialTypesOperationResult';
		prototype.getMessage = function() {
			return "SearchMaterialTypesOperationResult";
		};
	}, {});
	return SearchMaterialTypesOperationResult;
})