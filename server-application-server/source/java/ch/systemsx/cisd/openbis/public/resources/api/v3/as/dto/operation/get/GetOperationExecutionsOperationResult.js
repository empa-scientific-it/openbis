/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/get/GetObjectsOperationResult" ], function(stjs, GetObjectsOperationResult) {
	var GetOperationExecutionsOperationResult = function(objectMap) {
		GetObjectsOperationResult.call(this, objectMap);
	};
	stjs.extend(GetOperationExecutionsOperationResult, GetObjectsOperationResult, [ GetObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.operation.get.GetOperationExecutionsOperationResult';
		prototype.getMessage = function() {
			return "GetOperationExecutionsOperationResult";
		};
	}, {});
	return GetOperationExecutionsOperationResult;
})