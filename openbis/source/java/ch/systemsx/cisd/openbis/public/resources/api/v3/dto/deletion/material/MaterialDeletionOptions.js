/**
 * @author pkupczyk
 */
define([ "stjs", "dto/deletion/AbstractObjectDeletionOptions" ], function(stjs, AbstractObjectDeletionOptions) {
	var MaterialDeletionOptions = function() {
		AbstractObjectDeletionOptions.call(this);
	};
	stjs.extend(MaterialDeletionOptions, AbstractObjectDeletionOptions, [ AbstractObjectDeletionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.deletion.material.MaterialDeletionOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return MaterialDeletionOptions;
})