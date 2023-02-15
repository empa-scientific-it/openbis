/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/create/CreateObjectsOperation" ], function(stjs, CreateObjectsOperation) {
	var CreateDataSetsOperation = function(creations) {
		CreateObjectsOperation.call(this, creations);
	};
	stjs.extend(CreateDataSetsOperation, CreateObjectsOperation, [ CreateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.create.CreateDataSetsOperation';
		prototype.getMessage = function() {
			return "CreateDataSetsOperation";
		};
	}, {});
	return CreateDataSetsOperation;
})