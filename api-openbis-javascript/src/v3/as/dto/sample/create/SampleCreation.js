/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/Relationship" ], function(stjs, Relationship) {
	var SampleCreation = function() {
		this.properties = {};
		this.relationships = {};
	};
	stjs.extend(SampleCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.create.SampleCreation';
		constructor.serialVersionUID = 1;
		prototype.typeId = null;
		prototype.experimentId = null;
		prototype.projectId = null;		
		prototype.spaceId = null;
		prototype.code = null;
		prototype.tagIds = null;
		prototype.containerId = null;
		prototype.componentIds = null;
		prototype.parentIds = null;
		prototype.childIds = null;
		prototype.relationships = null;
		prototype.properties = null;
		prototype.attachments = null;
		prototype.creationId = null;
		prototype.autoGeneratedCode = null;
		prototype.metaData = null;
		prototype.getTypeId = function() {
			return this.typeId;
		};
		prototype.setTypeId = function(typeId) {
			this.typeId = typeId;
		};
		prototype.getExperimentId = function() {
			return this.experimentId;
		};
		prototype.setExperimentId = function(experimentId) {
			this.experimentId = experimentId;
		};
		prototype.getProjectId = function() {
			return this.projectId;
		};
		prototype.setProjectId = function(projectId) {
			this.projectId = projectId;
		};
		prototype.getSpaceId = function() {
			return this.spaceId;
		};
		prototype.setSpaceId = function(spaceId) {
			this.spaceId = spaceId;
		};
		prototype.getCode = function() {
			return this.code;
		};
		prototype.setCode = function(code) {
			this.code = code;
		};
		prototype.isAutoGeneratedCode = function() {
			return this.autoGeneratedCode;
		}
		prototype.setAutoGeneratedCode = function(autoGeneratedCode) {
			this.autoGeneratedCode = autoGeneratedCode;
		};
		prototype.getTagIds = function() {
			return this.tagIds;
		};
		prototype.setTagIds = function(tagIds) {
			this.tagIds = tagIds;
		};
		prototype.getContainerId = function() {
			return this.containerId;
		};
		prototype.setContainerId = function(containerId) {
			this.containerId = containerId;
		};
		prototype.getComponentIds = function() {
			return this.componentIds;
		};
		prototype.setComponentIds = function(componentIds) {
			this.componentIds = componentIds;
		};
		prototype.getParentIds = function() {
			return this.parentIds;
		};
		prototype.setParentIds = function(parentIds) {
			this.parentIds = parentIds;
		};
		prototype.getChildIds = function() {
			return this.childIds;
		};
		prototype.setChildIds = function(childIds) {
			this.childIds = childIds;
		};
		prototype.getRelationships = function() {
			return this.relationships;
		};
		prototype.setRelationships = function(relationships) {
			this.relationships = relationships;
		};
		prototype.relationship = function(sampleId) {
			var relationship = this.relationships[sampleId];
			if (relationship == null) {
				relationship = new Relationship();
				this.relationships[sampleId] = relationship;
			};
			return relationship;
		};
		prototype.getAttachments = function() {
			return this.attachments;
		};
		prototype.setAttachments = function(attachments) {
			this.attachments = attachments;
		};
		prototype.getProperty = function(propertyName) {
			return this.properties[propertyName];
		};
		prototype.setProperty = function(propertyName, propertyValue) {
			this.properties[propertyName] = propertyValue;
		};
		prototype.getProperties = function() {
			return this.properties;
		};
		prototype.setProperties = function(properties) {
			this.properties = properties;
		};
		prototype.getIntegerProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setIntegerProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getVarcharProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setVarcharProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getMultilineVarcharProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setMultilineVarcharProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getRealProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setRealProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getTimestampProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setTimestampProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getBooleanProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setBooleanProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getControlledVocabularyProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setControlledVocabularyProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getSampleProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setSampleProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getHyperlinkProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setHyperlinkProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getXmlProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setXmlProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getIntegerArrayProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setIntegerArrayProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getRealArrayProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setRealArrayProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getStringArrayProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setStringArrayProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getTimestampArrayProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setTimestampArrayProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getJsonProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setJsonProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
		prototype.getCreationId = function() {
			return this.creationId;
		};
		prototype.setCreationId = function(creationId) {
			this.creationId = creationId;
		};
		prototype.getMetaData = function() {
            return this.metaData;
        };
        prototype.setMetaData = function(metaData) {
            this.metaData = metaData;
        };
	}, {
		typeId : "IEntityTypeId",
		experimentId : "IExperimentId",
		projectId : "IProjectId",
		spaceId : "ISpaceId",
		tagIds : {
			name : "List",
			arguments : [ "Object" ]
		},
		properties : {
			name : "Map",
			arguments : [ null, null ]
		},
		containerId : "ISampleId",
		componentIds : {
			name : "List",
			arguments : [ "Object" ]
		},
		parentIds : {
			name : "List",
			arguments : [ "Object" ]
		},
		childIds : {
			name : "List",
			arguments : [ "Object" ]
		},
		relationships : {
			name : "Map",
			arguments : [ null, "Relationship" ]
		},
		attachments : {
			name : "List",
			arguments : [ "AttachmentCreation" ]
		},
		creationId : "CreationId"
	});
	return SampleCreation;
})