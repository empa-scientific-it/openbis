define([ "stjs", "as/dto/common/entity/AbstractEntityCreation" ], function(stjs, AbstractEntityCreation) {
	var DataSetCreation = function() {
	    AbstractEntityCreation.call(this);
		this.properties = {};
	};
	stjs.extend(DataSetCreation, AbstractEntityCreation, [AbstractEntityCreation], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.create.DataSetCreation';
		constructor.serialVersionUID = 1;
		prototype.typeId = null;
		prototype.dataSetKind = null;
		prototype.experimentId = null;
		prototype.sampleId = null;
		prototype.dataStoreId = null;
		prototype.code = null;
		prototype.measured = null;
		prototype.dataProducer = null;
		prototype.dataProductionDate = null;
		prototype.linkedData = null;
		prototype.tagIds = null;
		prototype.containerIds = null;
		prototype.componentIds = null;
		prototype.parentIds = null;
		prototype.childIds = null;
		prototype.creationId = null;
		prototype.autoGeneratedCode = null;
		prototype.metaData = null;

        prototype.getTypeId = function() {
            return this.typeId;
        };
        prototype.setTypeId = function(typeId) {
            this.typeId = typeId;
        };
        prototype.getDataSetKind = function() {
            return this.dataSetKind;
        };
        prototype.setDataSetKind = function(dataSetKind) {
            this.dataSetKind = dataSetKind;
        };
		prototype.getExperimentId = function() {
			return this.experimentId;
		};
		prototype.setExperimentId = function(experimentId) {
			this.experimentId = experimentId;
		};
		prototype.getSampleId = function() {
			return this.sampleId;
		};
		prototype.setSampleId = function(sampleId) {
			this.sampleId = sampleId;
		};
		prototype.getDataStoreId = function() {
			return this.dataStoreId;
		};
		prototype.setDataStoreId = function(dataStoreId) {
			this.dataStoreId = dataStoreId;
		};
		prototype.getCode = function() {
			return this.code;
		};
		prototype.setCode = function(code) {
			this.code = code;
		};
		prototype.isMeasured = function() {
			return this.measured;
		};
		prototype.setMeasured = function(measured) {
			this.measured = measured;
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
		prototype.getLinkedData = function() {
			return this.linkedData;
		};
		prototype.setLinkedData = function(linkedData) {
			this.linkedData = linkedData;
		};
		prototype.getTagIds = function() {
			return this.tagIds;
		};
		prototype.setTagIds = function(tagIds) {
			this.tagIds = tagIds;
		};
		prototype.getContainerIds = function() {
			return this.containerIds;
		};
		prototype.setContainerIds = function(containerIds) {
			this.containerIds = containerIds;
		};
		prototype.getComponentIds = function() {
			return this.componentIds;
		};
		prototype.setComponentIds = function(componentIds) {
			this.componentIds = componentIds;
		};
		prototype.getChildIds = function() {
			return this.childIds;
		};
		prototype.setChildIds = function(childIds) {
			this.childIds = childIds;
		};
		prototype.getParentIds = function() {
			return this.parentIds;
		};
		prototype.setParentIds = function(parentIds) {
			this.parentIds = parentIds;
		};
		prototype.getCreationId = function() {
			return this.creationId;
		};
		prototype.setCreationId = function(creationId) {
			this.creationId = creationId;
		};
		prototype.isAutoGeneratedCode = function() {
			return this.autoGeneratedCode;
		}
		prototype.setAutoGeneratedCode = function(autoGeneratedCode) {
			this.autoGeneratedCode = autoGeneratedCode;
		};
		prototype.getMetaData = function() {
            return this.metaData;
        };
        prototype.setMetaData = function(metaData) {
            this.metaData = metaData;
        };
	}, {
		typeId : "IEntityTypeId",
		dataSetKind : "DataSetKind",
		experimentId : "IExperimentId", 
		sampleId : "ISampleId",
		dataStoreId : "IDataStoreId",
		dataProductionDate : "Date",
		linkedData : "LinkedDataCreation",
		tagIds : {
			name : "List",
			arguments : [ "Object" ]
		},
		containerIds : {
			name : "List",
			arguments : [ "Object" ]
		},
		componentIds : {
			name : "List",
			arguments : [ "Object" ]
		},
		parentIds : {
			name : "List",
			arguments : [ "Object" ]
		},
		childIds : {
			name : "List",
			arguments : [ "Object" ]
		},
		creationId : "CreationId",
        metaData: {
            name: "Map",
            arguments: ["String", "String"]
        }
	});
	return DataSetCreation;
})