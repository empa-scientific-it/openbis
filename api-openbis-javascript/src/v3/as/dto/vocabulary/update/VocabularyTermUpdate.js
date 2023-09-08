/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/FieldUpdateValue" ], function(stjs, FieldUpdateValue) {
	var VocabularyTermUpdate = function() {
		this.label = new FieldUpdateValue();
		this.description = new FieldUpdateValue();
		this.previousTermId = new FieldUpdateValue();
		this.official = new FieldUpdateValue();
	};
	stjs.extend(VocabularyTermUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.update.VocabularyTermUpdate';
		constructor.serialVersionUID = 1;
		prototype.vocabularyTermId = null;
		prototype.label = null;
		prototype.description = null;
		prototype.previousTermId = null;
		prototype.official = null;

		prototype.getObjectId = function() {
			return this.getVocabularyTermId();
		};
		prototype.getVocabularyTermId = function() {
			return this.vocabularyTermId;
		};
		prototype.setVocabularyTermId = function(vocabularyTermId) {
			this.vocabularyTermId = vocabularyTermId;
		};
		prototype.getLabel = function() {
			return this.label;
		};
		prototype.setLabel = function(label) {
			this.label.setValue(label);
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description.setValue(description);
		};
		prototype.getPreviousTermId = function() {
			return this.previousTermId;
		};
		prototype.setPreviousTermId = function(previousTermId) {
			this.previousTermId.setValue(previousTermId);
		};
		prototype.getOfficial = function() {
			return this.official;
		};
		prototype.setOfficial = function(official) {
			this.official.setValue(official);
		};
	}, {
		vocabularyTermId : "IVocabularyTermId",
		previousTermId : {
			name: "FieldUpdateValue",
			arguments: ["IVocabularyTermId"]
		},
		label: {
			name: "FieldUpdateValue",
			arguments: ["String"]
		},
		description: {
			name: "FieldUpdateValue",
			arguments: ["String"]
		},
		official: {
			name: "FieldUpdateValue",
			arguments: ["Boolean"]
		}
	});
	return VocabularyTermUpdate;
})