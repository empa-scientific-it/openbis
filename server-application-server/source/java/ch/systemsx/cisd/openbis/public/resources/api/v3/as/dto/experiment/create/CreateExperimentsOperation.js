/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/create/CreateObjectsOperation" ], function(stjs, CreateObjectsOperation) {
	var CreateExperimentsOperation = function(creations) {
		CreateObjectsOperation.call(this, creations);
	};
	stjs.extend(CreateExperimentsOperation, CreateObjectsOperation, [ CreateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.create.CreateExperimentsOperation';
		prototype.getMessage = function() {
			return "CreateExperimentsOperation";
		};
	}, {});
	return CreateExperimentsOperation;
})