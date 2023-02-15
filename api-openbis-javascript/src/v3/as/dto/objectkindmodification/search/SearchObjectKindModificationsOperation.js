/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperation" ], function(stjs, SearchObjectsOperation) {
	var SearchObjectKindModificationsOperation = function(criteria, fetchOptions) {
		SearchObjectsOperation.call(this, criteria, fetchOptions);
	};
	stjs.extend(SearchObjectKindModificationsOperation, SearchObjectsOperation, [ SearchObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.objectkindmodification.search.SearchObjectKindModificationsOperation';
		prototype.getMessage = function() {
			return "SearchObjectKindModificationsOperation";
		};
	}, {});
	return SearchObjectKindModificationsOperation;
})