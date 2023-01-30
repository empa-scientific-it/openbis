/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/get/GetObjectsOperation" ], function(stjs, GetObjectsOperation) {
	var GetExperimentTypesOperation = function(objectIds, fetchOptions) {
		GetObjectsOperation.call(this, objectIds, fetchOptions);
	};
	stjs.extend(GetExperimentTypesOperation, GetObjectsOperation, [ GetObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.get.GetExperimentTypesOperation';
		prototype.getMessage = function() {
			return "GetExperimentTypesOperation";
		};
	}, {});
	return GetExperimentTypesOperation;
})