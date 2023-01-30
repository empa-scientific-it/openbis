/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/UpdateObjectsOperation" ], function(stjs, UpdateObjectsOperation) {
	var UpdateDataSetsOperation = function(updates) {
		UpdateObjectsOperation.call(this, updates);
	};
	stjs.extend(UpdateDataSetsOperation, UpdateObjectsOperation, [ UpdateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.update.UpdateDataSetsOperation';
		prototype.getMessage = function() {
			return "UpdateDataSetsOperation";
		};
	}, {});
	return UpdateDataSetsOperation;
})