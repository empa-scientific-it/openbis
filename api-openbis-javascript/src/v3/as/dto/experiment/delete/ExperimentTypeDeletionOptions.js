define([ "stjs", "as/dto/deletion/AbstractObjectDeletionOptions" ], function(stjs, AbstractObjectDeletionOptions) {
	var ExperimentTypeDeletionOptions = function() {
		AbstractObjectDeletionOptions.call(this);
	};
	stjs.extend(ExperimentTypeDeletionOptions, AbstractObjectDeletionOptions, [ AbstractObjectDeletionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.delete.ExperimentTypeDeletionOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return ExperimentTypeDeletionOptions;
})
