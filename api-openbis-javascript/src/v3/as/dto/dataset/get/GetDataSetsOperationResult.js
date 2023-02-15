/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/get/GetObjectsOperationResult" ], function(stjs, GetObjectsOperationResult) {
	var GetDataSetsOperationResult = function(objectMap) {
		GetObjectsOperationResult.call(this, objectMap);
	};
	stjs.extend(GetDataSetsOperationResult, GetObjectsOperationResult, [ GetObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.get.GetDataSetsOperationResult';
		prototype.getMessage = function() {
			return "GetDataSetsOperationResult";
		};
	}, {});
	return GetDataSetsOperationResult;
})