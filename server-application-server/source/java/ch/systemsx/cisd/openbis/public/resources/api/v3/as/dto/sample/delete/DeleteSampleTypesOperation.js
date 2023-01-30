define([ "stjs", "as/dto/common/delete/DeleteObjectsOperation" ], function(stjs, DeleteObjectsOperation) {
	var DeleteSampleTypesOperation = function(objectIds, options) {
		DeleteObjectsOperation.call(this, objectIds, options);
	};
	stjs.extend(DeleteSampleTypesOperation, DeleteObjectsOperation, [ DeleteObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.delete.DeleteSampleTypesOperation';
		prototype.getMessage = function() {
			return "DeleteSampleTypesOperation";
		};
	}, {});
	return DeleteSampleTypesOperation;
})
