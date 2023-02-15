/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/get/GetObjectsOperation" ], function(stjs, GetObjectsOperation) {
	var GetVocabularyTermsOperation = function(objectIds, fetchOptions) {
		GetObjectsOperation.call(this, objectIds, fetchOptions);
	};
	stjs.extend(GetVocabularyTermsOperation, GetObjectsOperation, [ GetObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.get.GetVocabularyTermsOperation';
		prototype.getMessage = function() {
			return "GetVocabularyTermsOperation";
		};
	}, {});
	return GetVocabularyTermsOperation;
})