/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/UpdateObjectsOperation" ], function(stjs, UpdateObjectsOperation) {
	var UpdateSpacesOperation = function(updates) {
		UpdateObjectsOperation.call(this, updates);
	};
	stjs.extend(UpdateSpacesOperation, UpdateObjectsOperation, [ UpdateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.space.update.UpdateSpacesOperation';
		prototype.getMessage = function() {
			return "UpdateSpacesOperation";
		};
	}, {});
	return UpdateSpacesOperation;
})