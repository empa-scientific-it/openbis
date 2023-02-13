define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/person/fetchoptions/PersonFetchOptions", "as/dto/operation/fetchoptions/OperationExecutionNotificationFetchOptions",
		"as/dto/operation/fetchoptions/OperationExecutionSummaryFetchOptions", "as/dto/operation/fetchoptions/OperationExecutionDetailsFetchOptions",
		"as/dto/operation/fetchoptions/OperationExecutionSortOptions" ], function(require, stjs, FetchOptions) {
	var OperationExecutionFetchOptions = function() {
	};
	stjs.extend(OperationExecutionFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.operation.fetchoptions.OperationExecutionFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.owner = null;
		prototype.notification = null;
		prototype.summary = null;
		prototype.details = null;
		prototype.sort = null;

		prototype.withOwner = function() {
			if (this.owner == null) {
				var PersonFetchOptions = require("as/dto/person/fetchoptions/PersonFetchOptions");
				this.owner = new PersonFetchOptions();
			}
			return this.owner;
		};
		prototype.withOwnerUsing = function(fetchOptions) {
			return this.owner = fetchOptions;
		};
		prototype.hasOwner = function() {
			return this.owner != null;
		};

		prototype.withNotification = function() {
			if (this.notification == null) {
				var OperationExecutionNotificationFetchOptions = require("as/dto/operation/fetchoptions/OperationExecutionNotificationFetchOptions");
				this.notification = new OperationExecutionNotificationFetchOptions();
			}
			return this.notification;
		};
		prototype.withNotificationUsing = function(fetchOptions) {
			return this.notification = fetchOptions;
		};
		prototype.hasNotification = function() {
			return this.notification != null;
		};

		prototype.withSummary = function() {
			if (this.summary == null) {
				var OperationExecutionSummaryFetchOptions = require("as/dto/operation/fetchoptions/OperationExecutionSummaryFetchOptions");
				this.summary = new OperationExecutionSummaryFetchOptions();
			}
			return this.summary;
		};
		prototype.withSummaryUsing = function(fetchOptions) {
			return this.summary = fetchOptions;
		};
		prototype.hasSummary = function() {
			return this.summary != null;
		};

		prototype.withDetails = function() {
			if (this.details == null) {
				var OperationExecutionDetailsFetchOptions = require("as/dto/operation/fetchoptions/OperationExecutionDetailsFetchOptions");
				this.details = new OperationExecutionDetailsFetchOptions();
			}
			return this.details;
		};
		prototype.withDetailsUsing = function(fetchOptions) {
			return this.details = fetchOptions;
		};
		prototype.hasDetails = function() {
			return this.details != null;
		};

		prototype.sortBy = function() {
			if (this.sort == null) {
				var OperationExecutionSortOptions = require("as/dto/operation/fetchoptions/OperationExecutionSortOptions");
				this.sort = new OperationExecutionSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		owner : "PersonFetchOptions",
		notification : "OperationExecutionNotificationFetchOptions",
		summary : "OperationExecutionSummaryFetchOptions",
		details : "OperationExecutionDetailsFetchOptions",
		sort : "OperationExecutionSortOptions"
	});
	return OperationExecutionFetchOptions;
})