define([ "stjs", "as/dto/common/delete/DeleteObjectsWithoutTrashOperationResult" ], function(stjs, DeleteObjectsWithoutTrashOperationResult) {
	var DeleteDataSetTypesOperationResult = function() {
		DeleteObjectsWithoutTrashOperationResult.call(this);
	};
	stjs.extend(DeleteDataSetTypesOperationResult, DeleteObjectsWithoutTrashOperationResult, [ DeleteObjectsWithoutTrashOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.delete.DeleteDataSetTypesOperationResult';
		prototype.getMessage = function() {
			return "DeleteDataSetTypesOperationResult";
		};
	}, {});
	return DeleteDataSetTypesOperationResult;
})
