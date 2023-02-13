define([ "stjs", "as/dto/common/delete/DeleteObjectsOperation" ], function(stjs, DeleteObjectsOperation) {
	var DeleteDataSetTypesOperation = function(objectIds, options) {
		DeleteObjectsOperation.call(this, objectIds, options);
	};
	stjs.extend(DeleteDataSetTypesOperation, DeleteObjectsOperation, [ DeleteObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.delete.DeleteDataSetTypesOperation';
		prototype.getMessage = function() {
			return "DeleteDataSetTypesOperation";
		};
	}, {});
	return DeleteDataSetTypesOperation;
})
