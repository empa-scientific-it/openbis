/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperationExecutionProgress" ], function(stjs, IOperationExecutionProgress) {
	var OperationExecutionProgress = function(message, numItemsProcessed, totalItemsToProcess) {
		this.message = message;
		this.numItemsProcessed = numItemsProcessed;
		this.totalItemsToProcess = totalItemsToProcess;
	};
	stjs.extend(OperationExecutionProgress, null, [ IOperationExecutionProgress ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.operation.OperationExecutionProgress';
		prototype.message = null;
		prototype.numItemsProcessed = null;
		prototype.totalItemsToProcess = null;
		prototype.getMessage = function() {
			return this.message;
		};
		prototype.getNumItemsProcessed = function() {
			return this.numItemsProcessed;
		};
		prototype.getTotalItemsToProcess = function() {
			return this.totalItemsToProcess;
		};
	}, {});
	return OperationExecutionProgress;
})