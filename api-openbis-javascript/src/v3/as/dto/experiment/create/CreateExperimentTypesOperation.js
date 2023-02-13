/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/create/CreateObjectsOperation" ], function(stjs, CreateObjectsOperation) {
	var CreateExperimentTypesOperation = function(creations) {
		CreateObjectsOperation.call(this, creations);
	};
	stjs.extend(CreateExperimentTypesOperation, CreateObjectsOperation, [ CreateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.create.CreateExperimentTypesOperation';
		prototype.getMessage = function() {
			return "CreateExperimentTypesOperation";
		};
	}, {});
	return CreateExperimentTypesOperation;
})