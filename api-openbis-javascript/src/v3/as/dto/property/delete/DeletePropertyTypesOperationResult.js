define([ "stjs", "as/dto/common/delete/DeleteObjectsWithoutTrashOperationResult" ], function(stjs, DeleteObjectsWithoutTrashOperationResult) {
	var DeletePropertyTypesOperationResult = function() {
		DeleteObjectsWithoutTrashOperationResult.call(this);
	};
	stjs.extend(DeletePropertyTypesOperationResult, DeleteObjectsWithoutTrashOperationResult, [ DeleteObjectsWithoutTrashOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.delete.DeletePropertyTypesOperationResult';
		prototype.getMessage = function() {
			return "DeletePropertyTypesOperationResult";
		};
	}, {});
	return DeletePropertyTypesOperationResult;
})
