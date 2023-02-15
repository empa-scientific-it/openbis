/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperation" ], function(stjs, IOperation) {
	var RevertDeletionsOperation = function(deletionIds) {
		this.deletionIds = deletionIds;
	};
	stjs.extend(RevertDeletionsOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.deletion.revert.RevertDeletionsOperation';
		prototype.deletionIds = null;
		prototype.getDeletionIds = function() {
			return this.deletionIds;
		};
		prototype.getMessage = function() {
			return "RevertDeletionsOperation";
		};
	}, {
		deletionIds : {
			name : "List",
			arguments : [ "IDeletionId" ]
		}
	});
	return RevertDeletionsOperation;
})