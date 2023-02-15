/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/UpdateObjectsOperation" ], function(stjs, UpdateObjectsOperation) {
	var UpdateMaterialsOperation = function(updates) {
		UpdateObjectsOperation.call(this, updates);
	};
	stjs.extend(UpdateMaterialsOperation, UpdateObjectsOperation, [ UpdateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.material.update.UpdateMaterialsOperation';
		prototype.getMessage = function() {
			return "UpdateMaterialsOperation";
		};
	}, {});
	return UpdateMaterialsOperation;
})