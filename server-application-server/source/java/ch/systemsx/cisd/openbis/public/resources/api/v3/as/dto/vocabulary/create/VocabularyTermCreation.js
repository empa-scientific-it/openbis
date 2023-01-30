define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var VocabularyTermCreation = function() {
	};
	stjs.extend(VocabularyTermCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.create.VocabularyTermCreation';
		constructor.serialVersionUID = 1;
		prototype.vocabularyId = null;
		prototype.code = null;
		prototype.label = null;
		prototype.description = null;
		prototype.official = true;
		prototype.previousTermId = null;
		prototype.getVocabularyId = function() {
			return this.vocabularyId;
		};
		prototype.setVocabularyId = function(vocabularyId) {
			this.vocabularyId = vocabularyId;
		};
		prototype.getCode = function() {
			return this.code;
		};
		prototype.setCode = function(code) {
			this.code = code;
		};
		prototype.getLabel = function() {
			return this.label;
		};
		prototype.setLabel = function(label) {
			this.label = label;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description = description;
		};
		prototype.isOfficial = function() {
			return this.official;
		};
		prototype.setOfficial = function(official) {
			this.official = official;
		};
		prototype.getPreviousTermId = function() {
			return this.previousTermId;
		};
		prototype.setPreviousTermId = function(previousTermId) {
			this.previousTermId = previousTermId;
		};
	}, {});
	return VocabularyTermCreation;
})

