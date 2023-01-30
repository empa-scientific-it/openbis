/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/delete/DeleteObjectsWithoutTrashOperationResult" ], function(stjs, DeleteObjectsWithoutTrashOperationResult) {
	var DeleteVocabularyTermsOperationResult = function() {
		DeleteObjectsWithoutTrashOperationResult.call(this);
	};
	stjs.extend(DeleteVocabularyTermsOperationResult, DeleteObjectsWithoutTrashOperationResult, [ DeleteObjectsWithoutTrashOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.delete.DeleteVocabularyTermsOperationResult';
		prototype.getMessage = function() {
			return "DeleteVocabularyTermsOperationResult";
		};
	}, {});
	return DeleteVocabularyTermsOperationResult;
})