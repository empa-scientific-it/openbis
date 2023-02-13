/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var Project = function() {
	};
	stjs.extend(Project, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.project.Project';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.permId = null;
		prototype.identifier = null;
		prototype.code = null;
		prototype.frozen = null;
		prototype.frozenForExperiments = null;
		prototype.frozenForSamples = null;
		prototype.description = null;
		prototype.registrationDate = null;
		prototype.modificationDate = null;
		prototype.experiments = null;
		prototype.samples = null;
		prototype.history = null;
        prototype.spaceHistory = null;
        prototype.experimentsHistory = null;
        prototype.samplesHistory = null;
        prototype.unknownHistory = null;
		prototype.space = null;
		prototype.registrator = null;
		prototype.modifier = null;
		prototype.leader = null;
		prototype.attachments = null;
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
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description = description;
		};
		prototype.isFrozen = function() {
			return this.frozen;
		}
		prototype.setFrozen = function(frozen) {
			this.frozen = frozen;
		}
		prototype.isFrozenForExperiments = function() {
			return this.frozenForExperiments;
		}
		prototype.setFrozenForExperiments = function(frozenForExperiments) {
			this.frozenForExperiments = frozenForExperiments;
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
			if (this.getFetchOptions().hasSamples()) {
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
				throw new exceptions.NotFetchedException("History have not been fetched.");
			}
		};
		prototype.setHistory = function(history) {
			this.history = history;
		};

		prototype.getSpaceHistory = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasSpaceHistory()) {
				return this.spaceHistory;
			} else {
				throw new exceptions.NotFetchedException("Space history have not been fetched.");
			}
		};
		prototype.setSpaceHistory = function(spaceHistory) {
			this.spaceHistory = spaceHistory;
		};

		prototype.getExperimentsHistory = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasExperimentsHistory()) {
				return this.experimentsHistory;
			} else {
				throw new exceptions.NotFetchedException("Experiments history have not been fetched.");
			}
		};
		prototype.setExperimentsHistory = function(experimentsHistory) {
			this.experimentsHistory = experimentsHistory;
		};

		prototype.getSamplesHistory = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasSamplesHistory()) {
				return this.samplesHistory;
			} else {
				throw new exceptions.NotFetchedException("Samples history have not been fetched.");
			}
		};
		prototype.setSamplesHistory = function(samplesHistory) {
			this.samplesHistory = samplesHistory;
		};

		prototype.getUnknownHistory = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasUnknownHistory()) {
				return this.unknownHistory;
			} else {
				throw new exceptions.NotFetchedException("Unknown history have not been fetched.");
			}
		};
		prototype.setUnknownHistory = function(unknownHistory) {
			this.unknownHistory = unknownHistory;
		};

		prototype.getSpace = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasSpace()) {
				return this.space;
			} else {
				throw new exceptions.NotFetchedException("Space has not been fetched.");
			}
		};
		prototype.setSpace = function(space) {
			this.space = space;
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
		prototype.getLeader = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasLeader()) {
				return this.leader;
			} else {
				throw new exceptions.NotFetchedException("Leader has not been fetched.");
			}
		};
		prototype.setLeader = function(leader) {
			this.leader = leader;
		};
		prototype.getAttachments = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasAttachments()) {
				return this.attachments;
			} else {
				throw new exceptions.NotFetchedException("Attachments have not been fetched.");
			}
		};
		prototype.setAttachments = function(attachments) {
			this.attachments = attachments;
		};
	}, {
		fetchOptions : "ProjectFetchOptions",
		permId : "ProjectPermId",
		identifier : "ProjectIdentifier",
		registrationDate : "Date",
		modificationDate : "Date",
		samples : {
			name : "List",
			arguments : [ "Sample" ]
		},
		history : {
			name : "List",
			arguments : [ "HistoryEntry" ]
		},
		spaceHistory : {
			name : "List",
			arguments : [ "HistoryEntry" ]
		},
		experimentsHistory : {
			name : "List",
			arguments : [ "HistoryEntry" ]
		},
		samplesHistory : {
			name : "List",
			arguments : [ "HistoryEntry" ]
		},
		unknownHistory : {
			name : "List",
			arguments : [ "HistoryEntry" ]
		},
		experiments : {
			name : "List",
			arguments : [ "Experiment" ]
		},
		space : "Space",
		registrator : "Person",
		modifier : "Person",
		leader : "Person",
		attachments : {
			name : "List",
			arguments : [ "Attachment" ]
		}
	});
	return Project;
})