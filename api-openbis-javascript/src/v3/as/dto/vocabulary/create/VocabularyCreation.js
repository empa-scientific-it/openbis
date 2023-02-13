define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var VocabularyCreation = function() {
	};
	stjs.extend(VocabularyCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.create.VocabularyCreation';
		constructor.serialVersionUID = 1;
		prototype.code = null;
		prototype.description = null;
		prototype.managedInternally = null;
		prototype.chosenFromList = null;
		prototype.urlTemplate = null;
		prototype.terms = null;
		prototype.getCode = function() {
			return this.code;
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
			return this.terms;
		};
		prototype.setTerms = function(terms) {
			this.terms = terms;
		};
	}, {
		terms : {
			name : "List",
			arguments : [ "Object" ]
		}
	});
	return VocabularyCreation;
})
