/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperationResult" ], function(stjs, SearchObjectsOperationResult) {
	var SearchObjectKindModificationsOperationResult = function(searchResult) {
		SearchObjectsOperationResult.call(this, searchResult);
	};
	stjs.extend(SearchObjectKindModificationsOperationResult, SearchObjectsOperationResult, [ SearchObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.objectkindmodification.search.SearchObjectKindModificationsOperationResult';
		prototype.getMessage = function() {
			return "SearchObjectKindModificationsOperationResult";
		};
	}, {});
	return SearchObjectKindModificationsOperationResult;
})