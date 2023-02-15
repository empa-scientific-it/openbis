define([ "stjs", "as/dto/deletion/AbstractObjectDeletionOptions", "as/dto/vocabulary/delete/VocabularyTermReplacement" ], function(stjs, AbstractObjectDeletionOptions, VocabularyTermReplacement) {
	var VocabularyTermDeletionOptions = function() {
		AbstractObjectDeletionOptions.call(this);
		this.replacements = [];
	};
	stjs.extend(VocabularyTermDeletionOptions, AbstractObjectDeletionOptions, [ AbstractObjectDeletionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.delete.VocabularyTermDeletionOptions';
		constructor.serialVersionUID = 1;
		prototype.replacements = null;
		prototype.replace = function(replacedId, replacementId) {
			this.replacements.push(new VocabularyTermReplacement(replacedId, replacementId));
		};
		prototype.getReplacements = function() {
			return this.replacements;
		};
	}, {
		replacements: {
			name: "List",
			arguments: ["VocabularyTermReplacement"]
		}
	});
	return VocabularyTermDeletionOptions;
})
