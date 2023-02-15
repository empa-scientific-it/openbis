define([ "stjs", "as/dto/common/update/UpdateObjectsOperationResult" ], function(stjs, UpdateObjectsOperationResult) {
	var UpdateSampleTypesOperationResult = function(objectIds) {
		UpdateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(UpdateSampleTypesOperationResult, UpdateObjectsOperationResult, [ UpdateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.update.UpdateSampleTypesOperationResult';
		prototype.getMessage = function() {
			return "UpdateSampleTypesOperationResult";
		};
	}, {});
	return UpdateSampleTypesOperationResult;
})