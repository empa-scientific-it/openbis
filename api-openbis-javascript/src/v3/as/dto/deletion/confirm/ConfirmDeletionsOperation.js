/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperation" ], function(stjs, IOperation) {
	var ConfirmDeletionsOperation = function(deletionIds) {
		this.deletionIds = deletionIds;
	};
	stjs.extend(ConfirmDeletionsOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.deletion.confirm.ConfirmDeletionsOperation';
		prototype.deletionIds = null;
		prototype.forceDeletion = false;
		prototype.forceDeletionOfDependentDeletions = false;
		prototype.getDeletionIds = function() {
			return this.deletionIds;
		};
		prototype.setForceDeletion = function(forceDeletion) {
			this.forceDeletion = forceDeletion;
		};
		prototype.isForceDeletion = function() {
			return this.forceDeletion;
		};
		prototype.setForceDeletionOfDependentDeletions = function(forceDeletionOfDependentDeletions) {
		    this.forceDeletionOfDependentDeletions = forceDeletionOfDependentDeletions;
		};
		prototype.isForceDeletionOfDependentDeletions = function() {
		    return this.forceDeletionOfDependentDeletions;
		};
		prototype.getMessage = function() {
			return "ConfirmDeletionsOperation";
		};
	}, {
		deletionIds : {
			name : "List",
			arguments : [ "IDeletionId" ]
		}
	});
	return ConfirmDeletionsOperation;
})