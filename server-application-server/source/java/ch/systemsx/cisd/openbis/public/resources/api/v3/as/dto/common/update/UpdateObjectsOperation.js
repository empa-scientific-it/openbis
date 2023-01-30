/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperation" ], function(stjs, IOperation) {
	var UpdateObjectsOperation = function(updates) {
		this.updates = updates;
	};
	stjs.extend(UpdateObjectsOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.update.UpdateObjectsOperation';
		prototype.updates = null;
		prototype.getUpdates = function() {
			return this.updates;
		};
		prototype.getMessage = function() {
			return "UpdateObjectsOperation";
		};
	}, {
		updates : {
			name : "List",
			arguments : [ "IObjectUpdate" ]
		}
	});
	return UpdateObjectsOperation;
})