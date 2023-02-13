define([ "stjs", "as/dto/common/delete/DeleteObjectsWithoutTrashOperationResult" ], function(stjs, DeleteObjectsWithoutTrashOperationResult) {
	var DeleteVocabulariesOperationResult = function() {
		DeleteObjectsWithoutTrashOperationResult.call(this);
	};
	stjs.extend(DeleteVocabulariesOperationResult, DeleteObjectsWithoutTrashOperationResult, [ DeleteObjectsWithoutTrashOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.delete.DeleteVocabulariesOperationResult';
		prototype.getMessage = function() {
			return "DeleteVocabulariesOperationResult";
		};
	}, {});
	return DeleteVocabulariesOperationResult;
})
