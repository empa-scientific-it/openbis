/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/create/CreateObjectsOperation" ], function(stjs, CreateObjectsOperation) {
	var CreateSamplesOperation = function(creations) {
		CreateObjectsOperation.call(this, creations);
	};
	stjs.extend(CreateSamplesOperation, CreateObjectsOperation, [ CreateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.create.CreateSamplesOperation';
		prototype.getMessage = function() {
			return "CreateSamplesOperation";
		};
	}, {});
	return CreateSamplesOperation;
})