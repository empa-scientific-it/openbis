define([ "stjs", "as/dto/common/delete/DeleteObjectsOperation" ], function(stjs, DeleteObjectsOperation) {
	var DeleteAuthorizationGroupsOperation = function(objectIds, options) {
		DeleteObjectsOperation.call(this, objectIds, options);
	};
	stjs.extend(DeleteAuthorizationGroupsOperation, DeleteObjectsOperation, [ DeleteObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.authorizationgroup.delete.DeleteAuthorizationGroupsOperation';
		prototype.getMessage = function() {
			return "DeleteAuthorizationGroupsOperation";
		};
	}, {});
	return DeleteAuthorizationGroupsOperation;
})