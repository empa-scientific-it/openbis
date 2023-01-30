define([ "stjs", "as/dto/common/delete/DeleteObjectsWithoutTrashOperationResult" ], function(stjs, DeleteObjectsWithoutTrashOperationResult) {
	var DeleteAuthorizationGroupsOperationResult = function() {
		DeleteObjectsWithoutTrashOperationResult.call(this);
	};
	stjs.extend(DeleteAuthorizationGroupsOperationResult, DeleteObjectsWithoutTrashOperationResult, [ DeleteObjectsWithoutTrashOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.authorizationgroup.delete.DeleteAuthorizationGroupsOperationResult';
		prototype.getMessage = function() {
			return "DeleteAuthorizationGroupsOperationResult";
		};
	}, {});
	return DeleteAuthorizationGroupsOperationResult;
})