/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/get/GetObjectsOperationResult" ], function(stjs, GetObjectsOperationResult) {
	var GetMaterialTypesOperationResult = function(objectMap) {
		GetObjectsOperationResult.call(this, objectMap);
	};
	stjs.extend(GetMaterialTypesOperationResult, GetObjectsOperationResult, [ GetObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.material.get.GetMaterialTypesOperationResult';
		prototype.getMessage = function() {
			return "GetMaterialTypesOperationResult";
		};
	}, {});
	return GetMaterialTypesOperationResult;
})