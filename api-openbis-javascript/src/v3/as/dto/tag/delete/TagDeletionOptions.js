/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/deletion/AbstractObjectDeletionOptions" ], function(stjs, AbstractObjectDeletionOptions) {
	var TagDeletionOptions = function() {
		AbstractObjectDeletionOptions.call(this);
	};
	stjs.extend(TagDeletionOptions, AbstractObjectDeletionOptions, [ AbstractObjectDeletionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.tag.delete.TagDeletionOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return TagDeletionOptions;
})