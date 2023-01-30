/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperation" ], function(stjs, SearchObjectsOperation) {
	var SearchCustomASServicesOperation = function(criteria, fetchOptions) {
		SearchObjectsOperation.call(this, criteria, fetchOptions);
	};
	stjs.extend(SearchCustomASServicesOperation, SearchObjectsOperation, [ SearchObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.search.SearchCustomASServicesOperation';
		prototype.getMessage = function() {
			return "SearchCustomASServicesOperation";
		};
	}, {});
	return SearchCustomASServicesOperation;
})