define([ "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/property/fetchoptions/PropertyTypeFetchOptions", "as/dto/person/fetchoptions/PersonFetchOptions",
		"as/dto/plugin/fetchoptions/PluginFetchOptions", "as/dto/property/fetchoptions/PropertyAssignmentSortOptions", "as/dto/semanticannotation/fetchoptions/SemanticAnnotationFetchOptions" ], function(stjs, FetchOptions) {
	var PropertyAssignmentFetchOptions = function() {
	};
	stjs.extend(PropertyAssignmentFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.fetchoptions.PropertyAssignmentFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.entityType = null;
		prototype.propertyType = null;
		prototype.semanticAnnotations = null;
		prototype.registrator = null;
		prototype.plugin = null;
		prototype.sort = null;

		prototype.withEntityType = function() {
			if (this.entityType == null) {
				var EntityTypeFetchOptions = require("as/dto/entitytype/fetchoptions/EntityTypeFetchOptions");
				this.entityType = new EntityTypeFetchOptions();
			}
			return this.entityType;
		};
		prototype.withEntityTypeUsing = function(fetchOptions) {
			return this.entityType = fetchOptions;
		};
		prototype.hasEntityType = function() {
			return this.entityType != null;
		};
		prototype.withPropertyType = function() {
			if (this.propertyType == null) {
				var PropertyTypeFetchOptions = require("as/dto/property/fetchoptions/PropertyTypeFetchOptions");
				this.propertyType = new PropertyTypeFetchOptions();
			}
			return this.propertyType;
		};
		prototype.withPropertyTypeUsing = function(fetchOptions) {
			return this.propertyType = fetchOptions;
		};
		prototype.hasPropertyType = function() {
			return this.propertyType != null;
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
		prototype.withPlugin = function() {
			if (this.plugin == null) {
				var PluginFetchOptions = require("as/dto/plugin/fetchoptions/PluginFetchOptions");
				this.plugin = new PluginFetchOptions();
			}
			return this.plugin;
		};
		prototype.withPluginUsing = function(fetchOptions) {
			return this.plugin = fetchOptions;
		};
		prototype.hasPlugin = function() {
			return this.plugin != null;
		};
		prototype.sortBy = function() {
			if (this.sort == null) {
				var PropertyAssignmentSortOptions = require("as/dto/property/fetchoptions/PropertyAssignmentSortOptions");
				this.sort = new PropertyAssignmentSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		entityType : "EntityTypeFetchOptions",
		propertyType : "PropertyTypeFetchOptions",
		semanticAnnotations : "SemanticAnnotationFetchOptions",
		registrator : "PersonFetchOptions",
		plugin : "PluginFetchOptions",
		sort : "PropertyAssignmentSortOptions"
	});
	return PropertyAssignmentFetchOptions;
})