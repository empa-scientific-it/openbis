define([ "stjs", "as/dto/common/delete/DeleteObjectsWithoutTrashOperationResult" ], function(stjs, DeleteObjectsWithoutTrashOperationResult) {
	var DeleteRoleAssignmentsOperationResult = function() {
		DeleteObjectsWithoutTrashOperationResult.call(this);
	};
	stjs.extend(DeleteRoleAssignmentsOperationResult, DeleteObjectsWithoutTrashOperationResult, [ DeleteObjectsWithoutTrashOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.roleassignment.delete.DeleteRoleAssignmentsOperationResult';
		prototype.getMessage = function() {
			return "DeleteRoleAssignmentsOperationResult";
		};
	}, {});
	return DeleteRoleAssignmentsOperationResult;
})