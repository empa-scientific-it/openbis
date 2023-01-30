/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperationResult" ], function(stjs, SearchObjectsOperationResult) {
	var SearchMaterialsOperationResult = function(searchResult) {
		SearchObjectsOperationResult.call(this, searchResult);
	};
	stjs.extend(SearchMaterialsOperationResult, SearchObjectsOperationResult, [ SearchObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.material.search.SearchMaterialsOperationResult';
		prototype.getMessage = function() {
			return "SearchMaterialsOperationResult";
		};
	}, {});
	return SearchMaterialsOperationResult;
})