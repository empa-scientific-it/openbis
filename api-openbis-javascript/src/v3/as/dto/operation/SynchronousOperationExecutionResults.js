/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/operation/IOperationExecutionResults" ], function(stjs, IOperationExecutionResults) {
	var SynchronousOperationExecutionResults = function(results) {
		this.results = results;
	};
	stjs.extend(SynchronousOperationExecutionResults, null, [ IOperationExecutionResults ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.operation.SynchronousOperationExecutionResults';
		prototype.results = null;
		prototype.getResults = function() {
			return this.results;
		};
	}, {
		results : {
			name : "List",
			arguments : [ "IOperationResult" ]
		}
	});
	return SynchronousOperationExecutionResults;
})