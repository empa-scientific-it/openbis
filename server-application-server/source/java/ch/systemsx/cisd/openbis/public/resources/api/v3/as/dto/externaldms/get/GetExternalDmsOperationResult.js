/**
 * @author anttil
 */
define([ "stjs", "as/dto/common/get/GetObjectsOperationResult" ], function(stjs, GetObjectsOperationResult) {
	var GetExternalDmsOperationResult = function(objectMap) {
		GetObjectsOperationResult.call(this, objectMap);
	};
	stjs.extend(GetExternalDmsOperationResult, GetObjectsOperationResult, [ GetObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.externaldms.get.GetExternalDmsOperationResult';
		prototype.getMessage = function() {
			return "GetExternalDmsOperationResult";
		};
	}, {});
	return GetExternalDmsOperationResult;
})