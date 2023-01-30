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
		creationId : "CreationId"
	});
	return ExperimentCreation;
})