/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/delete/DeleteObjectsOperation" ], function(stjs, DeleteObjectsOperation) {
	var DeleteSemanticAnnotationsOperation = function(objectIds, options) {
		DeleteObjectsOperation.call(this, objectIds, options);
	};
	stjs.extend(DeleteSemanticAnnotationsOperation, DeleteObjectsOperation, [ DeleteObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.semanticannotation.delete.DeleteSemanticAnnotationsOperation';
		prototype.getMessage = function() {
			return "DeleteSemanticAnnotationsOperation";
		};
	}, {});
	return DeleteSemanticAnnotationsOperation;
})