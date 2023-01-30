/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperation" ], function(stjs, SearchObjectsOperation) {
	var SearchDataStoresOperation = function(criteria, fetchOptions) {
		SearchObjectsOperation.call(this, criteria, fetchOptions);
	};
	stjs.extend(SearchDataStoresOperation, SearchObjectsOperation, [ SearchObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.datastore.search.SearchDataStoresOperation';
		prototype.getMessage = function() {
			return "SearchDataStoresOperation";
		};
	}, {});
	return SearchDataStoresOperation;
})