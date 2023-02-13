define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/operation/fetchoptions/OperationExecutionNotificationSortOptions" ], function(require, stjs, FetchOptions) {
	var OperationExecutionNotificationFetchOptions = function() {
	};
	stjs.extend(OperationExecutionNotificationFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.operation.fetchoptions.OperationExecutionNotificationFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.sort = null;

		prototype.sortBy = function() {
			if (this.sort == null) {
				var OperationExecutionNotificationSortOptions = require("as/dto/operation/fetchoptions/OperationExecutionNotificationSortOptions");
				this.sort = new OperationExecutionNotificationSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		sort : "OperationExecutionNotificationSortOptions"
	});
	return OperationExecutionNotificationFetchOptions;
})