/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/entity/AbstractEntityCreation" ], function(stjs, AbstractEntityCreation) {
	var ExperimentCreation = function() {
	    AbstractEntityCreation.call(this);
		this.properties = {};
	};
	stjs.extend(ExperimentCreation, AbstractEntityCreation, [AbstractEntityCreation], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.create.ExperimentCreation';
		constructor.serialVersionUID = 1;
		prototype.typeId = null;
		prototype.projectId = null;
		prototype.code = null;
		prototype.tagIds = null;
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