define([ "require", "stjs", "as/dto/common/fetchoptions/EntityWithPropertiesSortOptions" ], function(require, stjs, EntityWithPropertiesSortOptions) {
	var ExperimentSortOptions = function() {
		EntityWithPropertiesSortOptions.call(this);
	};

	var fields = {
		IDENTIFIER : "IDENTIFIER"
	};

	stjs.extend(ExperimentSortOptions, EntityWithPropertiesSortOptions, [ EntityWithPropertiesSortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.fetchoptions.ExperimentSortOptions';
		constructor.serialVersionUID = 1;

		prototype.identifier = function() {
			return this.getOrCreateSorting(fields.IDENTIFIER);
		};
		prototype.getIdentifier = function() {
			return this.getSorting(fields.IDENTIFIER);
		};
	}, {});
	return ExperimentSortOptions;
})