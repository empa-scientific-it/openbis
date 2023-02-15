define([ "stjs", "as/dto/common/get/GetObjectsOperationResult" ], function(stjs, GetObjectsOperationResult) {
	var GetRightsOperationResult = function(objectMap) {
		GetObjectsOperationResult.call(this, objectMap);
	};
	stjs.extend(GetRightsOperationResult, GetObjectsOperationResult, [ GetObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.rights.get.GetRightsOperationResult';
		prototype.getMessage = function() {
			return "GetRightsOperationResult";
		};
	}, {});
	return GetRightsOperationResult;
})
