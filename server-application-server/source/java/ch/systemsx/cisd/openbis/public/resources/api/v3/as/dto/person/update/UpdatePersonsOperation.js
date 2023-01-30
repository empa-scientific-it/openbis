define([ "stjs", "as/dto/common/update/UpdateObjectsOperation" ], function(stjs, UpdateObjectsOperation) {
	var UpdatePersonsOperation = function(updates) {
		UpdateObjectsOperation.call(this, updates);
	};
	stjs.extend(UpdatePersonsOperation, UpdateObjectsOperation, [ UpdateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.person.update.UpdatePersonsOperation';
		prototype.getMessage = function() {
			return "UpdatePersonsOperation";
		};
	}, {});
	return UpdatePersonsOperation;
})
