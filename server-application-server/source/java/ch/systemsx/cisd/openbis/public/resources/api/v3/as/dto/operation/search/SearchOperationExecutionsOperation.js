/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperation" ], function(stjs, SearchObjectsOperation) {
	var SearchOperationExecutionsOperation = function(criteria, fetchOptions) {
		SearchObjectsOperation.call(this, criteria, fetchOptions);
	};
	stjs.extend(SearchOperationExecutionsOperation, SearchObjectsOperation, [ SearchObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.operation.search.SearchOperationExecutionsOperation';
		prototype.getMessage = function() {
			return "SearchOperationExecutionsOperation";
		};
	}, {});
	return SearchOperationExecutionsOperation;
})