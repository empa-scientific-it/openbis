/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/entity/AbstractEntityUpdate", "as/dto/common/update/FieldUpdateValue",
    "as/dto/common/update/IdListUpdateValue", "as/dto/common/update/ListUpdateMapValues"],
    function(stjs, AbstractEntityUpdate, FieldUpdateValue, IdListUpdateValue, ListUpdateMapValues) {
	var DataSetUpdate = function() {
	    AbstractEntityUpdate.call(this);
		this.experimentId = new FieldUpdateValue();
		this.sampleId = new FieldUpdateValue();
		this.physicalData = new FieldUpdateValue();
		this.linkedData = new FieldUpdateValue();
		this.properties = {};
		this.tagIds = new IdListUpdateValue();
		this.containerIds = new IdListUpdateValue();
		this.componentIds = new IdListUpdateValue();
		this.parentIds = new IdListUpdateValue();
		this.childIds = new IdListUpdateValue();
		this.metaData = new ListUpdateMapValues();
	};
	stjs.extend(DataSetUpdate, AbstractEntityUpdate, [AbstractEntityUpdate], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.update.DataSetUpdate';
		constructor.serialVersionUID = 1;
		prototype.dataSetId = null;
		prototype.freeze = null;
		prototype.freezeForChildren = null;
		prototype.freezeForParents = null;
		prototype.freezeForComponents = null;
		prototype.freezeForContainers = null;
		prototype.experimentId = null;
		prototype.sampleId = null;
		prototype.physicalData = null;
		prototype.linkedData = null;
		prototype.tagIds = null;
		prototype.containerIds = null;
		prototype.componentIds = null;
		prototype.parentIds = null;
		prototype.childIds = null;
		prototype.metaData = null;

		prototype.getObjectId = function() {
			return this.getDataSetId();
		};
		prototype.getDataSetId = function() {
			return this.dataSetId;
		};
		prototype.setDataSetId = function(dataSetId) {
			this.dataSetId = dataSetId;
		};
		prototype.shouldBeFrozen = function() {
			return this.freeze;
		}
		prototype.freeze = function() {
			this.freeze = true;
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
		prototype.shouldBeFrozenForComponents = function() {
			return this.freezeForComponents;
		}
		prototype.freezeForComponents = function() {
			this.freeze = true;
			this.freezeForComponents = true;
		}
		prototype.shouldBeFrozenForContainers = function() {
			return this.freezeForContainers;
		}
		prototype.freezeForContainers = function() {
			this.freeze = true;
			this.freezeForContainers = true;
		}
		prototype.getExperimentId = function() {
			return this.experimentId;
		};
		prototype.setExperimentId = function(experimentId) {
			this.experimentId.setValue(experimentId);
		};
		prototype.getSampleId = function() {
			return this.sampleId;
		};
		prototype.setSampleId = function(sampleId) {
			this.sampleId.setValue(sampleId);
		};
		prototype.getPhysicalData = function() {
			return this.physicalData;
		};
		prototype.setPhysicalData = function(physicalData) {
			this.physicalData.setValue(physicalData);
		};
		prototype.getLinkedData = function() {
			return this.linkedData;
		};
		prototype.setLinkedData = function(linkedData) {
			this.linkedData.setValue(linkedData);
		};
		prototype.getTagIds = function() {
			return this.tagIds;
		};
		prototype.setTagActions = function(actions) {
			this.tagIds.setActions(actions);
		};
		prototype.getContainerIds = function() {
			return this.containerIds;
		};
		prototype.setContainerActions = function(actions) {
			this.containerIds.setActions(actions);
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
		prototype.getMetaData = function() {
            return this.metaData;
        };
        prototype.setMetaDataActions = function(actions) {
            this.metaData.setActions(actions);
        };
	}, {
		dataSetId : "IDataSetId",
		experimentId : {
			name : "FieldUpdateValue",
			arguments : [ "IExperimentId" ]
		},
		sampleId : {
			name : "FieldUpdateValue",
			arguments : [ "ISampleId" ]
		},
		physicalData : {
			name : "FieldUpdateValue",
			arguments : [ "PhysicalDataUpdate" ]
		},
		linkedData : {
			name : "FieldUpdateValue",
			arguments : [ "LinkedDataUpdate" ]
		},
		tagIds : {
			name : "IdListUpdateValue",
			arguments : [ "ITagId" ]
		},
		containerIds : {
			name : "IdListUpdateValue",
			arguments : [ "IDataSetId" ]
		},
		componentIds : {
			name : "IdListUpdateValue",
			arguments : [ "IDataSetId" ]
		},
		parentIds : {
			name : "IdListUpdateValue",
			arguments : [ "IDataSetId" ]
		},
		childIds : {
			name : "IdListUpdateValue",
			arguments : [ "IDataSetId" ]
		},
        metaData : "ListUpdateMapValues"
	});
	return DataSetUpdate;
})