/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/UpdateObjectsOperation" ], function(stjs, UpdateObjectsOperation) {
	var UpdateQueriesOperation = function(updates) {
		UpdateObjectsOperation.call(this, updates);
	};
	stjs.extend(UpdateQueriesOperation, UpdateObjectsOperation, [ UpdateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.query.update.UpdateQueriesOperation';
		prototype.getMessage = function() {
			return "UpdateQueriesOperation";
		};
	}, {});
	return UpdateQueriesOperation;
})