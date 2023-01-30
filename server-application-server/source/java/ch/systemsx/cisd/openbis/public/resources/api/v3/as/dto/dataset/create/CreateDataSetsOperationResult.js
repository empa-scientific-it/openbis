/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/create/CreateObjectsOperationResult" ], function(stjs, CreateObjectsOperationResult) {
	var CreateDataSetsOperationResult = function(objectIds) {
		CreateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(CreateDataSetsOperationResult, CreateObjectsOperationResult, [ CreateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.create.CreateDataSetsOperationResult';
		prototype.getMessage = function() {
			return "CreateDataSetsOperationResult";
		};
	}, {});
	return CreateDataSetsOperationResult;
})