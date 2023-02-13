/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperation" ], function(stjs, SearchObjectsOperation) {
	var SearchMaterialTypesOperation = function(criteria, fetchOptions) {
		SearchObjectsOperation.call(this, criteria, fetchOptions);
	};
	stjs.extend(SearchMaterialTypesOperation, SearchObjectsOperation, [ SearchObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.material.search.SearchMaterialTypesOperation';
		prototype.getMessage = function() {
			return "SearchMaterialTypesOperation";
		};
	}, {});
	return SearchMaterialTypesOperation;
})