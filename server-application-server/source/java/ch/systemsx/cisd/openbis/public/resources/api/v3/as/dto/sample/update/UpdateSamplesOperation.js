/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/UpdateObjectsOperation" ], function(stjs, UpdateObjectsOperation) {
	var UpdateSamplesOperation = function(updates) {
		UpdateObjectsOperation.call(this, updates);
	};
	stjs.extend(UpdateSamplesOperation, UpdateObjectsOperation, [ UpdateObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.update.UpdateSamplesOperation';
		prototype.getMessage = function() {
			return "UpdateSamplesOperation";
		};
	}, {});
	return UpdateSamplesOperation;
})