/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/delete/DeleteObjectsWithoutTrashOperationResult" ], function(stjs, DeleteObjectsWithoutTrashOperationResult) {
	var DeleteSemanticAnnotationsOperationResult = function() {
		DeleteObjectsWithoutTrashOperationResult.call(this);
	};
	stjs.extend(DeleteSemanticAnnotationsOperationResult, DeleteObjectsWithoutTrashOperationResult, [ DeleteObjectsWithoutTrashOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.semanticannotation.delete.DeleteSemanticAnnotationsOperationResult';
		prototype.getMessage = function() {
			return "DeleteSemanticAnnotationsOperationResult";
		};
	}, {});
	return DeleteSemanticAnnotationsOperationResult;
})