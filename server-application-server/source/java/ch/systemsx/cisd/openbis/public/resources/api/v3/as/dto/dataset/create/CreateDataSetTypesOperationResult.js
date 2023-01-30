/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/create/CreateObjectsOperationResult" ], function(stjs, CreateObjectsOperationResult) {
	var CreateDataSetTypesOperationResult = function(objectIds) {
		CreateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(CreateDataSetTypesOperationResult, CreateObjectsOperationResult, [ CreateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.create.CreateDataSetTypesOperationResult';
		prototype.getMessage = function() {
			return "CreateDataSetTypesOperationResult";
		};
	}, {});
	return CreateDataSetTypesOperationResult;
})