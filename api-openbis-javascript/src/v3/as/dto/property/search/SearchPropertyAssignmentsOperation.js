/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperation" ], function(stjs, SearchObjectsOperation) {
	var SearchPropertyAssignmentsOperation = function(criteria, fetchOptions) {
		SearchObjectsOperation.call(this, criteria, fetchOptions);
	};
	stjs.extend(SearchPropertyAssignmentsOperation, SearchObjectsOperation, [ SearchObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.search.SearchPropertyAssignmentsOperation';
		prototype.getMessage = function() {
			return "SearchPropertyAssignmentsOperation";
		};
	}, {});
	return SearchPropertyAssignmentsOperation;
})