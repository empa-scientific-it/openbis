define([ "stjs", "as/dto/common/create/CreateObjectsOperationResult" ], function(stjs, CreateObjectsOperationResult) {
	var CreateRoleAssignmentsOperationResult = function(objectIds) {
		CreateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(CreateRoleAssignmentsOperationResult, CreateObjectsOperationResult, [ CreateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.roleassignment.create.CreateRoleAssignmentsOperationResult';
		prototype.getMessage = function() {
			return "CreateRoleAssignmentsOperationResult";
		};
	}, {});
	return CreateRoleAssignmentsOperationResult;
})
