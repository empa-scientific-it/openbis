define([ "stjs", "as/dto/common/update/UpdateObjectsOperationResult" ], function(stjs, UpdateObjectsOperationResult) {
	var UpdateExperimentTypesOperationResult = function(objectIds) {
		UpdateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(UpdateExperimentTypesOperationResult, UpdateObjectsOperationResult, [ UpdateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.update.UpdateExperimentTypesOperationResult';
		prototype.getMessage = function() {
			return "UpdateExperimentTypesOperationResult";
		};
	}, {});
	return UpdateExperimentTypesOperationResult;
})