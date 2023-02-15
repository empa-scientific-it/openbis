/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/UpdateObjectsOperation" ], function(stjs, UpdateObjectsOperation) {
	var UpdateOperationExecutionsOperation = function(updates) {
		UpdateObjectsOperation.call(this, updates);
	};
	stjs.extend(UpdateOperationExecutionsOperation, UpdateObjectsOperation, [ UpdateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.operation.update.UpdateOperationExecutionsOperation';
		prototype.getMessage = function() {
			return "UpdateOperationExecutionsOperation";
		};
	}, {});
	return UpdateOperationExecutionsOperation;
})