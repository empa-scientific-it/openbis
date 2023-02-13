define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var OperationExecutionDetailsSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(OperationExecutionDetailsSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.operation.fetchoptions.OperationExecutionDetailsSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return OperationExecutionDetailsSortOptions;
})