define([ "stjs", "as/dto/common/search/SearchObjectsOperation" ], function(stjs, SearchObjectsOperation) {
	var SearchVocabulariesOperation = function(criteria, fetchOptions) {
		SearchObjectsOperation.call(this, criteria, fetchOptions);
	};
	stjs.extend(SearchVocabulariesOperation, SearchObjectsOperation, [ SearchObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.search.SearchVocabulariesOperation';
		prototype.getMessage = function() {
			return "SearchVocabulariesOperation";
		};
	}, {});
	return SearchVocabulariesOperation;
})
