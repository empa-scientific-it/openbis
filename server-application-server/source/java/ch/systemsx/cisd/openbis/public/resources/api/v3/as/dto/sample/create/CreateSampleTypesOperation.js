/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/create/CreateObjectsOperation" ], function(stjs, CreateObjectsOperation) {
	var CreateSampleTypesOperation = function(creations) {
		CreateObjectsOperation.call(this, creations);
	};
	stjs.extend(CreateSampleTypesOperation, CreateObjectsOperation, [ CreateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.create.CreateSampleTypesOperation';
		prototype.getMessage = function() {
			return "CreateSampleTypesOperation";
		};
	}, {});
	return CreateSampleTypesOperation;
})