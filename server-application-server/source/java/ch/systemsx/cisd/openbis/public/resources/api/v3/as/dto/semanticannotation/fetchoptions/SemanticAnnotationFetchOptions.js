/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/entitytype/fetchoptions/EntityTypeFetchOptions", "as/dto/property/fetchoptions/PropertyTypeFetchOptions",
		"as/dto/property/fetchoptions/PropertyAssignmentFetchOptions", "as/dto/semanticannotation/fetchoptions/SemanticAnnotationSortOptions" ], function(require, stjs, FetchOptions) {
	var SemanticAnnotationFetchOptions = function() {
	};
	stjs.extend(SemanticAnnotationFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.semanticannotation.fetchoptions.SemanticAnnotationFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.entityType = null;
		prototype.propertyType = null;
		prototype.propertyAssignment = null;
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
		prototype.withPropertyAssignment = function() {
			if (this.propertyAssignment == null) {
				var PropertyAssignmentFetchOptions = require("as/dto/property/fetchoptions/PropertyAssignmentFetchOptions");
				this.propertyAssignment = new PropertyAssignmentFetchOptions();
			}
			return this.propertyAssignment;
		};
		prototype.withPropertyAssignmentUsing = function(fetchOptions) {
			return this.propertyAssignment = fetchOptions;
		};
		prototype.hasPropertyAssignment = function() {
			return this.propertyAssignment != null;
		};
		prototype.sortBy = function() {
			if (this.sort == null) {
				var SemanticAnnotationSortOptions = require("as/dto/semanticannotation/fetchoptions/SemanticAnnotationSortOptions");
				this.sort = new SemanticAnnotationSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		entityType : "EntityTypeFetchOptions",
		propertyType : "PropertyTypeFetchOptions",
		propertyAssignment : "PropertyAssignmentFetchOptions",
		sort : "SemanticAnnotationSortOptions"
	});
	return SemanticAnnotationFetchOptions;
})