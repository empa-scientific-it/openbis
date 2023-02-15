define([ "stjs", "as/dto/common/delete/DeleteObjectsWithoutTrashOperationResult" ], function(stjs, DeleteObjectsWithoutTrashOperationResult) {
	var DeleteSampleTypesOperationResult = function() {
		DeleteObjectsWithoutTrashOperationResult.call(this);
	};
	stjs.extend(DeleteSampleTypesOperationResult, DeleteObjectsWithoutTrashOperationResult, [ DeleteObjectsWithoutTrashOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.delete.DeleteSampleTypesOperationResult';
		prototype.getMessage = function() {
			return "DeleteSampleTypesOperationResult";
		};
	}, {});
	return DeleteSampleTypesOperationResult;
})
