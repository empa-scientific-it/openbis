define([ "stjs", "as/dto/common/update/UpdateObjectsOperationResult" ], function(stjs, UpdateObjectsOperationResult) {
	var UpdateMaterialTypesOperationResult = function(objectIds) {
		UpdateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(UpdateMaterialTypesOperationResult, UpdateObjectsOperationResult, [ UpdateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.material.update.UpdateMaterialTypesOperationResult';
		prototype.getMessage = function() {
			return "UpdateMaterialTypesOperationResult";
		};
	}, {});
	return UpdateMaterialTypesOperationResult;
})