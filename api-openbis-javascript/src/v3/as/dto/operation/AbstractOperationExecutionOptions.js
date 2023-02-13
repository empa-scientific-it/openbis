/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/operation/IOperationExecutionOptions" ], function(stjs, IOperationExecutionOptions) {
	var AbstractOperationExecutionOptions = function() {
	};
	stjs.extend(AbstractOperationExecutionOptions, null, [ IOperationExecutionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.operation.AbstractOperationExecutionOptions';
		prototype.description = null;
		prototype.notification = null;
		prototype.availabilityTime = null;
		prototype.summaryAvailabilityTime = null;
		prototype.detailsAvailabilityTime = null;
		prototype.executeInOrder = false;

		prototype.setDescription = function(description) {
			this.description = description;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setNotification = function(notification) {
			this.notification = notification;
		};
		prototype.getNotification = function() {
			return this.notification;
		};
		prototype.setAvailabilityTime = function(availabilityTime) {
			this.availabilityTime = availabilityTime;
		};
		prototype.getAvailabilityTime = function() {
			return this.availabilityTime;
		};
		prototype.setSummaryAvailabilityTime = function(summaryAvailabilityTime) {
			this.summaryAvailabilityTime = summaryAvailabilityTime;
		};
		prototype.getSummaryAvailabilityTime = function() {
			return this.summaryAvailabilityTime;
		};
		prototype.setDetailsAvailabilityTime = function(detailsAvailabilityTime) {
			this.detailsAvailabilityTime = detailsAvailabilityTime;
		};
		prototype.getDetailsAvailabilityTime = function() {
			return this.detailsAvailabilityTime;
		};
		prototype.setExecuteInOrder = function(executeInOrder) {
			this.executeInOrder = executeInOrder;
		};
		prototype.isExecuteInOrder = function() {
			return this.executeInOrder;
		};

	}, {
		notification : "IOperationExecutionNotification"
	});
	return AbstractOperationExecutionOptions;
})