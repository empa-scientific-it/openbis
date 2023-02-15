/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/person/fetchoptions/PersonFetchOptions", 
         "as/dto/vocabulary/fetchoptions/VocabularyTermFetchOptions", "as/dto/vocabulary/fetchoptions/VocabularySortOptions" ], function(require, stjs,
		FetchOptions) {
	var VocabularyFetchOptions = function() {
	};
	stjs.extend(VocabularyFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.fetchoptions.VocabularyFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.registrator = null;
		prototype.terms = null;
		prototype.sort = null;
		prototype.withRegistrator = function() {
			if (this.registrator == null) {
				var PersonFetchOptions = require("as/dto/person/fetchoptions/PersonFetchOptions");
				this.registrator = new PersonFetchOptions();
			}
			return this.registrator;
		};
		prototype.withRegistratorUsing = function(fetchOptions) {
			return this.registrator = fetchOptions;
		};
		prototype.hasRegistrator = function() {
			return this.registrator != null;
		};
		prototype.withTerms = function() {
			if (this.terms == null) {
				var VocabularyTermFetchOptions = require("as/dto/vocabulary/fetchoptions/VocabularyTermFetchOptions");
				this.terms = new VocabularyTermFetchOptions();
			}
			return this.terms;
		};
		prototype.withTermsUsing = function(fetchOptions) {
			return this.terms = fetchOptions;
		};
		prototype.hasTerms = function() {
			return this.terms != null;
		};
		prototype.sortBy = function() {
			if (this.sort == null) {
				var VocabularySortOptions = require("as/dto/vocabulary/fetchoptions/VocabularySortOptions");
				this.sort = new VocabularySortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		registrator : "PersonFetchOptions",
		terms : "VocabularyTermFetchOptions",
		sort : "VocabularySortOptions"
	});
	return VocabularyFetchOptions;
})