define([ "stjs", "as/dto/common/update/UpdateObjectsOperationResult" ], function(stjs, UpdateObjectsOperationResult) {
	var UpdateAuthorizationGroupsOperationResult = function(objectIds) {
		UpdateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(UpdateAuthorizationGroupsOperationResult, UpdateObjectsOperationResult, [ UpdateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.authorizationgroup.update.UpdateAuthorizationGroupsOperationResult';
		prototype.getMessage = function() {
			return "UpdateAuthorizationGroupsOperationResult";
		};
	}, {});
	return UpdateAuthorizationGroupsOperationResult;
})
