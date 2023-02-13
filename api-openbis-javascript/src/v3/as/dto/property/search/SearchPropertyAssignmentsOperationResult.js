/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperationResult" ], function(stjs, SearchObjectsOperationResult) {
	var SearchPropertyAssignmentsOperationResult = function(searchResult) {
		SearchObjectsOperationResult.call(this, searchResult);
	};
	stjs.extend(SearchPropertyAssignmentsOperationResult, SearchObjectsOperationResult, [ SearchObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.search.SearchPropertyAssignmentsOperationResult';
		prototype.getMessage = function() {
			return "SearchPropertyAssignmentsOperationResult";
		};
	}, {});
	return SearchPropertyAssignmentsOperationResult;
})