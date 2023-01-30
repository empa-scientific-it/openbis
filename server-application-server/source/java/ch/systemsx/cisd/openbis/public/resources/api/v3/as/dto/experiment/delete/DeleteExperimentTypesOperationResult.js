define([ "stjs", "as/dto/common/delete/DeleteObjectsWithoutTrashOperationResult" ], function(stjs, DeleteObjectsWithoutTrashOperationResult) {
	var DeleteExperimentTypesOperationResult = function() {
		DeleteObjectsWithoutTrashOperationResult.call(this);
	};
	stjs.extend(DeleteExperimentTypesOperationResult, DeleteObjectsWithoutTrashOperationResult, [ DeleteObjectsWithoutTrashOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.delete.DeleteExperimentTypesOperationResult';
		prototype.getMessage = function() {
			return "DeleteExperimentTypesOperationResult";
		};
	}, {});
	return DeleteExperimentTypesOperationResult;
})
