/**
 * @author anttil
 */
define([ "stjs", "as/dto/common/create/CreateObjectsOperationResult" ], function(stjs, CreateObjectsOperationResult) {
	var CreateExternalDmsOperationResult = function(objectIds) {
		CreateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(CreateExternalDmsOperationResult, CreateObjectsOperationResult, [ CreateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.externaldms.create.CreateExternalDmsOperationResult';
		prototype.getMessage = function() {
			return "CreateExternalDmsOperationResult";
		};
	}, {});
	return CreateExternalDmsOperationResult;
})