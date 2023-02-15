/**
 * @author pkupczyk
 */
define([ "stjs" ], function(stjs) {
	var ProjectCreation = function() {
	};
	stjs.extend(ProjectCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.project.create.ProjectCreation';
		constructor.serialVersionUID = 1;
		prototype.spaceId = null;
		prototype.code = null;
		prototype.description = null;
		prototype.leaderId = null;
		prototype.attachments = null;
		prototype.creationId = null;

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
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description = description;
		};
		prototype.getLeaderId = function() {
			return this.leaderId;
		};
		prototype.setLeaderId = function(leaderId) {
			this.leaderId = leaderId;
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
		spaceId : "ISpaceId",
		leaderId : "IPersonId",
		attachments : {
			name : "List",
			arguments : [ "AttachmentCreation" ]
		},
		creationId : "CreationId"
	});
	return ProjectCreation;
})