/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/entitytype/fetchoptions/EntityTypeSortOptions", 
         "as/dto/property/fetchoptions/PropertyAssignmentFetchOptions"  ], function(stjs, FetchOptions) {
	var EntityTypeFetchOptions = function() {
	};
	stjs.extend(EntityTypeFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.entitytype.fetchoptions.EntityTypeFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.sort = null;
		prototype.propertyAssignments = null;
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
		prototype.sortBy = function() {
			if (this.sort == null) {
				var EntityTypeSortOptions = require("as/dto/entitytype/fetchoptions/EntityTypeSortOptions");
				this.sort = new EntityTypeSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		sort : "EntityTypeSortOptions",
		propertyAssignments: "PropertyAssignmentFetchOptions"
	});
	return EntityTypeFetchOptions;
})