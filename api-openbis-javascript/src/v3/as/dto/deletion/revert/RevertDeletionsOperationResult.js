/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var RevertDeletionsOperationResult = function() {
	};
	stjs.extend(RevertDeletionsOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.deletion.revert.RevertDeletionsOperationResult';
		prototype.getMessage = function() {
			return "RevertDeletionsOperationResult";
		};
	}, {});
	return RevertDeletionsOperationResult;
})