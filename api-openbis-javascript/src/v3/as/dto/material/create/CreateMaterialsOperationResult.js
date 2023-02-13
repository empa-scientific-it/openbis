/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/create/CreateObjectsOperationResult" ], function(stjs, CreateObjectsOperationResult) {
	var CreateMaterialsOperationResult = function(objectIds) {
		CreateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(CreateMaterialsOperationResult, CreateObjectsOperationResult, [ CreateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.material.create.CreateMaterialsOperationResult';
		prototype.getMessage = function() {
			return "CreateMaterialsOperationResult";
		};
	}, {});
	return CreateMaterialsOperationResult;
})