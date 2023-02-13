define([ "stjs", "as/dto/deletion/AbstractObjectDeletionOptions" ], function(stjs, AbstractObjectDeletionOptions) {
	var VocabularyDeletionOptions = function() {
		AbstractObjectDeletionOptions.call(this);
	};
	stjs.extend(VocabularyDeletionOptions, AbstractObjectDeletionOptions, [ AbstractObjectDeletionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.delete.VocabularyDeletionOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return VocabularyDeletionOptions;
})
