/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/delete/DeleteObjectsOperationResult" ], function(stjs, DeleteObjectsOperationResult) {
	var DeleteObjectsWithoutTrashOperationResult = function() {
	};
	stjs.extend(DeleteObjectsWithoutTrashOperationResult, DeleteObjectsOperationResult, [ DeleteObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.delete.DeleteObjectsWithoutTrashOperationResult';
		prototype.getMessage = function() {
			return "DeleteObjectsWithoutTrashOperationResult";
		};
	}, {});
	return DeleteObjectsWithoutTrashOperationResult;
})