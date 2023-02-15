define([ "stjs", "as/dto/common/update/UpdateObjectsOperationResult" ], function(stjs, UpdateObjectsOperationResult) {
	var UpdateDataSetTypesOperationResult = function(objectIds) {
		UpdateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(UpdateDataSetTypesOperationResult, UpdateObjectsOperationResult, [ UpdateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.update.UpdateDataSetTypesOperationResult';
		prototype.getMessage = function() {
			return "UpdateDataSetTypesOperationResult";
		};
	}, {});
	return UpdateDataSetTypesOperationResult;
})