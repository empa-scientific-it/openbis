define([ "stjs", "as/dto/common/update/UpdateObjectsOperationResult" ], function(stjs, UpdateObjectsOperationResult) {
	var UpdatePropertyTypesOperationResult = function(objectIds) {
		UpdateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(UpdatePropertyTypesOperationResult, UpdateObjectsOperationResult, [ UpdateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.update.UpdatePropertyTypesOperationResult';
		prototype.getMessage = function() {
			return "UpdatePropertyTypesOperationResult";
		};
	}, {});
	return UpdatePropertyTypesOperationResult;
})
