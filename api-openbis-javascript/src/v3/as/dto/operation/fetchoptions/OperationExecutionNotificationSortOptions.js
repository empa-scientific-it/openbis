define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var OperationExecutionNotificationSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(OperationExecutionNotificationSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.operation.fetchoptions.OperationExecutionNotificationSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return OperationExecutionNotificationSortOptions;
})