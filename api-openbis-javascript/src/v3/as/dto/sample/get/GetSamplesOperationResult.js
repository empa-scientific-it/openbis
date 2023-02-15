/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/get/GetObjectsOperationResult" ], function(stjs, GetObjectsOperationResult) {
	var GetSamplesOperationResult = function(objectMap) {
		GetObjectsOperationResult.call(this, objectMap);
	};
	stjs.extend(GetSamplesOperationResult, GetObjectsOperationResult, [ GetObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.get.GetSamplesOperationResult';
		prototype.getMessage = function() {
			return "GetSamplesOperationResult";
		};
	}, {});
	return GetSamplesOperationResult;
})