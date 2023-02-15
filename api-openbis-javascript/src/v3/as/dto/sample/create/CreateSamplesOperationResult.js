/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/create/CreateObjectsOperationResult" ], function(stjs, CreateObjectsOperationResult) {
	var CreateSamplesOperationResult = function(objectIds) {
		CreateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(CreateSamplesOperationResult, CreateObjectsOperationResult, [ CreateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.create.CreateSamplesOperationResult';
		prototype.getMessage = function() {
			return "CreateSamplesOperationResult";
		};
	}, {});
	return CreateSamplesOperationResult;
})