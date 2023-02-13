define([ "stjs", "as/dto/deletion/AbstractObjectDeletionOptions" ], function(stjs, AbstractObjectDeletionOptions) {
	var DataSetTypeDeletionOptions = function() {
		AbstractObjectDeletionOptions.call(this);
	};
	stjs.extend(DataSetTypeDeletionOptions, AbstractObjectDeletionOptions, [ AbstractObjectDeletionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.delete.DataSetTypeDeletionOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return DataSetTypeDeletionOptions;
})
