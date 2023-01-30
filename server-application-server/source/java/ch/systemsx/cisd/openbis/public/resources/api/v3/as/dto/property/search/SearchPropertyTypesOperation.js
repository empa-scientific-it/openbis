/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperation" ], function(stjs, SearchObjectsOperation) {
	var SearchPropertyTypesOperation = function(criteria, fetchOptions) {
		SearchObjectsOperation.call(this, criteria, fetchOptions);
	};
	stjs.extend(SearchPropertyTypesOperation, SearchObjectsOperation, [ SearchObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.search.SearchPropertyTypesOperation';
		prototype.getMessage = function() {
			return "SearchPropertyTypesOperation";
		};
	}, {});
	return SearchPropertyTypesOperation;
})