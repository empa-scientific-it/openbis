/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperation" ], function(stjs, SearchObjectsOperation) {
	var SearchExternalDmsOperation = function(criteria, fetchOptions) {
		SearchObjectsOperation.call(this, criteria, fetchOptions);
	};
	stjs.extend(SearchExternalDmsOperation, SearchObjectsOperation, [ SearchObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.externaldms.search.SearchExternalDmsOperation';
		prototype.getMessage = function() {
			return "SearchExternalDmsOperation";
		};
	}, {});
	return SearchExternalDmsOperation;
})