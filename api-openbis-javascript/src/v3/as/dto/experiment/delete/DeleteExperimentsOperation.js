/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/delete/DeleteObjectsOperation" ], function(stjs, DeleteObjectsOperation) {
	var DeleteExperimentsOperation = function(objectIds, options) {
		DeleteObjectsOperation.call(this, objectIds, options);
	};
	stjs.extend(DeleteExperimentsOperation, DeleteObjectsOperation, [ DeleteObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.delete.DeleteExperimentsOperation';
		prototype.getMessage = function() {
			return "DeleteExperimentsOperation";
		};
	}, {});
	return DeleteExperimentsOperation;
})