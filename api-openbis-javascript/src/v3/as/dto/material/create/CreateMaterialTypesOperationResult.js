/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/create/CreateObjectsOperationResult" ], function(stjs, CreateObjectsOperationResult) {
	var CreateMaterialTypesOperationResult = function(objectIds) {
		CreateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(CreateMaterialTypesOperationResult, CreateObjectsOperationResult, [ CreateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.material.create.CreateMaterialTypesOperationResult';
		prototype.getMessage = function() {
			return "CreateMaterialTypesOperationResult";
		};
	}, {});
	return CreateMaterialTypesOperationResult;
})