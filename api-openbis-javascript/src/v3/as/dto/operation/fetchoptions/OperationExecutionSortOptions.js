define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var OperationExecutionSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(OperationExecutionSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.operation.fetchoptions.OperationExecutionSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return OperationExecutionSortOptions;
})