/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/FieldUpdateValue" ], function(stjs, FieldUpdateValue) {
	var SemanticAnnotationUpdate = function() {
		this.predicateOntologyId = new FieldUpdateValue();
		this.predicateOntologyVersion = new FieldUpdateValue();
		this.predicateAccessionId = new FieldUpdateValue();
		this.descriptorOntologyId = new FieldUpdateValue();
		this.descriptorOntologyVersion = new FieldUpdateValue();
		this.descriptorAccessionId = new FieldUpdateValue();
	};
	stjs.extend(SemanticAnnotationUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.semanticannotation.update.SemanticAnnotationUpdate';
		constructor.serialVersionUID = 1;
		prototype.semanticAnnotationId = null;
		prototype.predicateOntologyId = null;
		prototype.predicateOntologyVersion = null;
		prototype.predicateAccessionId = null;
		prototype.descriptorOntologyId = null;
		prototype.descriptorOntologyVersion = null;
		prototype.descriptorAccessionId = null;

		prototype.getObjectId = function() {
			return this.getSemanticAnnotationId();
		};
		prototype.getSemanticAnnotationId = function() {
			return this.semanticAnnotationId;
		};
		prototype.setSemanticAnnotationId = function(semanticAnnotationId) {
			this.semanticAnnotationId = semanticAnnotationId;
		};
		prototype.getPredicateOntologyId = function() {
			return this.predicateOntologyId;
		};
		prototype.setPredicateOntologyId = function(predicateOntologyId) {
			this.predicateOntologyId.setValue(predicateOntologyId);
		};
		prototype.getPredicateOntologyVersion = function() {
			return this.predicateOntologyVersion;
		};
		prototype.setPredicateOntologyVersion = function(predicateOntologyVersion) {
			this.predicateOntologyVersion.setValue(predicateOntologyVersion);
		};
		prototype.getPredicateAccessionId = function() {
			return this.predicateAccessionId;
		};
		prototype.setPredicateAccessionId = function(predicateAccessionId) {
			this.predicateAccessionId.setValue(predicateAccessionId);
		};
		prototype.getDescriptorOntologyId = function() {
			return this.descriptorOntologyId;
		};
		prototype.setDescriptorOntologyId = function(descriptorOntologyId) {
			this.descriptorOntologyId.setValue(descriptorOntologyId);
		};
		prototype.getDescriptorOntologyVersion = function() {
			return this.descriptorOntologyVersion;
		};
		prototype.setDescriptorOntologyVersion = function(descriptorOntologyVersion) {
			this.descriptorOntologyVersion.setValue(descriptorOntologyVersion);
		};
		prototype.getDescriptorAccessionId = function() {
			return this.descriptorAccessionId;
		};
		prototype.setDescriptorAccessionId = function(descriptorAccessionId) {
			this.descriptorAccessionId.setValue(descriptorAccessionId);
		};
	}, {
		semanticAnnotationId : "ISemanticAnnotationId",
		predicateOntologyId: {
			name: "FieldUpdateValue",
			arguments: ["String"]
		},
		predicateOntologyVersion: {
			name: "FieldUpdateValue",
			arguments: ["String"]
		},
		predicateAccessionId: {
			name: "FieldUpdateValue",
			arguments: ["String"]
		},
		descriptorOntologyId: {
			name: "FieldUpdateValue",
			arguments: ["String"]
		},
		descriptorOntologyVersion: {
			name: "FieldUpdateValue",
			arguments: ["String"]
		},
		descriptorAccessionId: {
			name: "FieldUpdateValue",
			arguments: ["String"]
		}
	});
	return SemanticAnnotationUpdate;
})