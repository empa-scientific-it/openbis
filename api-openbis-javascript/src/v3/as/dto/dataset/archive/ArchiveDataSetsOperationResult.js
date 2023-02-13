/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var ArchiveDataSetsOperationResult = function() {
	};
	stjs.extend(ArchiveDataSetsOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.archive.ArchiveDataSetsOperationResult';
		prototype.getMessage = function() {
			return "ArchiveDataSetsOperationResult";
		};
	}, {});
	return ArchiveDataSetsOperationResult;
})