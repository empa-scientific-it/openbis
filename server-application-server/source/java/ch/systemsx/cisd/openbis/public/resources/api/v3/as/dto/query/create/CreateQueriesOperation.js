/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/create/CreateObjectsOperation" ], function(stjs, CreateObjectsOperation) {
	var CreateQueriesOperation = function(creations) {
		CreateObjectsOperation.call(this, creations);
	};
	stjs.extend(CreateQueriesOperation, CreateObjectsOperation, [ CreateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.query.create.CreateQueriesOperation';
		prototype.getMessage = function() {
			return "CreateQueriesOperation";
		};
	}, {});
	return CreateQueriesOperation;
})