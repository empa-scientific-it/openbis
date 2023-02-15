define([ "stjs", "as/dto/common/delete/DeleteObjectsOperation" ], function(stjs, DeleteObjectsOperation) {
	var DeleteVocabulariesOperation = function(objectIds, options) {
		DeleteObjectsOperation.call(this, objectIds, options);
	};
	stjs.extend(DeleteVocabulariesOperation, DeleteObjectsOperation, [ DeleteObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.delete.DeleteVocabulariesOperation';
		prototype.getMessage = function() {
			return "DeleteVocabulariesOperation";
		};
	}, {});
	return DeleteVocabulariesOperation;
})
