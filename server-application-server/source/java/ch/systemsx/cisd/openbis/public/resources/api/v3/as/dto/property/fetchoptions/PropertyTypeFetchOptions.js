define([ "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/vocabulary/fetchoptions/VocabularyFetchOptions", "as/dto/material/fetchoptions/MaterialTypeFetchOptions", 
		"as/dto/sample/fetchoptions/SampleTypeFetchOptions",
		"as/dto/semanticannotation/fetchoptions/SemanticAnnotationFetchOptions", "as/dto/person/fetchoptions/PersonFetchOptions", "as/dto/property/fetchoptions/PropertyTypeSortOptions" ], function(
		stjs, FetchOptions) {
	var PropertyTypeFetchOptions = function() {
	};
	stjs.extend(PropertyTypeFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.fetchoptions.PropertyTypeFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.vocabulary = null;
		prototype.materialType = null;
		prototype.sampleType = null;
		prototype.semanticAnnotations = null;
		prototype.registrator = null;
		prototype.sort = null;
		prototype.withVocabulary = function() {
			if (this.vocabulary == null) {
				var VocabularyFetchOptions = require("as/dto/vocabulary/fetchoptions/VocabularyFetchOptions");
				this.vocabulary = new VocabularyFetchOptions();
			}
			return this.vocabulary;
		};
		prototype.withVocabularyUsing = function(fetchOptions) {
			return this.vocabulary = fetchOptions;
		};
		prototype.hasVocabulary = function() {
			return this.vocabulary != null;
		};
		prototype.withMaterialType = function() {
			if (this.materialType == null) {
				var MaterialTypeFetchOptions = require("as/dto/material/fetchoptions/MaterialTypeFetchOptions");
				this.materialType = new MaterialTypeFetchOptions();
			}
			return this.materialType;
		};
		prototype.withMaterialTypeUsing = function(fetchOptions) {
			return this.materialType = fetchOptions;
		};
		prototype.hasMaterialType = function() {
			return this.materialType != null;
		};
		prototype.withSampleType = function() {
			if (this.sampleType == null) {
				var SampleTypeFetchOptions = require("as/dto/sample/fetchoptions/SampleTypeFetchOptions");
				this.sampleType = new SampleTypeFetchOptions();
			}
			return this.sampleType;
		};
		prototype.withSampleTypeUsing = function(fetchOptions) {
			return this.sampleType = fetchOptions;
		};
		prototype.hasSampleType = function() {
			return this.sampleType != null;
		};
		prototype.withSemanticAnnotations = function() {
			if (this.semanticAnnotations == null) {
				var SemanticAnnotationFetchOptions = require("as/dto/semanticannotation/fetchoptions/SemanticAnnotationFetchOptions");
				this.semanticAnnotations = new SemanticAnnotationFetchOptions();
			}
			return this.semanticAnnotations;
		};
		prototype.withSemanticAnnotationsUsing = function(fetchOptions) {
			return this.semanticAnnotations = fetchOptions;
		};
		prototype.hasSemanticAnnotations = function() {
			return this.semanticAnnotations != null;
		};
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
		prototype.sortBy = function() {
			if (this.sort == null) {
				var PropertyTypeSortOptions = require("as/dto/property/fetchoptions/PropertyTypeSortOptions");
				this.sort = new PropertyTypeSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		vocabulary : "VocabularyFetchOptions",
		materialType : "MaterialTypeFetchOptions",
		sampleType : "SampleTypeFetchOptions",
		semanticAnnotations : "SemanticAnnotationFetchOptions",
		registrator : "PersonFetchOptions",
		sort : "PropertyTypeSortOptions"
	});
	return PropertyTypeFetchOptions;
})