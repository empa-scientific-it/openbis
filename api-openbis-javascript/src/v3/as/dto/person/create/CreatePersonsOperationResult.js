define([ "stjs", "as/dto/common/create/CreateObjectsOperationResult" ], function(stjs, CreateObjectsOperationResult) {
	var CreatePersonsOperationResult = function(objectIds) {
		CreateObjectsOperationResult.call(this, objectIds);
	};
	stjs.extend(CreatePersonsOperationResult, CreateObjectsOperationResult, [ CreateObjectsOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.person.create.CreatePersonsOperationResult';
		prototype.getMessage = function() {
			return "CreatePersonsOperationResult";
		};
	}, {});
	return CreatePersonsOperationResult;
})
