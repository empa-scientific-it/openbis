/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/create/CreateObjectsOperationResult" ], function(stjs, CreateObjectsOperationResult) {
	var CreateExperimentTypesOperationResult = function(objectIds) {
		CreateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(CreateExperimentTypesOperationResult, CreateObjectsOperationResult, [ CreateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.create.CreateExperimentTypesOperationResult';
		prototype.getMessage = function() {
			return "CreateExperimentTypesOperationResult";
		};
	}, {});
	return CreateExperimentTypesOperationResult;
})