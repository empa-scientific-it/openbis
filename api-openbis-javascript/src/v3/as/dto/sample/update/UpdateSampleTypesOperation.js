define([ "stjs", "as/dto/common/update/UpdateObjectsOperation" ], function(stjs, UpdateObjectsOperation) {
	var UpdateSampleTypesOperation = function(updates) {
		UpdateObjectsOperation.call(this, updates);
	};
	stjs.extend(UpdateSampleTypesOperation, UpdateObjectsOperation, [ UpdateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.update.UpdateSampleTypesOperation';
		prototype.getMessage = function() {
			return "UpdateSampleTypesOperation";
		};
	}, {});
	return UpdateSampleTypesOperation;
})