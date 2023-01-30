define([ "stjs", "as/dto/common/create/CreateObjectsOperation" ], function(stjs, CreateObjectsOperation) {
	var CreateRoleAssignmentsOperation = function(creations) {
		CreateObjectsOperation.call(this, creations);
	};
	stjs.extend(CreateRoleAssignmentsOperation, CreateObjectsOperation, [ CreateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.roleassignment.create.CreateRoleAssignmentsOperation';
		prototype.getMessage = function() {
			return "CreateRoleAssignmentsOperation";
		};
	}, {});
	return CreateRoleAssignmentsOperation;
})
