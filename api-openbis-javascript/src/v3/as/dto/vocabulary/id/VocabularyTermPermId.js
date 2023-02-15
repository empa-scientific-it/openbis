/**
 * Vocabulary term code.
 * 
 * @author pkupczyk
 */
define([ "stjs", "util/Exceptions", "as/dto/vocabulary/id/IVocabularyTermId" ], function(stjs, exceptions, IVocabularyTermId) {
	/**
	 * @param code
	 *            Vocabulary term code, e.g. "MY_TERM".
     * @param vocabularyCode Vocabulary code, e.g. "MY_VOCABULARY"
	 */
	var VocabularyTermPermId = function(code, vocabularyCode) {
		this.setCode(code);
		this.setVocabularyCode(vocabularyCode);
	};
	stjs.extend(VocabularyTermPermId, null, [ IVocabularyTermId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.id.VocabularyTermPermId';
		constructor.serialVersionUID = 1;
		prototype.code = null;
		prototype.vocabularyCode = null;
		prototype.getCode = function() {
			return this.code;
		};
		prototype.setCode = function(code) {
			if (code == null) {
				throw new exceptions.IllegalArgumentException("Code cannot be null");
			}
			this.code = code;
		};
		prototype.getVocabularyCode = function() {
			return this.vocabularyCode;
		};
		prototype.setVocabularyCode = function(vocabularyCode) {
			if (vocabularyCode == null) {
//				throw new exceptions.IllegalArgumentException("Vocabulary code cannot be null");
			}
			this.vocabularyCode = vocabularyCode;
		};
		prototype.toString = function() {
			return this.getCode() + " (" + this.getVocabularyCode() + ")";
		};
	}, {});
	return VocabularyTermPermId;
})