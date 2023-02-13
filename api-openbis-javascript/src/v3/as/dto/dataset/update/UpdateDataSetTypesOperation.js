define([ "stjs", "as/dto/common/update/UpdateObjectsOperation" ], function(stjs, UpdateObjectsOperation) {
	var UpdateDataSetTypesOperation = function(updates) {
		UpdateObjectsOperation.call(this, updates);
	};
	stjs.extend(UpdateDataSetTypesOperation, UpdateObjectsOperation, [ UpdateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.update.UpdateDataSetTypesOperation';
		prototype.getMessage = function() {
			return "UpdateDataSetTypesOperation";
		};
	}, {});
	return UpdateDataSetTypesOperation;
})