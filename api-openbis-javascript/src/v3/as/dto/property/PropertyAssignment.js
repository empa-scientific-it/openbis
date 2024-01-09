define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var PropertyAssignment = function() {
	};
	stjs.extend(PropertyAssignment, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.PropertyAssignment';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.permId = null;
		prototype.section = null;
		prototype.ordinal = null;
		prototype.entityType = null;
		prototype.propertyType = null;
		prototype.mandatory = null;
		prototype.showInEditView = null;
		prototype.showRawValueInForms = null;
		prototype.semanticAnnotations = null;
		prototype.semanticAnnotationsInherited = null;
		prototype.registrator = null;
		prototype.registrationDate = null;
		prototype.plugin = null;
		prototype.unique = null;

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
		prototype.getSection = function() {
			return this.section;
		};
		prototype.setSection = function(section) {
			this.section = section;
		};
		prototype.getOrdinal = function() {
			return this.ordinal;
		};
		prototype.setOrdinal = function(ordinal) {
			this.ordinal = ordinal;
		};
		prototype.getEntityType = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasEntityType()) {
				return this.entityType;
			} else {
				throw new exceptions.NotFetchedException("Entity type has not been fetched.");
			}
		};
		prototype.setEntityType = function(entityType) {
			this.entityType = entityType;
		};
		prototype.getPropertyType = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasPropertyType()) {
				return this.propertyType;
			} else {
				throw new exceptions.NotFetchedException("Property type has not been fetched.");
			}
		};
		prototype.setPropertyType = function(propertyType) {
			this.propertyType = propertyType;
		};
		prototype.isMandatory = function() {
			return this.mandatory;
		};
		prototype.setMandatory = function(mandatory) {
			this.mandatory = mandatory;
		};
		prototype.isShowInEditView = function() {
			return this.showInEditView;
		};
		prototype.setShowInEditView = function(showInEditView) {
			this.showInEditView = showInEditView;
		};
		prototype.isShowRawValueInForms = function() {
			return this.showRawValueInForms;
		};
		prototype.setShowRawValueInForms = function(showRawValueInForms) {
			this.showRawValueInForms = showRawValueInForms;
		};
		prototype.getSemanticAnnotations = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasSemanticAnnotations()) {
				return this.semanticAnnotations;
			} else {
				throw new exceptions.NotFetchedException("Semantic annotations have not been fetched.");
			}
		};
		prototype.setSemanticAnnotations = function(semanticAnnotations) {
			this.semanticAnnotations = semanticAnnotations;
		};
		prototype.isSemanticAnnotationsInherited = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasSemanticAnnotations()) {
				return this.semanticAnnotationsInherited;
			} else {
				throw new exceptions.NotFetchedException("Semantic annotations have not been fetched.");
			}
		};
		prototype.setSemanticAnnotationsInherited = function(semanticAnnotationsInherited) {
			this.semanticAnnotationsInherited = semanticAnnotationsInherited;
		};
		prototype.getRegistrator = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasRegistrator()) {
				return this.registrator;
			} else {
				throw new exceptions.NotFetchedException("Registrator has not been fetched.");
			}
		};
		prototype.setRegistrator = function(registrator) {
			this.registrator = registrator;
		};
		prototype.getRegistrationDate = function() {
			return this.registrationDate;
		};
		prototype.setRegistrationDate = function(registrationDate) {
			this.registrationDate = registrationDate;
		};
		prototype.getPlugin = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasPlugin()) {
				return this.plugin;
			} else {
				throw new exceptions.NotFetchedException("Plugin has not been fetched.");
			}
		};
		prototype.setPlugin = function(plugin) {
			this.plugin = plugin;
		};
		prototype.setUnique = function(unique) {
            this.unique = unique;
        };
		prototype.isUnique = function() {
            return this.unique;
        };
	}, {
		fetchOptions : "PropertyAssignmentFetchOptions",
		permId: "PropertyAssignmentPermId",
		entityType : "IEntityType",
		propertyType : "PropertyType",
		semanticAnnotations : {
			name : "List",
			arguments : [ "SemanticAnnotation" ]
		},
		semanticAnnotationsInherited : "Boolean",
		registrator : "Person",
		registrationDate : "Date",
		plugin : "Plugin"
	});
	return PropertyAssignment;
})