define([ "stjs", "as/dto/common/delete/DeleteObjectsOperation" ], function(stjs, DeleteObjectsOperation) {
	var DeletePropertyTypesOperation = function(objectIds, options) {
		DeleteObjectsOperation.call(this, objectIds, options);
	};
	stjs.extend(DeletePropertyTypesOperation, DeleteObjectsOperation, [ DeleteObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.delete.DeletePropertyTypesOperation';
		prototype.getMessage = function() {
			return "DeletePropertyTypesOperation";
		};
	}, {});
	return DeletePropertyTypesOperation;
})
