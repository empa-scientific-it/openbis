/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/sample/fetchoptions/SampleTypeSortOptions", "as/dto/property/fetchoptions/PropertyAssignmentFetchOptions",
         "as/dto/plugin/fetchoptions/PluginFetchOptions", "as/dto/semanticannotation/fetchoptions/SemanticAnnotationFetchOptions" ], function(stjs, FetchOptions) {
	var SampleTypeFetchOptions = function() {
	};
	stjs.extend(SampleTypeFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.fetchoptions.SampleTypeFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.sort = null;
		prototype.propertyAssignments = null;
		prototype.validationPlugin = null;
		prototype.semanticAnnotations = null;
		prototype.withPropertyAssignments = function() {
			if (this.propertyAssignments == null) {
				var PropertyAssignmentFetchOptions = require("as/dto/property/fetchoptions/PropertyAssignmentFetchOptions");
				this.propertyAssignments = new PropertyAssignmentFetchOptions();
			}
			return this.propertyAssignments;
		};
		prototype.withPropertyAssignmentsUsing = function(fetchOptions) {
			return this.propertyAssignments = fetchOptions;
		};
		prototype.hasPropertyAssignments = function() {
			return this.propertyAssignments != null;
		};
		prototype.withValidationPlugin = function() {
			if (this.validationPlugin == null) {
				var PluginFetchOptions = require("as/dto/plugin/fetchoptions/PluginFetchOptions");
				this.validationPlugin = new PluginFetchOptions();
			}
			return this.validationPlugin;
		};
		prototype.withValidationPluginUsing = function(fetchOptions) {
			return this.validationPlugin = fetchOptions;
		};
		prototype.hasValidationPlugin = function() {
			return this.validationPlugin != null;
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
		prototype.sortBy = function() {
			if (this.sort == null) {
				var SampleTypeSortOptions = require("as/dto/sample/fetchoptions/SampleTypeSortOptions");
				this.sort = new SampleTypeSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		propertyAssignments : "PropertyAssignmentFetchOptions",
		validationPlugin : "PluginFetchOptions",
		semanticAnnotations : "SemanticAnnotationFetchOptions",
		sort : "SampleTypeSortOptions"
	});
	return SampleTypeFetchOptions;
})