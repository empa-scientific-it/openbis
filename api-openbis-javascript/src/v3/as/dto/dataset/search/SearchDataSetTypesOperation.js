/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperation" ], function(stjs, SearchObjectsOperation) {
	var SearchDataSetTypesOperation = function(criteria, fetchOptions) {
		SearchObjectsOperation.call(this, criteria, fetchOptions);
	};
	stjs.extend(SearchDataSetTypesOperation, SearchObjectsOperation, [ SearchObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.search.SearchDataSetTypesOperation';
		prototype.getMessage = function() {
			return "SearchDataSetTypesOperation";
		};
	}, {});
	return SearchDataSetTypesOperation;
})