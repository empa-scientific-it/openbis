define([ "stjs", "as/dto/common/delete/DeleteObjectsOperation" ], function(stjs, DeleteObjectsOperation) {
	var DeleteExperimentTypesOperation = function(objectIds, options) {
		DeleteObjectsOperation.call(this, objectIds, options);
	};
	stjs.extend(DeleteExperimentTypesOperation, DeleteObjectsOperation, [ DeleteObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.delete.DeleteExperimentTypesOperation';
		prototype.getMessage = function() {
			return "DeleteExperimentTypesOperation";
		};
	}, {});
	return DeleteExperimentTypesOperation;
})
