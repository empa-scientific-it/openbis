define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var ExperimentTypeSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(ExperimentTypeSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.fetchoptions.ExperimentTypeSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return ExperimentTypeSortOptions;
})