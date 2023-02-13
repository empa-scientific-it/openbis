define([ "stjs", "as/dto/common/get/GetObjectsOperationResult" ], function(stjs, GetObjectsOperationResult) {
	var GetRoleAssignmentsOperationResult = function(objectMap) {
		GetObjectsOperationResult.call(this, objectMap);
	};
	stjs.extend(GetRoleAssignmentsOperationResult, GetObjectsOperationResult, [ GetObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.roleassignment.get.GetRoleAssignmentsOperationResult';
		prototype.getMessage = function() {
			return "GetRoleAssignmentsOperationResult";
		};
	}, {});
	return GetRoleAssignmentsOperationResult;
})
