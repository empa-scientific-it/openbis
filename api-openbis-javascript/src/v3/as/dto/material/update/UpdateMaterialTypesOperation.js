define([ "stjs", "as/dto/common/update/UpdateObjectsOperation" ], function(stjs, UpdateObjectsOperation) {
	var UpdateMaterialTypesOperation = function(updates) {
		UpdateObjectsOperation.call(this, updates);
	};
	stjs.extend(UpdateMaterialTypesOperation, UpdateObjectsOperation, [ UpdateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.material.update.UpdateMaterialTypesOperation';
		prototype.getMessage = function() {
			return "UpdateMaterialTypesOperation";
		};
	}, {});
	return UpdateMaterialTypesOperation;
})