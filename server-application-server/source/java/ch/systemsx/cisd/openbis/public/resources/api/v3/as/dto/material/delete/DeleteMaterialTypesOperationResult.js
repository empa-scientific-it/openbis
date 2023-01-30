define([ "stjs", "as/dto/common/delete/DeleteObjectsWithoutTrashOperationResult" ], function(stjs, DeleteObjectsWithoutTrashOperationResult) {
	var DeleteMaterialTypesOperationResult = function() {
		DeleteObjectsWithoutTrashOperationResult.call(this);
	};
	stjs.extend(DeleteMaterialTypesOperationResult, DeleteObjectsWithoutTrashOperationResult, [ DeleteObjectsWithoutTrashOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.material.delete.DeleteMaterialTypesOperationResult';
		prototype.getMessage = function() {
			return "DeleteMaterialTypesOperationResult";
		};
	}, {});
	return DeleteMaterialTypesOperationResult;
})
