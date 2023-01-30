/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/create/CreateObjectsOperation" ], function(stjs, CreateObjectsOperation) {
	var CreateMaterialsOperation = function(creations) {
		CreateObjectsOperation.call(this, creations);
	};
	stjs.extend(CreateMaterialsOperation, CreateObjectsOperation, [ CreateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.material.create.CreateMaterialsOperation';
		prototype.getMessage = function() {
			return "CreateMaterialsOperation";
		};
	}, {});
	return CreateMaterialsOperation;
})