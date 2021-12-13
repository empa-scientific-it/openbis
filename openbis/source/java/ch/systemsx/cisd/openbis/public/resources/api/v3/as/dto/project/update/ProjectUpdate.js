/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/FieldUpdateValue", "as/dto/attachment/update/AttachmentListUpdateValue" ], function(stjs, FieldUpdateValue, AttachmentListUpdateValue) {
	var ProjectUpdate = function() {
		this.spaceId = new FieldUpdateValue();
		this.description = new FieldUpdateValue();
		this.attachments = new AttachmentListUpdateValue();
	};
	stjs.extend(ProjectUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.project.update.ProjectUpdate';
		constructor.serialVersionUID = 1;
		prototype.projectId = null;
		prototype.freeze = null;
		prototype.freezeForExperiments = null;
		prototype.freezeForSamples = null;
		prototype.spaceId = null;
		prototype.description = null;
		prototype.attachments = null;

		prototype.getObjectId = function() {
			return this.getProjectId();
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
			this.spaceId.setValue(spaceId);
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description.setValue(description);
		};
		prototype.shouldBeFrozen = function() {
			return this.freeze;
		}
		prototype.freeze = function() {
			this.freeze = true;
		}
		prototype.shouldBeFrozenForExperiments = function() {
			return this.freezeForExperiments;
		}
		prototype.freezeForExperiments = function() {
			this.freeze = true;
			this.freezeForExperiments = true;
		}
		prototype.shouldBeFrozenForSamples = function() {
			return this.freezeForSamples;
		}
		prototype.freezeForSamples = function() {
			this.freeze = true;
			this.freezeForSamples = true;
		}
		prototype.getAttachments = function() {
			return this.attachments;
		};
		prototype.setAttachmentsActions = function(actions) {
			this.attachments.setActions(actions);
		};
	}, {
		projectId : "IProjectId",
		spaceId : {
			name : "FieldUpdateValue",
			arguments : [ "ISpaceId" ]
		},
		description : {
			name : "FieldUpdateValue",
			arguments : [ "String" ]
		},
		attachments : "AttachmentListUpdateValue"
	});
	return ProjectUpdate;
})