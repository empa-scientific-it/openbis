/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/create/CreateObjectsOperationResult" ], function(stjs, CreateObjectsOperationResult) {
	var CreateSampleTypesOperationResult = function(objectIds) {
		CreateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(CreateSampleTypesOperationResult, CreateObjectsOperationResult, [ CreateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.create.CreateSampleTypesOperationResult';
		prototype.getMessage = function() {
			return "CreateSampleTypesOperationResult";
		};
	}, {});
	return CreateSampleTypesOperationResult;
})