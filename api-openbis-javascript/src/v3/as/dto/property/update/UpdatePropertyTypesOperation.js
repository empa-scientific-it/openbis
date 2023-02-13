define([ "stjs", "as/dto/common/update/UpdateObjectsOperation" ], function(stjs, UpdateObjectsOperation) {
	var UpdatePropertyTypesOperation = function(updates) {
		UpdateObjectsOperation.call(this, updates);
	};
	stjs.extend(UpdatePropertyTypesOperation, UpdateObjectsOperation, [ UpdateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.update.UpdatePropertyTypesOperation';
		prototype.getMessage = function() {
			return "UpdatePropertyTypesOperation";
		};
	}, {});
	return UpdatePropertyTypesOperation;
})
