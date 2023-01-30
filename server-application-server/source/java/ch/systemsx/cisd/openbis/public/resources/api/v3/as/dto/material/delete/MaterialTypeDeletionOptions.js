define([ "stjs", "as/dto/deletion/AbstractObjectDeletionOptions" ], function(stjs, AbstractObjectDeletionOptions) {
	var MaterialTypeDeletionOptions = function() {
		AbstractObjectDeletionOptions.call(this);
	};
	stjs.extend(MaterialTypeDeletionOptions, AbstractObjectDeletionOptions, [ AbstractObjectDeletionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.material.delete.MaterialTypeDeletionOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return MaterialTypeDeletionOptions;
})
