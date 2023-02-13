/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/delete/DeleteObjectsOperation" ], function(stjs, DeleteObjectsOperation) {
	var DeleteOperationExecutionsOperation = function(objectIds, options) {
		DeleteObjectsOperation.call(this, objectIds, options);
	};
	stjs.extend(DeleteOperationExecutionsOperation, DeleteObjectsOperation, [ DeleteObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.operation.delete.DeleteOperationExecutionsOperation';
		prototype.getMessage = function() {
			return "DeleteOperationExecutionsOperation";
		};
	}, {});
	return DeleteOperationExecutionsOperation;
})