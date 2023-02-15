/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/get/GetObjectsOperationResult" ], function(stjs, GetObjectsOperationResult) {
	var GetExperimentsOperationResult = function(objectMap) {
		GetObjectsOperationResult.call(this, objectMap);
	};
	stjs.extend(GetExperimentsOperationResult, GetObjectsOperationResult, [ GetObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.get.GetExperimentsOperationResult';
		prototype.getMessage = function() {
			return "GetExperimentsOperationResult";
		};
	}, {});
	return GetExperimentsOperationResult;
})