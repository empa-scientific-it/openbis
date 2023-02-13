define([ "stjs", "as/dto/common/get/GetObjectsOperation" ], function(stjs, GetObjectsOperation) {
	var GetAuthorizationGroupsOperation = function(objectIds, fetchOptions) {
		GetObjectsOperation.call(this, objectIds, fetchOptions);
	};
	stjs.extend(GetAuthorizationGroupsOperation, GetObjectsOperation, [ GetObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.authorizationgroup.get.GetAuthorizationGroupsOperation';
		prototype.getMessage = function() {
			return "GetAuthorizationGroupsOperation";
		};
	}, {});
	return GetAuthorizationGroupsOperation;
})
