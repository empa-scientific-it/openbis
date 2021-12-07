/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/FieldUpdateValue" ], function(stjs, FieldUpdateValue) {
	var OperationExecutionUpdate = function() {
		this.description = new FieldUpdateValue();
		this.deleteSummaryRequested = false;
		this.deleteDetailsRequested = false;
	};
	stjs.extend(OperationExecutionUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.operation.update.OperationExecutionUpdate';
		constructor.serialVersionUID = 1;
		prototype.executionId = null;
		prototype.description = null;
		prototype.deleteSummaryRequested = null;
		prototype.deleteDetailsRequested = null;

		prototype.getObjectId = function() {
			return this.getExecutionId();
		};
		prototype.getExecutionId = function() {
			return this.executionId;
		};
		prototype.setExecutionId = function(executionId) {
			this.executionId = executionId;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description.setValue(description);
		};
		prototype.deleteSummary = function() {
			this.deleteSummaryRequested = true;
		};
		prototype.isDeleteSummary = function() {
			return this.deleteSummaryRequested;
		};
		prototype.deleteDetails = function() {
			this.deleteDetailsRequested = true;
		};
		prototype.isDeleteDetails = function() {
			return this.deleteDetailsRequested;
		};
	}, {
		executionId : "IOperationExecutionId",
		description : {
			name : "FieldUpdateValue",
			arguments : [ "String" ]
		}
	});
	return OperationExecutionUpdate;
})