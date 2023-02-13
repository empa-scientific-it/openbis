/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/create/CreateObjectsOperation" ], function(stjs, CreateObjectsOperation) {
	var CreateDataSetTypesOperation = function(creations) {
		CreateObjectsOperation.call(this, creations);
	};
	stjs.extend(CreateDataSetTypesOperation, CreateObjectsOperation, [ CreateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.create.CreateDataSetTypesOperation';
		prototype.getMessage = function() {
			return "CreateDataSetTypesOperation";
		};
	}, {});
	return CreateDataSetTypesOperation;
})