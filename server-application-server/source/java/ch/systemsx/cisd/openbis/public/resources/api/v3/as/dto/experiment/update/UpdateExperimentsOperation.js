/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/UpdateObjectsOperation" ], function(stjs, UpdateObjectsOperation) {
	var UpdateExperimentsOperation = function(updates) {
		UpdateObjectsOperation.call(this, updates);
	};
	stjs.extend(UpdateExperimentsOperation, UpdateObjectsOperation, [ UpdateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.update.UpdateExperimentsOperation';
		prototype.getMessage = function() {
			return "UpdateExperimentsOperation";
		};
	}, {});
	return UpdateExperimentsOperation;
})