define([ "stjs", "as/dto/common/delete/DeleteObjectsOperation" ], function(stjs, DeleteObjectsOperation) {
	var DeleteMaterialTypesOperation = function(objectIds, options) {
		DeleteObjectsOperation.call(this, objectIds, options);
	};
	stjs.extend(DeleteMaterialTypesOperation, DeleteObjectsOperation, [ DeleteObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.material.delete.DeleteMaterialTypesOperation';
		prototype.getMessage = function() {
			return "DeleteMaterialTypesOperation";
		};
	}, {});
	return DeleteMaterialTypesOperation;
})
