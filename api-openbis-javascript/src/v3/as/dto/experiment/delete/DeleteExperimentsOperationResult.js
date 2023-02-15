/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/delete/DeleteObjectsWithTrashOperationResult" ], function(stjs, DeleteObjectsWithTrashOperationResult) {
	var DeleteExperimentsOperationResult = function(deletionId) {
		DeleteObjectsWithTrashOperationResult.call(this, deletionId);
	};
	stjs.extend(DeleteExperimentsOperationResult, DeleteObjectsWithTrashOperationResult, [ DeleteObjectsWithTrashOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.delete.DeleteExperimentsOperationResult';
		prototype.getMessage = function() {
			return "DeleteExperimentsOperationResult";
		};
	}, {});
	return DeleteExperimentsOperationResult;
})