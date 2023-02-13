define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var OperationExecution = function() {
	};
	stjs.extend(OperationExecution, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.operation.OperationExecution';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.permId = null;
		prototype.code = null;
		prototype.state = null;
		prototype.owner = null;
		prototype.description = null;
		prototype.notification = null;
		prototype.availability = null;
		prototype.availabilityTime = null;
		prototype.summary = null;
		prototype.summaryAvailability = null;
		prototype.summaryAvailabilityTime = null;
		prototype.details = null;
		prototype.detailsAvailability = null;
		prototype.detailsAvailabilityTime = null;
		prototype.creationDate = null;
		prototype.startDate = null;
		prototype.finishDate = null;

		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};
		prototype.getPermId = function() {
			return this.permId;
		};
		prototype.setPermId = function(permId) {
			this.permId = permId;
		};
		prototype.getCode = function() {
			return this.code;
		};
		prototype.setCode = function(code) {
			this.code = code;
		};
		prototype.getState = function() {
			return this.state;
		};
		prototype.setState = function(state) {
			this.state = state;
		};
		prototype.getOwner = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasOwner()) {
				return this.owner;
			} else {
				throw new exceptions.NotFetchedException("Owner has not been fetched.");
			}
		};
		prototype.setOwner = function(owner) {
			this.owner = owner;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description = description;
		};
		prototype.getNotification = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasNotification()) {
				return this.notification;
			} else {
				throw new exceptions.NotFetchedException("Notification has not been fetched.");
			}
		};
		prototype.setNotification = function(notification) {
			this.notification = notification;
		};
		prototype.getAvailability = function() {
			return this.availability;
		};
		prototype.setAvailability = function(availability) {
			this.availability = availability;
		};
		prototype.getAvailabilityTime = function() {
			return this.availabilityTime;
		};
		prototype.setAvailabilityTime = function(availabilityTime) {
			this.availabilityTime = availabilityTime;
		};
		prototype.getSummary = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasSummary()) {
				return this.summary;
			} else {
				throw new exceptions.NotFetchedException("Summary has not been fetched.");
			}
		};
		prototype.setSummary = function(summary) {
			this.summary = summary;
		};
		prototype.getSummaryAvailability = function() {
			return this.summaryAvailability;
		};
		prototype.setSummaryAvailability = function(summaryAvailability) {
			this.summaryAvailability = summaryAvailability;
		};
		prototype.getSummaryAvailabilityTime = function() {
			return this.summaryAvailabilityTime;
		};
		prototype.setSummaryAvailabilityTime = function(summaryAvailabilityTime) {
			this.summaryAvailabilityTime = summaryAvailabilityTime;
		};
		prototype.getDetails = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasDetails()) {
				return this.details;
			} else {
				throw new exceptions.NotFetchedException("Details has not been fetched.");
			}
		};
		prototype.setDetails = function(details) {
			this.details = details;
		};
		prototype.getDetailsAvailability = function() {
			return this.detailsAvailability;
		};
		prototype.setDetailsAvailability = function(detailsAvailability) {
			this.detailsAvailability = detailsAvailability;
		};
		prototype.getDetailsAvailabilityTime = function() {
			return this.detailsAvailabilityTime;
		};
		prototype.setDetailsAvailabilityTime = function(detailsAvailabilityTime) {
			this.detailsAvailabilityTime = detailsAvailabilityTime;
		};
		prototype.getCreationDate = function() {
			return this.creationDate;
		};
		prototype.setCreationDate = function(creationDate) {
			this.creationDate = creationDate;
		};
		prototype.getStartDate = function() {
			return this.startDate;
		};
		prototype.setStartDate = function(startDate) {
			this.startDate = startDate;
		};
		prototype.getFinishDate = function() {
			return this.finishDate;
		};
		prototype.setFinishDate = function(finishDate) {
			this.finishDate = finishDate;
		};
		prototype.toString = function() {
			return "OperationExecution code: " + this.code;
		};
	}, {
		fetchOptions : "OperationExecutionFetchOptions",
		permId : "OperationExecutionPermId",
		state : "OperationExecutionState",
		owner : "Person",
		notification : "IOperationExecutionNotification",
		availability : "OperationExecutionAvailability",
		summary : "OperationExecutionSummary",
		summaryAvailability : "OperationExecutionAvailability",
		details : "OperationExecutionDetails",
		detailsAvailability : "OperationExecutionAvailability",
		creationDate : "Date",
		startDate : "Date",
		finishDate : "Date"
	});
	return OperationExecution;
})