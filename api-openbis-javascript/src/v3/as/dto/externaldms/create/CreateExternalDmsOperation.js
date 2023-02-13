/**
 * @author anttil
 */
define([ "stjs", "as/dto/common/create/CreateObjectsOperation" ], function(stjs, CreateObjectsOperation) {
	var CreateExternalDmsOperation = function(creations) {
		CreateObjectsOperation.call(this, creations);
	};
	stjs.extend(CreateExternalDmsOperation, CreateObjectsOperation, [ CreateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.externaldms.create.CreateExternalDmsOperation';
		prototype.getMessage = function() {
			return "CreateExternalDmsOperation";
		};
	}, {});
	return CreateExternalDmsOperation;
})