/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/IObjectId" ], function(stjs, IObjectId) {
	var IOperationExecutionId = function() {
	};
	stjs.extend(IOperationExecutionId, null, [ IObjectId ], null, {});
	return IOperationExecutionId;
})