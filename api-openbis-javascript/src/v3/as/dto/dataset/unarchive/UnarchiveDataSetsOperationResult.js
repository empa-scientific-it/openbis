/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var UnarchiveDataSetsOperationResult = function() {
	};
	stjs.extend(UnarchiveDataSetsOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.unarchive.UnarchiveDataSetsOperationResult';
		prototype.getMessage = function() {
			return "UnarchiveDataSetsOperationResult";
		};
	}, {});
	return UnarchiveDataSetsOperationResult;
})