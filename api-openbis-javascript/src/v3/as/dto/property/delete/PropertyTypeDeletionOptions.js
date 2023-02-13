define([ "stjs", "as/dto/deletion/AbstractObjectDeletionOptions" ], function(stjs, AbstractObjectDeletionOptions) {
	var PropertyTypeDeletionOptions = function() {
		AbstractObjectDeletionOptions.call(this);
	};
	stjs.extend(PropertyTypeDeletionOptions, AbstractObjectDeletionOptions, [ AbstractObjectDeletionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.delete.PropertyTypeDeletionOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return PropertyTypeDeletionOptions;
})
