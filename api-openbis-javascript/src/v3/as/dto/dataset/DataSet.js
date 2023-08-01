/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var DataSet = function() {
	};
	stjs.extend(DataSet, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.DataSet';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.permId = null;
		prototype.code = null;
		prototype.frozen = null;
		prototype.frozenForChildren = null;
		prototype.frozenForParents = null;
		prototype.frozenForComponents = null;
		prototype.frozenForContainers = null;
		prototype.accessDate = null;
		prototype.measured = null;
		prototype.postRegistered = null;
		prototype.parents = null;
		prototype.children = null;
		prototype.containers = null;
		prototype.components = null;
		prototype.physicalData = null;
		prototype.linkedData = null;
		prototype.tags = null;
		prototype.type = null;
		prototype.kind = null;
		prototype.dataStore = null;
		prototype.history = null;
        prototype.propertiesHistory = null;
        prototype.experimentHistory = null;
        prototype.sampleHistory = null;
        prototype.parentsHistory = null;
        prototype.childrenHistory = null;
        prototype.containersHistory = null;
        prototype.componentsHistory = null;
        prototype.contentCopiesHistory = null;
        prototype.unknownHistory = null;
		prototype.modificationDate = null;
		prototype.modifier = null;
		prototype.registrationDate = null;
		prototype.registrator = null;
		prototype.experiment = null;
		prototype.sample = null;
		prototype.properties = null;
		prototype.materialProperties = null;
		prototype.sampleProperties = null;
		prototype.dataProducer = null;
		prototype.dataProductionDate = null;
		prototype.metaData = null;

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
		prototype.getCode = function() {
			return this.code;
		};
		prototype.setCode = function(code) {
			this.code = code;
		};
		prototype.isFrozen = function() {
			return this.frozen;
		};
		prototype.setFrozen = function(frozen) {
			this.frozen = frozen;
		};
		prototype.isFrozenForChildren = function() {
			return this.frozenForChildren;
		};
		prototype.setFrozenForChildren = function(frozenForChildren) {
			this.frozenForChildren = frozenForChildren;
		};
		prototype.isFrozenForParents = function() {
			return this.frozenForParents;
		};
		prototype.setFrozenForParents = function(frozenForParents) {
			this.frozenForParents = frozenForParents;
		};
		prototype.isFrozenForComponents = function() {
			return this.frozenForComponents;
		};
		prototype.setFrozenForComponents = function(frozenForComponents) {
			this.frozenForComponents = frozenForComponents;
		};
		prototype.isFrozenForContainers = function() {
			return this.frozenForContainers;
		};
		prototype.setFrozenForContainers = function(frozenForContainers) {
			this.frozenForContainers = frozenForContainers;
		};
		prototype.getAccessDate = function() {
			return this.accessDate;
		};
		prototype.setAccessDate = function(accessDate) {
			this.accessDate = accessDate;
		};
		prototype.isMeasured = function() {
			return this.measured;
		};
		prototype.setMeasured = function(measured) {
			this.measured = measured;
		};
		prototype.isPostRegistered = function() {
			return this.postRegistered;
		};
		prototype.setPostRegistered = function(postRegistered) {
			this.postRegistered = postRegistered;
		};
		prototype.getParents = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasParents()) {
				return this.parents;
			} else {
				throw new exceptions.NotFetchedException("Parents has not been fetched.");
			}
		};
		prototype.setParents = function(parents) {
			this.parents = parents;
		};
		prototype.getChildren = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasChildren()) {
				return this.children;
			} else {
				throw new exceptions.NotFetchedException("Children has not been fetched.");
			}
		};
		prototype.setChildren = function(children) {
			this.children = children;
		};
		prototype.getContainers = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasContainers()) {
				return this.containers;
			} else {
				throw new exceptions.NotFetchedException("Container data sets has not been fetched.");
			}
		};
		prototype.setContainers = function(containers) {
			this.containers = containers;
		};
		prototype.getComponents = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasComponents()) {
				return this.components;
			} else {
				throw new exceptions.NotFetchedException("Component data sets has not been fetched.");
			}
		};
		prototype.setComponents = function(components) {
			this.components = components;
		};
		prototype.getPhysicalData = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasPhysicalData()) {
				return this.physicalData;
			} else {
				throw new exceptions.NotFetchedException("Physical data has not been fetched.");
			}
		};
		prototype.setPhysicalData = function(physicalData) {
			this.physicalData = physicalData;
		};
		prototype.getLinkedData = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasLinkedData()) {
				return this.linkedData;
			} else {
				throw new exceptions.NotFetchedException("Linked data has not been fetched.");
			}
		};
		prototype.setLinkedData = function(linkedData) {
			this.linkedData = linkedData;
		};
		prototype.getTags = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasTags()) {
				return this.tags;
			} else {
				throw new exceptions.NotFetchedException("Tags has not been fetched.");
			}
		};
		prototype.setTags = function(tags) {
			this.tags = tags;
		};
		prototype.getType = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasType()) {
				return this.type;
			} else {
				throw new exceptions.NotFetchedException("Data Set type has not been fetched.");
			}
		};
		prototype.setType = function(type) {
			this.type = type;
		};
        prototype.getKind = function() {
            return this.kind;
        };
        prototype.setKind = function(kind) {
            this.kind = kind;
        };
		prototype.getDataStore = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasDataStore()) {
				return this.dataStore;
			} else {
				throw new exceptions.NotFetchedException("Data store has not been fetched.");
			}
		};
		prototype.setDataStore = function(dataStore) {
			this.dataStore = dataStore;
		};
		prototype.getHistory = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasHistory()) {
				return this.history;
			} else {
				throw new exceptions.NotFetchedException("History has not been fetched.");
			}
		};
		prototype.setHistory = function(history) {
			this.history = history;
		};

		prototype.getPropertiesHistory = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasPropertiesHistory()) {
				return this.propertiesHistory;
			} else {
				throw new exceptions.NotFetchedException("Properties history has not been fetched.");
			}
		};
		prototype.setPropertiesHistory = function(propertiesHistory) {
			this.propertiesHistory = propertiesHistory;
		};

		prototype.getExperimentHistory = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasExperimentHistory()) {
				return this.experimentHistory;
			} else {
				throw new exceptions.NotFetchedException("Experiment history has not been fetched.");
			}
		};
		prototype.setExperimentHistory = function(experimentHistory) {
			this.experimentHistory = experimentHistory;
		};

		prototype.getSampleHistory = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasSampleHistory()) {
				return this.sampleHistory;
			} else {
				throw new exceptions.NotFetchedException("Sample history has not been fetched.");
			}
		};
		prototype.setSampleHistory = function(sampleHistory) {
			this.sampleHistory = sampleHistory;
		};

		prototype.getParentsHistory = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasParentsHistory()) {
				return this.parentsHistory;
			} else {
				throw new exceptions.NotFetchedException("Parents history has not been fetched.");
			}
		};
		prototype.setParentsHistory = function(parentsHistory) {
			this.parentsHistory = parentsHistory;
		};

		prototype.getChildrenHistory = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasChildrenHistory()) {
				return this.childrenHistory;
			} else {
				throw new exceptions.NotFetchedException("Children history has not been fetched.");
			}
		};
		prototype.setChildrenHistory = function(childrenHistory) {
			this.childrenHistory = childrenHistory;
		};

		prototype.getContainersHistory = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasContainersHistory()) {
				return this.containersHistory;
			} else {
				throw new exceptions.NotFetchedException("Containers history has not been fetched.");
			}
		};
		prototype.setContainersHistory = function(containersHistory) {
			this.containersHistory = containersHistory;
		};

		prototype.getComponentsHistory = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasComponentsHistory()) {
				return this.componentsHistory;
			} else {
				throw new exceptions.NotFetchedException("Components history has not been fetched.");
			}
		};
		prototype.setComponentsHistory = function(componentsHistory) {
			this.componentsHistory = componentsHistory;
		};

		prototype.getContentCopiesHistory = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasContentCopiesHistory()) {
				return this.contentCopiesHistory;
			} else {
				throw new exceptions.NotFetchedException("Content copies history has not been fetched.");
			}
		};
		prototype.setContentCopiesHistory = function(contentCopiesHistory) {
			this.contentCopiesHistory = contentCopiesHistory;
		};

		prototype.getUnknownHistory = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasUnknownHistory()) {
				return this.unknownHistory;
			} else {
				throw new exceptions.NotFetchedException("Unknown history has not been fetched.");
			}
		};
		prototype.setUnknownHistory = function(unknownHistory) {
			this.unknownHistory = unknownHistory;
		};

		prototype.getModificationDate = function() {
			return this.modificationDate;
		};
		prototype.setModificationDate = function(modificationDate) {
			this.modificationDate = modificationDate;
		};
		prototype.getModifier = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasModifier()) {
				return this.modifier;
			} else {
				throw new exceptions.NotFetchedException("Modifier has not been fetched.");
			}
		};
		prototype.setModifier = function(modifier) {
			this.modifier = modifier;
		};
		prototype.getRegistrationDate = function() {
			return this.registrationDate;
		};
		prototype.setRegistrationDate = function(registrationDate) {
			this.registrationDate = registrationDate;
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
		prototype.getExperiment = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasExperiment()) {
				return this.experiment;
			} else {
				throw new exceptions.NotFetchedException("Experiment has not been fetched.");
			}
		};
		prototype.setExperiment = function(experiment) {
			this.experiment = experiment;
		};
		prototype.getSample = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasSample()) {
				return this.sample;
			} else {
				throw new exceptions.NotFetchedException("Sample has not been fetched.");
			}
		};
		prototype.setSample = function(sample) {
			this.sample = sample;
		};
		prototype.getProperty = function(propertyName) {
			var properties = this.getProperties();
			return properties ? properties[propertyName] : null;
		};
		prototype.setProperty = function(propertyName, propertyValue) {
			if (this.properties == null) {
				this.properties = {};
			}
			this.properties[propertyName] = propertyValue;
		};
		prototype.getProperties = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasProperties()) {
				return this.properties;
			} else {
				throw new exceptions.NotFetchedException("Properties has not been fetched.");
			}
		};
		prototype.setProperties = function(properties) {
			this.properties = properties;
		};
		prototype.getMaterialProperty = function(propertyName) {
			var properties = this.getMaterialProperties();
			return properties ? properties[propertyName] : null;
		};
		prototype.setMaterialProperty = function(propertyName, propertyValue) {
			if (this.materialProperties == null) {
				this.materialProperties = {};
			}
			this.materialProperties[propertyName] = propertyValue;
		};
		prototype.getMaterialProperties = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasMaterialProperties()) {
				return this.materialProperties;
			} else {
				throw new exceptions.NotFetchedException("Material properties has not been fetched.");
			}
		};
		prototype.setMaterialProperties = function(materialProperties) {
			this.materialProperties = materialProperties;
		};
		prototype.getSampleProperties = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasSampleProperties()) {
				return this.sampleProperties;
			} else {
				throw new exceptions.NotFetchedException("Sample properties have not been fetched.");
			}
		};
		prototype.setSampleProperties = function(sampleProperties) {
			this.sampleProperties = sampleProperties;
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
		prototype.getDataProducer = function() {
			return this.dataProducer;
		};
		prototype.setDataProducer = function(dataProducer) {
			this.dataProducer = dataProducer;
		};
		prototype.getDataProductionDate = function() {
			return this.dataProductionDate;
		};
		prototype.setDataProductionDate = function(dataProductionDate) {
			this.dataProductionDate = dataProductionDate;
		};
		prototype.getMetaData = function() {
            return this.metaData;
        };
        prototype.setMetaData = function(metaData) {
            this.metaData = metaData;
        };
	}, {
		fetchOptions : "DataSetFetchOptions",
		permId : "DataSetPermId",
		accessDate : "Date",
		parents : {
			name : "List",
			arguments : [ "DataSet" ]
		},
		children : {
			name : "List",
			arguments : [ "DataSet" ]
		},
		containers : {
			name : "List",
			arguments : [ "DataSet" ]
		},
		components : {
			name : "List",
			arguments : [ "DataSet" ]
		},
		physicalData : "PhysicalData",
		linkedData : "LinkedData",
		tags : {
			name : "Set",
			arguments : [ "Tag" ]
		},
		type : "DataSetType",
		kind: "DataSetKind",
		dataStore : "DataStore",
		history : {
			name : "List",
			arguments : [ "HistoryEntry" ]
		},
		propertiesHistory : {
			name : "List",
			arguments : [ "HistoryEntry" ]
		},
		experimentHistory : {
			name : "List",
			arguments : [ "HistoryEntry" ]
		},
		sampleHistory : {
			name : "List",
			arguments : [ "HistoryEntry" ]
		},
		parentsHistory : {
			name : "List",
			arguments : [ "HistoryEntry" ]
		},
		childrenHistory : {
			name : "List",
			arguments : [ "HistoryEntry" ]
		},
		containersHistory : {
			name : "List",
			arguments : [ "HistoryEntry" ]
		},
		componentsHistory : {
			name : "List",
			arguments : [ "HistoryEntry" ]
		},
		contentCopiesHistory : {
			name : "List",
			arguments : [ "HistoryEntry" ]
		},
		unknownHistory : {
			name : "List",
			arguments : [ "HistoryEntry" ]
		},
		modificationDate : "Date",
		modifier : "Person",
		registrationDate : "Date",
		registrator : "Person",
		experiment : "Experiment",
		sample : "Sample",
		properties : {
			name : "Map",
			arguments : [ "String", "Serializable" ]
		},
		materialProperties : {
			name : "Map",
			arguments : [ "String", "Material" ]
		},
		sampleProperties : {
			name : "Map",
			arguments : [ "String", "Sample[]" ]
		},
		dataProductionDate : "Date",
		metaData: {
            name: "Map",
            arguments: ["String", "String"]
        }
	});
	return DataSet;
})