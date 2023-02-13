define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var OperationExecutionSummarySortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(OperationExecutionSummarySortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.operation.fetchoptions.OperationExecutionSummarySortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return OperationExecutionSummarySortOptions;
})