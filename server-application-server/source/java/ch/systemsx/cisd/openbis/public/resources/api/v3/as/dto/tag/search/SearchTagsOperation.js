/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperation" ], function(stjs, SearchObjectsOperation) {
	var SearchTagsOperation = function(criteria, fetchOptions) {
		SearchObjectsOperation.call(this, criteria, fetchOptions);
	};
	stjs.extend(SearchTagsOperation, SearchObjectsOperation, [ SearchObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.tag.search.SearchTagsOperation';
		prototype.getMessage = function() {
			return "SearchTagsOperation";
		};
	}, {});
	return SearchTagsOperation;
})