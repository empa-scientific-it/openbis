/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var ConfirmDeletionsOperationResult = function() {
	};
	stjs.extend(ConfirmDeletionsOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.deletion.confirm.ConfirmDeletionsOperationResult';
		prototype.getMessage = function() {
			return "ConfirmDeletionsOperationResult";
		};
	}, {});
	return ConfirmDeletionsOperationResult;
})