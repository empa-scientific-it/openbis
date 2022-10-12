/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperation" ], function(stjs, SearchObjectsOperation) {
	var SearchSessionInformationOperation = function(criteria, fetchOptions) {
		SearchObjectsOperation.call(this, criteria, fetchOptions);
	};
	stjs.extend(SearchSessionInformationOperation, SearchObjectsOperation, [ SearchObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.session.search.SearchSessionInformationOperation';
		prototype.getMessage = function() {
			return "SearchSessionInformationOperation";
		};
	}, {});
	return SearchSessionInformationOperation;
})