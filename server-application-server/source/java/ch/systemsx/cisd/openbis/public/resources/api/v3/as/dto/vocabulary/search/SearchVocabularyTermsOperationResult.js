/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperationResult" ], function(stjs, SearchObjectsOperationResult) {
	var SearchVocabularyTermsOperationResult = function(searchResult) {
		SearchObjectsOperationResult.call(this, searchResult);
	};
	stjs.extend(SearchVocabularyTermsOperationResult, SearchObjectsOperationResult, [ SearchObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.search.SearchVocabularyTermsOperationResult';
		prototype.getMessage = function() {
			return "SearchVocabularyTermsOperationResult";
		};
	}, {});
	return SearchVocabularyTermsOperationResult;
})