/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/delete/DeleteObjectsOperationResult" ], function(stjs, DeleteObjectsOperationResult) {
	var DeleteObjectsWithTrashOperationResult = function(deletionId) {
		this.deletionId = deletionId;
	};
	stjs.extend(DeleteObjectsWithTrashOperationResult, DeleteObjectsOperationResult, [ DeleteObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.delete.DeleteObjectsWithTrashOperationResult';
		prototype.deletionId = null;
		prototype.getDeletionId = function() {
			return this.deletionId;
		};
		prototype.getMessage = function() {
			return "DeleteObjectsWithTrashOperationResult";
		};
	}, {
		deletionId : "IDeletionId"
	});
	return DeleteObjectsWithTrashOperationResult;
})