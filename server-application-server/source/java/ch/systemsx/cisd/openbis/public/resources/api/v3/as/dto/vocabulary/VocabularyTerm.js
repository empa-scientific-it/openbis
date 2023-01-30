/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var VocabularyTerm = function() {
	};
	stjs.extend(VocabularyTerm, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.VocabularyTerm';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.permId = null;
		prototype.code = null;
		prototype.label = null;
		prototype.description = null;
		prototype.ordinal = null;
		prototype.official = null;
		prototype.vocabulary = null;
		prototype.registrationDate = null;
		prototype.registrator = null;
		prototype.modificationDate = null;

		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};
		prototype.getPermId = function() {
			return this.permId;
		};
		prototype.setPermId = function(permId) {
			this.permId = permId;
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
		prototype.getOrdinal = function() {
			return this.ordinal;
		};
		prototype.setOrdinal = function(ordinal) {
			this.ordinal = ordinal;
		};
		prototype.isOfficial = function() {
			return this.official;
		};
		prototype.setOfficial = function(official) {
			this.official = official;
		};
		prototype.getVocabulary = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasVocabulary()) {
				return this.vocabulary;
			} else {
				throw new exceptions.NotFetchedException("Vocabulary has not been fetched.");
			}
		};
		prototype.setVocabulary = function(vocabulary) {
			this.vocabulary = vocabulary;
		};
		prototype.getRegistrationDate = function() {
			return this.registrationDate;
		};
		prototype.setRegistrationDate = function(registrationDate) {
			this.registrationDate = registrationDate;
		};
		prototype.getRegistrator = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasRegistrator()) {
				return this.registrator;
			} else {
				throw new exceptions.NotFetchedException("Registrator has not been fetched.");
			}
		};
		prototype.setRegistrator = function(registrator) {
			this.registrator = registrator;
		};
		prototype.getModificationDate = function() {
			return this.modificationDate;
		};
		prototype.setModificationDate = function(modificationDate) {
			this.modificationDate = modificationDate;
		};
	}, {
		fetchOptions : "VocabularyTermFetchOptions",
		permId : "VocabularyTermPermId",
		vocabulary : "Vocabulary",
		registrationDate : "Date",
		registrator : "Person",
		modificationDate : "Date"
	});
	return VocabularyTerm;
})