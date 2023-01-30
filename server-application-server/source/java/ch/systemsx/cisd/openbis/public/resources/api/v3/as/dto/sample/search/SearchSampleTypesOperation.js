/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperation" ], function(stjs, SearchObjectsOperation) {
	var SearchSampleTypesOperation = function(criteria, fetchOptions) {
		SearchObjectsOperation.call(this, criteria, fetchOptions);
	};
	stjs.extend(SearchSampleTypesOperation, SearchObjectsOperation, [ SearchObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.search.SearchSampleTypesOperation';
		prototype.getMessage = function() {
			return "SearchSampleTypesOperation";
		};
	}, {});
	return SearchSampleTypesOperation;
})