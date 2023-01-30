/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperation" ], function(stjs, SearchObjectsOperation) {
	var SearchDeletionsOperation = function(criteria, fetchOptions) {
		SearchObjectsOperation.call(this, criteria, fetchOptions);
	};
	stjs.extend(SearchDeletionsOperation, SearchObjectsOperation, [ SearchObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.deletion.search.SearchDeletionsOperation';
		prototype.getMessage = function() {
			return "SearchDeletionsOperation";
		};
	}, {});
	return SearchDeletionsOperation;
})