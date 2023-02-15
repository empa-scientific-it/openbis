/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/UpdateObjectsOperation" ], function(stjs, UpdateObjectsOperation) {
	var UpdateExternalDmsOperation = function(updates) {
		UpdateObjectsOperation.call(this, updates);
	};
	stjs.extend(UpdateExternalDmsOperation, UpdateObjectsOperation, [ UpdateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.externaldms.update.UpdateExternalDmsOperation';
		prototype.getMessage = function() {
			return "UpdateExternalDmsOperation";
		};
	}, {});
	return UpdateExternalDmsOperation;
})