/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/get/GetObjectsOperationResult" ], function(stjs, GetObjectsOperationResult) {
	var GetMaterialsOperationResult = function(objectMap) {
		GetObjectsOperationResult.call(this, objectMap);
	};
	stjs.extend(GetMaterialsOperationResult, GetObjectsOperationResult, [ GetObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.material.get.GetMaterialsOperationResult';
		prototype.getMessage = function() {
			return "GetMaterialsOperationResult";
		};
	}, {});
	return GetMaterialsOperationResult;
})