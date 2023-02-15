define([ "stjs" ], function(stjs) {
	var VocabularyTermReplacement = function(replacedId, replacementId) {
		this.replacedId = replacedId ? replacedId : null;
		this.replacementId = replacementId ? replacementId : null;
	};
	stjs.extend(VocabularyTermReplacement, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.delete.VocabularyTermReplacement';
		constructor.serialVersionUID = 1;
		prototype.replacedId = null;
		prototype.replacementId = null;

		prototype.getReplacedId = function() {
			return this.replacedId;
		};
		prototype.setReplacedId = function(replacedId) {
			this.replacedId = replacedId;
		};
		prototype.getReplacementId = function() {
			return this.replacementId;
		};
		prototype.setReplacementId = function(replacementId) {
			this.replacementId = replacementId;
		};

	}, {
		replacedId : "IVocabularyTermId",
		replacementId : "IVocabularyTermId"
	});
	return VocabularyTermReplacement;
})
