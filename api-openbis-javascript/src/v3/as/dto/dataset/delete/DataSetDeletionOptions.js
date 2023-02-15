/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/deletion/AbstractObjectDeletionOptions" ], function(stjs, AbstractObjectDeletionOptions) {
	var DataSetDeletionOptions = function() {
		AbstractObjectDeletionOptions.call(this);
	};
	stjs.extend(DataSetDeletionOptions, AbstractObjectDeletionOptions, [ AbstractObjectDeletionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.delete.DataSetDeletionOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return DataSetDeletionOptions;
})