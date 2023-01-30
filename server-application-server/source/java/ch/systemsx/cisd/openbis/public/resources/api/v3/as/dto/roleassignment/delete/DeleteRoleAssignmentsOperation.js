define([ "stjs", "as/dto/common/delete/DeleteObjectsOperation" ], function(stjs, DeleteObjectsOperation) {
	var DeleteRoleAssignmentsOperation = function(objectIds, options) {
		DeleteObjectsOperation.call(this, objectIds, options);
	};
	stjs.extend(DeleteRoleAssignmentsOperation, DeleteObjectsOperation, [ DeleteObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.roleassignment.delete.DeleteRoleAssignmentsOperation';
		prototype.getMessage = function() {
			return "DeleteRoleAssignmentsOperation";
		};
	}, {});
	return DeleteRoleAssignmentsOperation;
})