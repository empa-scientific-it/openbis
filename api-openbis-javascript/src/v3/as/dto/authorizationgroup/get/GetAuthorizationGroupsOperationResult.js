define([ "stjs", "as/dto/common/get/GetObjectsOperationResult" ], function(stjs, GetObjectsOperationResult) {
	var GetAuthorizationGroupsOperationResult = function(objectMap) {
		GetObjectsOperationResult.call(this, objectMap);
	};
	stjs.extend(GetAuthorizationGroupsOperationResult, GetObjectsOperationResult, [ GetObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.authorizationgroup.get.GetAuthorizationGroupsOperationResult';
		prototype.getMessage = function() {
			return "GetAuthorizationGroupsOperationResult";
		};
	}, {});
	return GetAuthorizationGroupsOperationResult;
})
