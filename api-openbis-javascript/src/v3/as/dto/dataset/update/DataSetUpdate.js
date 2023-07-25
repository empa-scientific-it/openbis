/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/FieldUpdateValue", "as/dto/common/update/IdListUpdateValue",
    "as/dto/common/update/ListUpdateMapValues"],
    function(stjs, FieldUpdateValue, IdListUpdateValue, ListUpdateMapValues) {
	var DataSetUpdate = function() {
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
	stjs.extend(DataSetUpdate, null, [], function(constructor, prototype) {
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
		prototype.properties = null;
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
		properties : {
			name : "Map",
			arguments : [ "String", "Serializable" ]
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