/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/entity/AbstractEntityUpdate", "as/dto/common/update/FieldUpdateValue", "as/dto/common/update/IdListUpdateValue",
			"as/dto/attachment/update/AttachmentListUpdateValue", "as/dto/common/update/RelationshipUpdate",
			"as/dto/common/update/ListUpdateMapValues" ],
			function(stjs, AbstractEntityUpdate, FieldUpdateValue, IdListUpdateValue, AttachmentListUpdateValue,
			RelationshipUpdate, ListUpdateMapValues) {
	var SampleUpdate = function() {
	    AbstractEntityUpdate.call(this);
		this.properties = {};
		this.experimentId = new FieldUpdateValue();
		this.projectId = new FieldUpdateValue();		
		this.spaceId = new FieldUpdateValue();
		this.tagIds = new IdListUpdateValue();
		this.containerId = new FieldUpdateValue();
		this.componentIds = new IdListUpdateValue();
		this.parentIds = new IdListUpdateValue();
		this.childIds = new IdListUpdateValue();
		this.relationships = {};
		this.attachments = new AttachmentListUpdateValue();
		this.metaData = new ListUpdateMapValues();
	};
	stjs.extend(SampleUpdate, AbstractEntityUpdate, [AbstractEntityUpdate], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.update.SampleUpdate';
		constructor.serialVersionUID = 1;
		prototype.sampleId = null;
		prototype.freeze = null;
		prototype.freezeForComponents = null;
		prototype.freezeForChildren = null;
		prototype.freezeForParents = null;
		prototype.freezeForDataSets = null;
		prototype.experimentId = null;
		prototype.projectId = null;
		prototype.spaceId = null;
		prototype.tagIds = null;
		prototype.containerId = null;
		prototype.componentIds = null;
		prototype.parentIds = null;
		prototype.childIds = null;
		prototype.relationships = null;
		prototype.attachments = null;
		prototype.metaData = null;

		prototype.getObjectId = function() {
			return this.getSampleId();
		};
		prototype.getSampleId = function() {
			return this.sampleId;
		};
		prototype.setSampleId = function(sampleId) {
			this.sampleId = sampleId;
		};
		prototype.shouldBeFrozen = function() {
			return this.freeze;
		}
		prototype.freeze = function() {
			this.freeze = true;
		}
		prototype.shouldBeFrozenForComponents = function() {
			return this.freezeForComponents;
		}
		prototype.freezeForComponents = function() {
			this.freeze = true;
			this.freezeForComponents = true;
		}
		prototype.shouldBeFrozenForChildren = function() {
			return this.freezeForChildren;
		}
		prototype.freezeForChildren = function() {
			this.freeze = true;
			this.freezeForChildren = true;
		}
		prototype.shouldBeFrozenForParents = function() {
			return this.freezeForParents;
		}
		prototype.freezeForParents = function() {
			this.freeze = true;
			this.freezeForParents = true;
		}
		prototype.shouldBeFrozenForDataSets = function() {
			return this.freezeForDataSets;
		}
		prototype.freezeForDataSets = function() {
			this.freeze = true;
			this.freezeForDataSets = true;
		}
		prototype.getProjectId = function() {
			return this.projectId;
		};
		prototype.setProjectId = function(projectId) {
			this.projectId.setValue(projectId);
		};		
		prototype.getExperimentId = function() {
			return this.experimentId;
		};
		prototype.setExperimentId = function(experimentId) {
			this.experimentId.setValue(experimentId);
		};
		prototype.getSpaceId = function() {
			return this.spaceId;
		};
		prototype.setSpaceId = function(spaceId) {
			this.spaceId.setValue(spaceId);
		};
		prototype.getContainerId = function() {
			return this.containerId;
		};
		prototype.setContainerId = function(containerId) {
			this.containerId.setValue(containerId);
		};
		prototype.getTagIds = function() {
			return this.tagIds;
		};
		prototype.setTagActions = function(actions) {
			this.tagIds.setActions(actions);
		};
		prototype.getComponentIds = function() {
			return this.componentIds;
		};
		prototype.setComponentActions = function(actions) {
			this.componentIds.setActions(actions);
		};
		prototype.getParentIds = function() {
			return this.parentIds;
		};
		prototype.setParentActions = function(actions) {
			this.parentIds.setActions(actions);
		};
		prototype.getChildIds = function() {
			return this.childIds;
		};
		prototype.setChildActions = function(actions) {
			this.childIds.setActions(actions);
		};
		prototype.getAttachments = function() {
			return this.attachments;
		};
		prototype.getRelationships = function() {
			return this.relationships;
		};
		prototype.relationship = function(sampleId) {
			var relationshipUpdate = this.relationships[sampleId];
			if (relationshipUpdate == null) {
				relationshipUpdate = new RelationshipUpdate();
				this.relationships[sampleId] = relationshipUpdate;
			}
			return relationshipUpdate;
		};
		prototype.setRelationships = function(relationships) {
			this.relationships = relationships;
		};
		prototype.setAttachmentsActions = function(actions) {
			this.attachments.setActions(actions);
		};
		prototype.getMetaData = function() {
            return this.metaData;
        };
        prototype.setMetaDataActions = function(actions) {
            this.metaData.setActions(actions);
        };
	}, {
		sampleId : "ISampleId",
		experimentId : {
			name : "FieldUpdateValue",
			arguments : [ "IExperimentId" ]
		},
		projectId : {
			name : "FieldUpdateValue",
			arguments : [ "IProjectId" ]
		},
		spaceId : {
			name : "FieldUpdateValue",
			arguments : [ "ISpaceId" ]
		},
		tagIds : {
			name : "IdListUpdateValue",
			arguments : [ "ITagId" ]
		},
		containerId : {
			name : "FieldUpdateValue",
			arguments : [ "ISampleId" ]
		},
		componentIds : {
			name : "IdListUpdateValue",
			arguments : [ "ISampleId" ]
		},
		parentIds : {
			name : "IdListUpdateValue",
			arguments : [ "ISampleId" ]
		},
		childIds : {
			name : "IdListUpdateValue",
			arguments : [ "ISampleId" ]
		},
		relationships : {
			name : "Map",
			arguments : [ "ISampleId", "RelationshipUpdate" ]
		},
		attachments : "AttachmentListUpdateValue",
		metaData : "ListUpdateMapValues"
	});
	return SampleUpdate;
})