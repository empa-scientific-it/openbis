/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/delete/DeleteObjectsWithoutTrashOperationResult" ], function(stjs, DeleteObjectsWithoutTrashOperationResult) {
	var DeleteExternalDmsOperationResult = function() {
		DeleteObjectsWithoutTrashOperationResult.call(this);
	};
	stjs.extend(DeleteExternalDmsOperationResult, DeleteObjectsWithoutTrashOperationResult, [ DeleteObjectsWithoutTrashOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.externaldms.delete.DeleteExternalDmsOperationResult';
		prototype.getMessage = function() {
			return "DeleteExternalDmsOperationResult";
		};
	}, {});
	return DeleteExternalDmsOperationResult;
})