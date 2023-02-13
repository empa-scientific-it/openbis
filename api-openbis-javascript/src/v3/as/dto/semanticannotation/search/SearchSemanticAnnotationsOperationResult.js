/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperationResult" ], function(stjs, SearchObjectsOperationResult) {
	var SearchSemanticAnnotationsOperationResult = function(searchResult) {
		SearchObjectsOperationResult.call(this, searchResult);
	};
	stjs.extend(SearchSemanticAnnotationsOperationResult, SearchObjectsOperationResult, [ SearchObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.semanticannotation.search.SearchSemanticAnnotationsOperationResult';
		prototype.getMessage = function() {
			return "SearchSemanticAnnotationsOperationResult";
		};
	}, {});
	return SearchSemanticAnnotationsOperationResult;
})