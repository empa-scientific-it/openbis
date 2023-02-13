define([ "stjs", "as/dto/common/create/CreateObjectsOperationResult" ], function(stjs, CreateObjectsOperationResult) {
	var CreatePropertyTypesOperationResult = function(objectIds) {
		CreateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(CreatePropertyTypesOperationResult, CreateObjectsOperationResult, [ CreateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.create.CreatePropertyTypesOperationResult';
		prototype.getMessage = function() {
			return "CreatePropertyTypesOperationResult";
		};
	}, {});
	return CreatePropertyTypesOperationResult;
})
