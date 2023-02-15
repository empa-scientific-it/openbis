define([ "stjs", "as/dto/common/update/UpdateObjectsOperationResult" ], function(stjs, UpdateObjectsOperationResult) {
	var UpdatePersonsOperationResult = function(objectIds) {
		UpdateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(UpdatePersonsOperationResult, UpdateObjectsOperationResult, [ UpdateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.person.update.UpdatePersonsOperationResult';
		prototype.getMessage = function() {
			return "UpdatePersonsOperationResult";
		};
	}, {});
	return UpdatePersonsOperationResult;
})
