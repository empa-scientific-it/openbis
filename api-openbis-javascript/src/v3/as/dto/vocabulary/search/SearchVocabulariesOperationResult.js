define([ "stjs", "as/dto/common/search/SearchObjectsOperationResult" ], function(stjs, SearchObjectsOperationResult) {
	var SearchVocabulariesOperationResult = function(searchResult) {
		SearchObjectsOperationResult.call(this, searchResult);
	};
	stjs.extend(SearchVocabulariesOperationResult, SearchObjectsOperationResult, [ SearchObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.search.SearchVocabulariesOperationResult';
		prototype.getMessage = function() {
			return "SearchVocabulariesOperationResult";
		};
	}, {});
	return SearchVocabulariesOperationResult;
})
