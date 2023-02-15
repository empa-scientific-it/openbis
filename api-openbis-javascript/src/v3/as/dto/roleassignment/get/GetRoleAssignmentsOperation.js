define([ "stjs", "as/dto/common/get/GetObjectsOperation" ], function(stjs, GetObjectsOperation) {
	var GetRoleAssignmentsOperation = function(objectIds, fetchOptions) {
		GetObjectsOperation.call(this, objectIds, fetchOptions);
	};
	stjs.extend(GetRoleAssignmentsOperation, GetObjectsOperation, [ GetObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.roleassignment.get.GetRoleAssignmentsOperation';
		prototype.getMessage = function() {
			return "GetRoleAssignmentsOperation";
		};
	}, {});
	return GetRoleAssignmentsOperation;
})
