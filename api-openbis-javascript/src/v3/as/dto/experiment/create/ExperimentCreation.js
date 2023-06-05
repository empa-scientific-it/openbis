/**
 * @author pkupczyk
 */
define([ "stjs" ], function(stjs) {
	var ExperimentCreation = function() {
		this.properties = {};
	};
	stjs.extend(ExperimentCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.create.ExperimentCreation';
		constructor.serialVersionUID = 1;
		prototype.typeId = null;
		prototype.projectId = null;
		prototype.code = null;
		prototype.tagIds = null;
		prototype.properties = null;
		prototype.attachments = null;
		prototype.creationId = null;
        prototype.metaData = null;
		prototype.setTypeId = function(typeId) {
			this.typeId = typeId;
		};
		prototype.setProjectId = function(projectId) {
			this.projectId = projectId;
		};
		prototype.setCode = function(code) {
			this.code = code;
		};
		prototype.getTypeId = function() {
			return this.typeId;
		};
		prototype.getProjectId = function() {
			return this.projectId;
		};
		prototype.getCode = function() {
			return this.code;
		};
		prototype.getTagIds = function() {
			return this.tagIds;
		};
		prototype.setTagIds = function(tagIds) {
			this.tagIds = tagIds;
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
		prototype.getAttachments = function() {
			return this.attachments;
		};
		prototype.setAttachments = function(attachments) {
			this.attachments = attachments;
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
		projectId : "IProjectId",
		tagIds : {
			name : "List",
			arguments : [ "Object" ]
		},
		properties : {
			name : "Map",
			arguments : [ null, null ]
		},
		attachments : {
			name : "List",
			arguments : [ "AttachmentCreation" ]
		},
        creationId : "CreationId",
        metaData: {
             name: "Map",
             arguments: ["String", "String"]
        }
	});
	return ExperimentCreation;
})