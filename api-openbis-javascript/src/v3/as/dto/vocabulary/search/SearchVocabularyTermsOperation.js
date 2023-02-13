/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/SearchObjectsOperation" ], function(stjs, SearchObjectsOperation) {
	var SearchVocabularyTermsOperation = function(criteria, fetchOptions) {
		SearchObjectsOperation.call(this, criteria, fetchOptions);
	};
	stjs.extend(SearchVocabularyTermsOperation, SearchObjectsOperation, [ SearchObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.search.SearchVocabularyTermsOperation';
		prototype.getMessage = function() {
			return "SearchVocabularyTermsOperation";
		};
	}, {});
	return SearchVocabularyTermsOperation;
})