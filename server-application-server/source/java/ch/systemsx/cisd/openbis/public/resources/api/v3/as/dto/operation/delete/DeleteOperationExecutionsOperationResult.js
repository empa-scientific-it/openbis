/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/delete/DeleteObjectsWithoutTrashOperationResult" ], function(stjs, DeleteObjectsWithoutTrashOperationResult) {
	var DeleteOperationExecutionsOperationResult = function() {
		DeleteObjectsWithoutTrashOperationResult.call(this);
	};
	stjs.extend(DeleteOperationExecutionsOperationResult, DeleteObjectsWithoutTrashOperationResult, [ DeleteObjectsWithoutTrashOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.operation.delete.DeleteOperationExecutionsOperationResult';
		prototype.getMessage = function() {
			return "DeleteOperationExecutionsOperationResult";
		};
	}, {});
	return DeleteOperationExecutionsOperationResult;
})