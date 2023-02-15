/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var SemanticAnnotation = function() {
	};
	stjs.extend(SemanticAnnotation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.semanticannotation.SemanticAnnotation';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.permId = null;
		prototype.entityType = null;
		prototype.propertyType = null;
		prototype.propertyAssignment = null;
		prototype.predicateOntologyId = null;
		prototype.predicateOntologyVersion = null;
		prototype.predicateAccessionId = null;
		prototype.descriptorOntologyId = null;
		prototype.descriptorOntologyVersion = null;
		prototype.descriptorAccessionId = null;
		prototype.creationDate = null;

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
		prototype.getEntityType = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasEntityType()) {
				return this.entityType;
			} else {
				throw new exceptions.NotFetchedException("EntityType has not been fetched.");
			}
		};
		prototype.setEntityType = function(entityType) {
			this.entityType = entityType;
		};
		prototype.getPropertyType = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasPropertyType()) {
				return this.propertyType;
			} else {
				throw new exceptions.NotFetchedException("PropertyType has not been fetched.");
			}
		};
		prototype.setPropertyType = function(propertyType) {
			this.propertyType = propertyType;
		};
		prototype.getPropertyAssignment = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasPropertyAssignment()) {
				return this.propertyAssignment;
			} else {
				throw new exceptions.NotFetchedException("PropertyAssignment has not been fetched.");
			}
		};
		prototype.setPropertyAssignment = function(propertyAssignment) {
			this.propertyAssignment = propertyAssignment;
		};
		prototype.getPredicateOntologyId = function() {
			return this.predicateOntologyId;
		};
		prototype.setPredicateOntologyId = function(predicateOntologyId) {
			this.predicateOntologyId = predicateOntologyId;
		};
		prototype.getPredicateOntologyVersion = function() {
			return this.predicateOntologyVersion;
		};
		prototype.setPredicateOntologyVersion = function(predicateOntologyVersion) {
			this.predicateOntologyVersion = predicateOntologyVersion;
		};
		prototype.getPredicateAccessionId = function() {
			return this.predicateAccessionId;
		};
		prototype.setPredicateAccessionId = function(predicateAccessionId) {
			this.predicateAccessionId = predicateAccessionId;
		};
		prototype.getDescriptorOntologyId = function() {
			return this.descriptorOntologyId;
		};
		prototype.setDescriptorOntologyId = function(descriptorOntologyId) {
			this.descriptorOntologyId = descriptorOntologyId;
		};
		prototype.getDescriptorOntologyVersion = function() {
			return this.descriptorOntologyVersion;
		};
		prototype.setDescriptorOntologyVersion = function(descriptorOntologyVersion) {
			this.descriptorOntologyVersion = descriptorOntologyVersion;
		};
		prototype.getDescriptorAccessionId = function() {
			return this.descriptorAccessionId;
		};
		prototype.setDescriptorAccessionId = function(descriptorAccessionId) {
			this.descriptorAccessionId = descriptorAccessionId;
		};
		prototype.getCreationDate = function() {
			return this.creationDate;
		};
		prototype.setCreationDate = function(creationDate) {
			this.creationDate = creationDate;
		};

	}, {
		fetchOptions : "SemanticAnnotationFetchOptions",
		permId : "SemanticAnnotationPermId",
		entityType : "IEntityType",
		propertyType : "PropertyType",
		propertyAssignment : "PropertyAssignment",
		creationDate : "Date"
	});
	return SemanticAnnotation;
})