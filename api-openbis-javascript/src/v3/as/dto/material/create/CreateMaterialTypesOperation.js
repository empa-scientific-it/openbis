/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/create/CreateObjectsOperation" ], function(stjs, CreateObjectsOperation) {
	var CreateMaterialTypesOperation = function(creations) {
		CreateObjectsOperation.call(this, creations);
	};
	stjs.extend(CreateMaterialTypesOperation, CreateObjectsOperation, [ CreateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.material.create.CreateMaterialTypesOperation';
		prototype.getMessage = function() {
			return "CreateMaterialTypesOperation";
		};
	}, {});
	return CreateMaterialTypesOperation;
})