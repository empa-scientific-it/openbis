/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "util/Exceptions", "as/dto/common/entity/AbstractEntity" ], function(stjs, exceptions, AbstractEntity) {
	var Experiment = function() {
	    AbstractEntity.call(this);
	};
	stjs.extend(Experiment, AbstractEntity, [AbstractEntity], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.Experiment';
		constructor.serialVersionUID = 1;
		prototype.permId = null;
		prototype.identifier = null;
		prototype.code = null;
		prototype.frozen = null;
		prototype.frozenForDataSets = null;
		prototype.frozenForSamples = null;
		prototype.registrationDate = null;
		prototype.modificationDate = null;
		prototype.type = null;
		prototype.project = null;
		prototype.dataSets = null;
		prototype.samples = null;
		prototype.history = null;
        prototype.propertiesHistory = null;
        prototype.projectHistory = null;
        prototype.samplesHistory = null;
        prototype.dataSetsHistory = null;
        prototype.unknownHistory = null;
		prototype.materialProperties = null;
		prototype.sampleProperties = null;
		prototype.tags = null;
		prototype.registrator = null;
		prototype.modifier = null;
		prototype.attachments = null;
		prototype.metaData = null;
		prototype.getFetchOptions = function() {
		    return AbstractEntity.prototype.getFetchOptions.call(this);
		};
		prototype.setFetchOptions = function(fetchOptions) {
		    AbstractEntity.prototype.setFetchOptions.call(this, fetchOptions);
		};
		prototype.getPermId = function() {
			return this.permId;
		};
		prototype.setPermId = function(permId) {
			this.permId = permId;
		};
		prototype.getIdentifier = function() {
			return this.identifier;
		};
		prototype.setIdentifier = function(identifier) {
			this.identifier = identifier;
		};
		prototype.getCode = function() {
			return this.code;
		};
		prototype.setCode = function(code) {
			this.code = code;
		};
		prototype.isFrozen = function() {
			return this.frozen;
		}
		prototype.setFrozen = function(frozen) {
			this.frozen = frozen;
		}
		prototype.isFrozenForDataSets = function() {
			return this.frozenForDataSets;
		}
		prototype.setFrozenForDataSets = function(frozenForDataSets) {
			this.frozenForDataSets = frozenForDataSets;
		}
		prototype.isFrozenForSamples = function() {
			return this.frozenForSamples;
		}
		prototype.setFrozenForSamples = function(frozenForSamples) {
			this.frozenForSamples = frozenForSamples;
		}
		prototype.getRegistrationDate = function() {
			return this.registrationDate;
		};
		prototype.setRegistrationDate = function(registrationDate) {
			this.registrationDate = registrationDate;
		};
		prototype.getModificationDate = function() {
			return this.modificationDate;
		};
		prototype.setModificationDate = function(modificationDate) {
			this.modificationDate = modificationDate;
		};
		prototype.getType = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasType()) {
				return this.type;
			} else {
				throw new exceptions.NotFetchedException("Experiment type has not been fetched.");
			}
		};
		prototype.setType = function(type) {
			this.type = type;
		};
		prototype.getProject = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasProject()) {
				return this.project;
			} else {
				throw new exceptions.NotFetchedException("Project has not been fetched.");
			}
		};
		prototype.setProject = function(project) {
			this.project = project;
		};
		prototype.getDataSets = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasDataSets()) {
				return this.dataSets;
			} else {
				throw new exceptions.NotFetchedException("Data sets have not been fetched.");
			}
		};
		prototype.setDataSets = function(dataSets) {
			this.dataSets = dataSets;
		};
		prototype.getSamples = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasSamples()) {
				return this.samples;
			} else {
				throw new exceptions.NotFetchedException("Samples have not been fetched.");
			}
		};
		prototype.setSamples = function(samples) {
			this.samples = samples;
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

		prototype.getProjectHistory = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasProjectHistory()) {
				return this.projectHistory;
			} else {
				throw new exceptions.NotFetchedException("Project history has not been fetched.");
			}
		};
		prototype.setProjectHistory = function(projectHistory) {
			this.projectHistory = projectHistory;
		};

		prototype.getSamplesHistory = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasSamplesHistory()) {
				return this.samplesHistory;
			} else {
				throw new exceptions.NotFetchedException("Samples history has not been fetched.");
			}
		};
		prototype.setSamplesHistory = function(samplesHistory) {
			this.samplesHistory = samplesHistory;
		};

		prototype.getDataSetsHistory = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasDataSetsHistory()) {
				return this.dataSetsHistory;
			} else {
				throw new exceptions.NotFetchedException("Data sets history has not been fetched.");
			}
		};
		prototype.setDataSetsHistory = function(dataSetsHistory) {
			this.dataSetsHistory = dataSetsHistory;
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
				throw new exceptions.NotFetchedException("Material properties have not been fetched.");
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
		prototype.getAttachments = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasAttachments()) {
				return this.attachments;
			} else {
				throw new exceptions.NotFetchedException("Attachments has not been fetched.");
			}
		};
		prototype.setAttachments = function(attachments) {
			this.attachments = attachments;
		};
		prototype.getMetaData = function() {
            return this.metaData;
        };
        prototype.setMetaData = function(metaData) {
            this.metaData = metaData;
        };
		prototype.toString = function() {
			return "Experiment " + this.permId;
		};
	}, {
		fetchOptions : "ExperimentFetchOptions",
		permId : "ExperimentPermId",
		identifier : "ExperimentIdentifier",
		registrationDate : "Date",
		modificationDate : "Date",
		type : "ExperimentType",
		project : "Project",
		dataSets : {
			name : "List",
			arguments : [ "DataSet" ]
		},
		samples : {
			name : "List",
			arguments : [ "Sample" ]
		},
		history : {
			name : "List",
			arguments : [ "HistoryEntry" ]
		},
		propertiesHistory : {
			name : "List",
			arguments : [ "HistoryEntry" ]
		},
		projectHistory : {
			name : "List",
			arguments : [ "HistoryEntry" ]
		},
		samplesHistory : {
			name : "List",
			arguments : [ "HistoryEntry" ]
		},
		dataSetsHistory : {
			name : "List",
			arguments : [ "HistoryEntry" ]
		},
		unknownHistory : {
			name : "List",
			arguments : [ "HistoryEntry" ]
		},
		materialProperties : {
			name : "Map",
			arguments : [ "String", "Material" ]
		},
		sampleProperties : {
			name : "Map",
			arguments : [ "String", "Sample[]" ]
		},
		tags : {
			name : "Set",
			arguments : [ "Tag" ]
		},
		registrator : "Person",
		modifier : "Person",
		attachments : {
			name : "List",
			arguments : [ "Attachment" ]
		},
        metaData: {
             name: "Map",
             arguments: ["String", "String"]
         }
	});
	return Experiment;
})