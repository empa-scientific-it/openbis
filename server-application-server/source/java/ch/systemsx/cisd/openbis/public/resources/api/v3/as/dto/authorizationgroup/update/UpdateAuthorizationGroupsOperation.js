define([ "stjs", "as/dto/common/update/UpdateObjectsOperation" ], function(stjs, UpdateObjectsOperation) {
	var UpdateAuthorizationGroupsOperation = function(updates) {
		UpdateObjectsOperation.call(this, updates);
	};
	stjs.extend(UpdateAuthorizationGroupsOperation, UpdateObjectsOperation, [ UpdateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.authorizationgroup.update.UpdateAuthorizationGroupsOperation';
		prototype.getMessage = function() {
			return "UpdateAuthorizationGroupsOperation";
		};
	}, {});
	return UpdateAuthorizationGroupsOperation;
})
