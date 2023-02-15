define([ "stjs", "as/dto/common/create/CreateObjectsOperation" ], function(stjs, CreateObjectsOperation) {
	var CreateAuthorizationGroupsOperation = function(creations) {
		CreateObjectsOperation.call(this, creations);
	};
	stjs.extend(CreateAuthorizationGroupsOperation, CreateObjectsOperation, [ CreateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.authorizationgroup.create.CreateAuthorizationGroupsOperation';
		prototype.getMessage = function() {
			return "CreateAuthorizationGroupsOperation";
		};
	}, {});
	return CreateAuthorizationGroupsOperation;
})
