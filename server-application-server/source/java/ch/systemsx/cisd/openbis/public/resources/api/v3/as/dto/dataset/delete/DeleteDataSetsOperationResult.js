/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/delete/DeleteObjectsWithTrashOperationResult" ], function(stjs, DeleteObjectsWithTrashOperationResult) {
	var DeleteDataSetsOperationResult = function(deletionId) {
		DeleteObjectsWithTrashOperationResult.call(this, deletionId);
	};
	stjs.extend(DeleteDataSetsOperationResult, DeleteObjectsWithTrashOperationResult, [ DeleteObjectsWithTrashOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.delete.DeleteDataSetsOperationResult';
		prototype.getMessage = function() {
			return "DeleteDataSetsOperationResult";
		};
	}, {});
	return DeleteDataSetsOperationResult;
})