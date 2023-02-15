define([ "stjs", "as/dto/common/get/GetObjectsOperationResult" ], function(stjs, GetObjectsOperationResult) {
	var GetPropertyTypesOperationResult = function(objectMap) {
		GetObjectsOperationResult.call(this, objectMap);
	};
	stjs.extend(GetPropertyTypesOperationResult, GetObjectsOperationResult, [ GetObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.get.GetPropertyTypesOperationResult';
		prototype.getMessage = function() {
			return "GetPropertyTypesOperationResult";
		};
	}, {});
	return GetPropertyTypesOperationResult;
})
