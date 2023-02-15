/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/get/GetObjectsOperationResult" ], function(stjs, GetObjectsOperationResult) {
	var GetExperimentTypesOperationResult = function(objectMap) {
		GetObjectsOperationResult.call(this, objectMap);
	};
	stjs.extend(GetExperimentTypesOperationResult, GetObjectsOperationResult, [ GetObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.get.GetExperimentTypesOperationResult';
		prototype.getMessage = function() {
			return "GetExperimentTypesOperationResult";
		};
	}, {});
	return GetExperimentTypesOperationResult;
})