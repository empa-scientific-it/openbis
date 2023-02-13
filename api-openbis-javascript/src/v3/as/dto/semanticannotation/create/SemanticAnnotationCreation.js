/**
 * @author pkupczyk
 */
define([ "stjs" ], function(stjs) {
	var SemanticAnnotationCreation = function() {
	};
	stjs.extend(SemanticAnnotationCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.semanticannotation.create.SemanticAnnotationCreation';
		constructor.serialVersionUID = 1;
		prototype.entityTypeId = null;
		prototype.propertyTypeId = null;
		prototype.propertyAssignmentId = null;
		prototype.predicateOntologyId = null;
		prototype.predicateOntologyVersion = null;
		prototype.predicateAccessionId = null;
		prototype.descriptorOntologyId = null;
		prototype.descriptorOntologyVersion = null;
		prototype.descriptorAccessionId = null;

		prototype.getEntityTypeId = function() {
			return this.entityTypeId;
		};
		prototype.setEntityTypeId = function(entityTypeId) {
			this.entityTypeId = entityTypeId;
		};
		prototype.getPropertyTypeId = function() {
			return this.propertyTypeId;
		};
		prototype.setPropertyTypeId = function(propertyTypeId) {
			this.propertyTypeId = propertyTypeId;
		};
		prototype.getPropertyAssignmentId = function() {
			return this.propertyAssignmentId;
		};
		prototype.setPropertyAssignmentId = function(propertyAssignmentId) {
			this.propertyAssignmentId = propertyAssignmentId;
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
	}, {
		entityTypeId : "IEntityTypeId",
		propertyTypeId : "IPropertyTypeId",
		propertyAssignmentId : "IPropertyAssignmentId"
	});
	return SemanticAnnotationCreation;
})