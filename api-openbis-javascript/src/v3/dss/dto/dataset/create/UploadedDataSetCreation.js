define([ "stjs" ], function(stjs) {
	var UploadedDataSetCreation = function() {
		this.properties = {};
	};
	stjs.extend(UploadedDataSetCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dss.dto.dataset.create.UploadedDataSetCreation';
		constructor.serialVersionUID = 1;
		prototype.typeId = null;
		prototype.experimentId = null;
		prototype.sampleId = null;
		prototype.properties = null;
		prototype.parentIds = null;
		prototype.uploadId = null;

		prototype.getTypeId = function() {
			return this.typeId;
		};
		prototype.setTypeId = function(typeId) {
			this.typeId = typeId;
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
		prototype.getParentIds = function() {
			return this.parentIds;
		};
		prototype.setParentIds = function(parentIds) {
			this.parentIds = parentIds;
		};
		prototype.getUploadId = function() {
			return this.uploadId;
		};
		prototype.setUploadId = function(uploadId) {
			this.uploadId = uploadId;
		};
	}, {
		typeId : "IEntityTypeId",
		experimentId : "IExperimentId",
		sampleId : "ISampleId",
		properties : {
			name : "Map",
			arguments : [ "String", "Serializable" ]
		},
		parentIds : {
			name : "List",
			arguments : [ "IDataSetId" ]
		}
	});
	return UploadedDataSetCreation;
})