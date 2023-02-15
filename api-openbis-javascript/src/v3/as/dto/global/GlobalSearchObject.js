define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var GlobalSearchObject = function() {
	};
	stjs.extend(GlobalSearchObject, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.global.GlobalSearchObject';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.objectKind = null;
		prototype.objectPermId = null;
		prototype.objectIdentifier = null;
		prototype.match = null;
		prototype.score = null;
		prototype.experiment = null;
		prototype.sample = null;
		prototype.dataSet = null;
		prototype.material = null;

		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};
		prototype.getObjectKind = function() {
			return this.objectKind;
		};
		prototype.setObjectKind = function(objectKind) {
			this.objectKind = objectKind;
		};
		prototype.getObjectPermId = function() {
			return this.objectPermId;
		};
		prototype.setObjectPermId = function(objectPermId) {
			this.objectPermId = objectPermId;
		};
		prototype.getObjectIdentifier = function() {
			return this.objectIdentifier;
		};
		prototype.setObjectIdentifier = function(objectIdentifier) {
			this.objectIdentifier = objectIdentifier;
		};
		prototype.getMatch = function() {
			return this.match;
		};
		prototype.setMatch = function(match) {
			this.match = match;
		};
		prototype.getScore = function() {
			return this.score;
		};
		prototype.setScore = function(score) {
			this.score = score;
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
		prototype.getDataSet = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasDataSet()) {
				return this.dataSet;
			} else {
				throw new exceptions.NotFetchedException("Data set has not been fetched.");
			}
		};
		prototype.setDataSet = function(dataSet) {
			this.dataSet = dataSet;
		};
		prototype.getMaterial = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasMaterial()) {
				return this.material;
			} else {
				throw new exceptions.NotFetchedException("Material has not been fetched.");
			}
		};
		prototype.setMaterial = function(material) {
			this.material = material;
		};

	}, {
		fetchOptions : "GlobalSearchObjectFetchOptions",
		objectKind : "GlobalSearchObjectKind",
		objectPermId : "IObjectId",
		objectIdentifier : "IObjectId",
		experiment : "Experiment",
		sample : "Sample",
		dataSet : "DataSet",
		material : "Material"
	});
	return GlobalSearchObject;
})