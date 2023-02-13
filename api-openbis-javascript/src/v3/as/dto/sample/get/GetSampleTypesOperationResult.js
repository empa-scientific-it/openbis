/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/get/GetObjectsOperationResult" ], function(stjs, GetObjectsOperationResult) {
	var GetSampleTypesOperationResult = function(objectMap) {
		GetObjectsOperationResult.call(this, objectMap);
	};
	stjs.extend(GetSampleTypesOperationResult, GetObjectsOperationResult, [ GetObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.get.GetSampleTypesOperationResult';
		prototype.getMessage = function() {
			return "GetSampleTypesOperationResult";
		};
	}, {});
	return GetSampleTypesOperationResult;
})