/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperation" ], function(stjs, SearchObjectsOperation) {
	var SearchPersonalAccessTokensOperation = function(criteria, fetchOptions) {
		SearchObjectsOperation.call(this, criteria, fetchOptions);
	};
	stjs.extend(SearchPersonalAccessTokensOperation, SearchObjectsOperation, [ SearchObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.pat.search.SearchPersonalAccessTokensOperation';
		prototype.getMessage = function() {
			return "SearchPersonalAccessTokensOperation";
		};
	}, {});
	return SearchPersonalAccessTokensOperation;
})