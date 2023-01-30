/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/delete/DeleteObjectsWithTrashOperationResult" ], function(stjs, DeleteObjectsWithTrashOperationResult) {
	var DeleteSamplesOperationResult = function(deletionId) {
		DeleteObjectsWithTrashOperationResult.call(this, deletionId);
	};
	stjs.extend(DeleteSamplesOperationResult, DeleteObjectsWithTrashOperationResult, [ DeleteObjectsWithTrashOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.delete.DeleteSamplesOperationResult';
		prototype.getMessage = function() {
			return "DeleteSamplesOperationResult";
		};
	}, {});
	return DeleteSamplesOperationResult;
})