/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/get/GetObjectsOperationResult" ], function(stjs, GetObjectsOperationResult) {
	var GetVocabularyTermsOperationResult = function(objectMap) {
		GetObjectsOperationResult.call(this, objectMap);
	};
	stjs.extend(GetVocabularyTermsOperationResult, GetObjectsOperationResult, [ GetObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.get.GetVocabularyTermsOperationResult';
		prototype.getMessage = function() {
			return "GetVocabularyTermsOperationResult";
		};
	}, {});
	return GetVocabularyTermsOperationResult;
})