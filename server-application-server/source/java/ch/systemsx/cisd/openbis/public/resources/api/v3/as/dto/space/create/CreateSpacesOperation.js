/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/create/CreateObjectsOperation" ], function(stjs, CreateObjectsOperation) {
	var CreateSpacesOperation = function(creations) {
		CreateObjectsOperation.call(this, creations);
	};
	stjs.extend(CreateSpacesOperation, CreateObjectsOperation, [ CreateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.space.create.CreateSpacesOperation';
		prototype.getMessage = function() {
			return "CreateSpacesOperation";
		};
	}, {});
	return CreateSpacesOperation;
})