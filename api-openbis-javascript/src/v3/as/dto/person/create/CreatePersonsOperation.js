define([ "stjs", "as/dto/common/create/CreateObjectsOperation" ], function(stjs, CreateObjectsOperation) {
	var CreatePersonsOperation = function(creations) {
		CreateObjectsOperation.call(this, creations);
	};
	stjs.extend(CreatePersonsOperation, CreateObjectsOperation, [ CreateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.person.create.CreatePersonsOperation';
		prototype.getMessage = function() {
			return "CreatePersonsOperation";
		};
	}, {});
	return CreatePersonsOperation;
})
