/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/deletion/AbstractObjectDeletionOptions" ], function(stjs, AbstractObjectDeletionOptions) {
	var SampleDeletionOptions = function() {
		AbstractObjectDeletionOptions.call(this);
	};
	stjs.extend(SampleDeletionOptions, AbstractObjectDeletionOptions, [ AbstractObjectDeletionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.delete.SampleDeletionOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return SampleDeletionOptions;
})