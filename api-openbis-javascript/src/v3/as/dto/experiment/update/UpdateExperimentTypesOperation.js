define([ "stjs", "as/dto/common/update/UpdateObjectsOperation" ], function(stjs, UpdateObjectsOperation) {
	var UpdateExperimentTypesOperation = function(updates) {
		UpdateObjectsOperation.call(this, updates);
	};
	stjs.extend(UpdateExperimentTypesOperation, UpdateObjectsOperation, [ UpdateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.update.UpdateExperimentTypesOperation';
		prototype.getMessage = function() {
			return "UpdateExperimentTypesOperation";
		};
	}, {});
	return UpdateExperimentTypesOperation;
})