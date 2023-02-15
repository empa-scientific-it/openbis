/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/create/CreateObjectsOperationResult" ], function(stjs, CreateObjectsOperationResult) {
	var CreateExperimentsOperationResult = function(objectIds) {
		CreateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(CreateExperimentsOperationResult, CreateObjectsOperationResult, [ CreateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.create.CreateExperimentsOperationResult';
		prototype.getMessage = function() {
			return "CreateExperimentsOperationResult";
		};
	}, {});
	return CreateExperimentsOperationResult;
})