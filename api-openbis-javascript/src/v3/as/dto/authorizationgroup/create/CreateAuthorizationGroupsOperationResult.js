define([ "stjs", "as/dto/common/create/CreateObjectsOperationResult" ], function(stjs, CreateObjectsOperationResult) {
	var CreateAuthorizationGroupsOperationResult = function(objectIds) {
		CreateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(CreateAuthorizationGroupsOperationResult, CreateObjectsOperationResult, [ CreateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.authorizationgroup.create.CreateAuthorizationGroupsOperationResult';
		prototype.getMessage = function() {
			return "CreateAuthorizationGroupsOperationResult";
		};
	}, {});
	return CreateAuthorizationGroupsOperationResult;
})
