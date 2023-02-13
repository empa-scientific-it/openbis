/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperation" ], function(stjs, SearchObjectsOperation) {
	var SearchSemanticAnnotationsOperation = function(criteria, fetchOptions) {
		SearchObjectsOperation.call(this, criteria, fetchOptions);
	};
	stjs.extend(SearchSemanticAnnotationsOperation, SearchObjectsOperation, [ SearchObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.semanticannotation.search.SearchSemanticAnnotationsOperation';
		prototype.getMessage = function() {
			return "SearchSemanticAnnotationsOperation";
		};
	}, {});
	return SearchSemanticAnnotationsOperation;
})