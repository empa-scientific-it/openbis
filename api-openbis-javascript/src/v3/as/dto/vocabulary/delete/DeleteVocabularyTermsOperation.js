/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/delete/DeleteObjectsOperation" ], function(stjs, DeleteObjectsOperation) {
	var DeleteVocabularyTermsOperation = function(objectIds, options) {
		DeleteObjectsOperation.call(this, objectIds, options);
	};
	stjs.extend(DeleteVocabularyTermsOperation, DeleteObjectsOperation, [ DeleteObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.delete.DeleteVocabularyTermsOperation';
		prototype.getMessage = function() {
			return "DeleteVocabularyTermsOperation";
		};
	}, {});
	return DeleteVocabularyTermsOperation;
})