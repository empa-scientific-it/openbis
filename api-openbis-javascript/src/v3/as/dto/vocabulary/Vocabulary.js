/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "util/Exceptions", "as/dto/vocabulary/id/VocabularyPermId" ], function(stjs, exceptions) {
	var Vocabulary = function() {
	};
	stjs.extend(Vocabulary, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.Vocabulary';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.code = null;
		prototype.description = null;
		prototype.registrationDate = null;
		prototype.registrator = null;
		prototype.modificationDate = null;
		prototype.managedInternally = null;
		prototype.chosenFromList = null;
		prototype.urlTemplate = null;
		prototype.terms = null;

		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};
		prototype.getCode = function() {
			return this.code;
		};
		prototype.getPermId = function() {
			var VocabularyPermId = require("as/dto/vocabulary/id/VocabularyPermId");
			return new VocabularyPermId(this.code);
		};
		prototype.setCode = function(code) {
			this.code = code;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description = description;
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
		prototype.isManagedInternally = function() {
			return this.managedInternally;
		};
		prototype.setManagedInternally = function(managedInternally) {
			this.managedInternally = managedInternally;
		};
		prototype.isInternalNameSpace = function() {
			return this.isManagedInternally();
		};
		prototype.setInternalNameSpace = function(internalNameSpace) {
			this.setManagedInternally(internalNameSpace);
		};
		prototype.isChosenFromList = function() {
			return this.chosenFromList;
		};
		prototype.setChosenFromList = function(chosenFromList) {
			this.chosenFromList = chosenFromList;
		};
		prototype.getUrlTemplate = function() {
			return this.urlTemplate;
		};
		prototype.setUrlTemplate = function(urlTemplate) {
			this.urlTemplate = urlTemplate;
		};
		prototype.getTerms = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasTerms()) {
				return this.terms;
			} else {
				throw new exceptions.NotFetchedException("Terms have not been fetched.");
			}
		};
		prototype.setTerms = function(terms) {
			this.terms = terms;
		};
	}, {
		fetchOptions : "VocabularyFetchOptions",
		registrationDate : "Date",
		registrator : "Person",
		modificationDate : "Date",
		terms : {
			name : "List",
			arguments : [ "VocabularyTerm" ]
		}
	});
	return Vocabulary;
})