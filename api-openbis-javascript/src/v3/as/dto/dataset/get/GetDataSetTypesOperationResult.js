/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/get/GetObjectsOperationResult" ], function(stjs, GetObjectsOperationResult) {
	var GetDataSetTypesOperationResult = function(objectMap) {
		GetObjectsOperationResult.call(this, objectMap);
	};
	stjs.extend(GetDataSetTypesOperationResult, GetObjectsOperationResult, [ GetObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.get.GetDataSetTypesOperationResult';
		prototype.getMessage = function() {
			return "GetDataSetTypesOperationResult";
		};
	}, {});
	return GetDataSetTypesOperationResult;
})