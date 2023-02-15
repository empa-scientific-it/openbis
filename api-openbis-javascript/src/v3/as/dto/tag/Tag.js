/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var Tag = function() {
	};
	stjs.extend(Tag, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.tag.Tag';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.permId = null;
		prototype.code = null;
		prototype.description = null;
		prototype.isPrivate = null;
		prototype.experiments = null;
		prototype.samples = null;
		prototype.dataSets = null;
		prototype.materials = null;
		prototype.registrationDate = null;
		prototype.owner = null;
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
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description = description;
		};
		prototype.isPrivate = function() {
			return this.isPrivate;
		};
		prototype.setPrivate = function(isPrivate) {
			this.isPrivate = isPrivate;
		};
		prototype.getExperiments = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasExperiments()) {
				return this.experiments;
			} else {
				throw new exceptions.NotFetchedException("Experiments have not been fetched.");
			}
		};
		prototype.setExperiments = function(experiments) {
			this.experiments = experiments;
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
		prototype.getMaterials = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasMaterials()) {
				return this.materials;
			} else {
				throw new exceptions.NotFetchedException("Materials have not been fetched.");
			}
		};
		prototype.setMaterials = function(materials) {
			this.materials = materials;
		};
		prototype.getRegistrationDate = function() {
			return this.registrationDate;
		};
		prototype.setRegistrationDate = function(registrationDate) {
			this.registrationDate = registrationDate;
		};
		prototype.getOwner = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasOwner()) {
				return this.owner;
			} else {
				throw new exceptions.NotFetchedException("Owner has not been fetched.");
			}
		};
		prototype.setOwner = function(owner) {
			this.owner = owner;
		};
	}, {
		fetchOptions : "TagFetchOptions",
		permId : "TagPermId",
		experiments : {
			name : "List",
			arguments : [ "Experiment" ]
		},
		samples : {
			name : "List",
			arguments : [ "Sample" ]
		},
		dataSets : {
			name : "List",
			arguments : [ "DataSet" ]
		},
		materials : {
			name : "List",
			arguments : [ "Material" ]
		},
		registrationDate : "Date",
		owner : "Person"
	});
	return Tag;
})