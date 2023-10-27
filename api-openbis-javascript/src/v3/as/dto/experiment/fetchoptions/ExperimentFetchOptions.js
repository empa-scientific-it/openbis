/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "as/dto/common/fetchoptions/AbstractEntityFetchOptions", 'as/dto/experiment/fetchoptions/ExperimentTypeFetchOptions', 'as/dto/project/fetchoptions/ProjectFetchOptions',
		'as/dto/tag/fetchoptions/TagFetchOptions', 'as/dto/person/fetchoptions/PersonFetchOptions', 'as/dto/attachment/fetchoptions/AttachmentFetchOptions',
		'as/dto/dataset/fetchoptions/DataSetFetchOptions', 'as/dto/sample/fetchoptions/SampleFetchOptions', 'as/dto/history/fetchoptions/HistoryEntryFetchOptions',
		'as/dto/material/fetchoptions/MaterialFetchOptions', 'as/dto/sample/fetchoptions/SampleFetchOptions', 'as/dto/experiment/fetchoptions/ExperimentSortOptions' ], function(require, stjs, AbstractEntityFetchOptions) {
	var ExperimentFetchOptions = function() {
	    AbstractEntityFetchOptions.call(this);
	};
	stjs.extend(ExperimentFetchOptions, AbstractEntityFetchOptions, [ AbstractEntityFetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.fetchoptions.ExperimentFetchOptions';
		constructor.serialVersionUID = 1;
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
		prototype.sort = null;
		prototype.withType = function() {
			if (this.type == null) {
				var ExperimentTypeFetchOptions = require("as/dto/experiment/fetchoptions/ExperimentTypeFetchOptions");
				this.type = new ExperimentTypeFetchOptions();
			}
			return this.type;
		};
		prototype.withTypeUsing = function(fetchOptions) {
			return this.type = fetchOptions;
		};
		prototype.hasType = function() {
			return this.type != null;
		};
		prototype.withProject = function() {
			if (this.project == null) {
				var ProjectFetchOptions = require("as/dto/project/fetchoptions/ProjectFetchOptions");
				this.project = new ProjectFetchOptions();
			}
			return this.project;
		};
		prototype.withProjectUsing = function(fetchOptions) {
			return this.project = fetchOptions;
		};
		prototype.hasProject = function() {
			return this.project != null;
		};
		prototype.withDataSets = function() {
			if (this.dataSets == null) {
				var DataSetFetchOptions = require("as/dto/dataset/fetchoptions/DataSetFetchOptions");
				this.dataSets = new DataSetFetchOptions();
			}
			return this.dataSets;
		};
		prototype.withDataSetsUsing = function(fetchOptions) {
			return this.dataSets = fetchOptions;
		};
		prototype.hasDataSets = function() {
			return this.dataSets != null;
		};
		prototype.withSamples = function() {
			if (this.samples == null) {
				var SampleFetchOptions = require("as/dto/sample/fetchoptions/SampleFetchOptions");
				this.samples = new SampleFetchOptions();
			}
			return this.samples;
		};
		prototype.withSamplesUsing = function(fetchOptions) {
			return this.samples = fetchOptions;
		};
		prototype.hasSamples = function() {
			return this.samples != null;
		};
		prototype.withHistory = function() {
			if (this.history == null) {
				var HistoryEntryFetchOptions = require("as/dto/history/fetchoptions/HistoryEntryFetchOptions");
				this.history = new HistoryEntryFetchOptions();
			}
			return this.history;
		};
		prototype.withHistoryUsing = function(fetchOptions) {
			return this.history = fetchOptions;
		};
		prototype.hasHistory = function() {
			return this.history != null;
		};

		prototype.withPropertiesHistory = function() {
			if (this.propertiesHistory == null) {
				var HistoryEntryFetchOptions = require("as/dto/history/fetchoptions/HistoryEntryFetchOptions");
				this.propertiesHistory = new HistoryEntryFetchOptions();
			}
			return this.propertiesHistory;
		};
		prototype.withPropertiesHistoryUsing = function(fetchOptions) {
			return this.propertiesHistory = fetchOptions;
		};
		prototype.hasPropertiesHistory = function() {
			return this.propertiesHistory != null;
		};

		prototype.withProjectHistory = function() {
			if (this.projectHistory == null) {
				var HistoryEntryFetchOptions = require("as/dto/history/fetchoptions/HistoryEntryFetchOptions");
				this.projectHistory = new HistoryEntryFetchOptions();
			}
			return this.projectHistory;
		};
		prototype.withProjectHistoryUsing = function(fetchOptions) {
			return this.projectHistory = fetchOptions;
		};
		prototype.hasProjectHistory = function() {
			return this.projectHistory != null;
		};

		prototype.withSamplesHistory = function() {
			if (this.samplesHistory == null) {
				var HistoryEntryFetchOptions = require("as/dto/history/fetchoptions/HistoryEntryFetchOptions");
				this.samplesHistory = new HistoryEntryFetchOptions();
			}
			return this.samplesHistory;
		};
		prototype.withSamplesHistoryUsing = function(fetchOptions) {
			return this.samplesHistory = fetchOptions;
		};
		prototype.hasSamplesHistory = function() {
			return this.samplesHistory != null;
		};

		prototype.withDataSetsHistory = function() {
			if (this.dataSetsHistory == null) {
				var HistoryEntryFetchOptions = require("as/dto/history/fetchoptions/HistoryEntryFetchOptions");
				this.dataSetsHistory = new HistoryEntryFetchOptions();
			}
			return this.dataSetsHistory;
		};
		prototype.withDataSetsHistoryUsing = function(fetchOptions) {
			return this.dataSetsHistory = fetchOptions;
		};
		prototype.hasDataSetsHistory = function() {
			return this.dataSetsHistory != null;
		};

		prototype.withUnknownHistory = function() {
			if (this.unknownHistory == null) {
				var HistoryEntryFetchOptions = require("as/dto/history/fetchoptions/HistoryEntryFetchOptions");
				this.unknownHistory = new HistoryEntryFetchOptions();
			}
			return this.unknownHistory;
		};
		prototype.withUnknownHistoryUsing = function(fetchOptions) {
			return this.unknownHistory = fetchOptions;
		};
		prototype.hasUnknownHistory = function() {
			return this.unknownHistory != null;
		};

		prototype.withMaterialProperties = function() {
			if (this.materialProperties == null) {
				var MaterialFetchOptions = require("as/dto/material/fetchoptions/MaterialFetchOptions");
				this.materialProperties = new MaterialFetchOptions();
			}
			return this.materialProperties;
		};
		prototype.withMaterialPropertiesUsing = function(fetchOptions) {
			return this.materialProperties = fetchOptions;
		};
		prototype.hasMaterialProperties = function() {
			return this.materialProperties != null;
		};
		prototype.withSampleProperties = function() {
			if (this.sampleProperties == null) {
				var SampleFetchOptions = require("as/dto/sample/fetchoptions/SampleFetchOptions");
				this.sampleProperties = new SampleFetchOptions();
			}
			return this.sampleProperties;
		};
		prototype.withSamplePropertiesUsing = function(fetchOptions) {
			return this.sampleProperties = fetchOptions;
		};
		prototype.hasSampleProperties = function() {
			return this.sampleProperties != null;
		};
		prototype.withTags = function() {
			if (this.tags == null) {
				var TagFetchOptions = require("as/dto/tag/fetchoptions/TagFetchOptions");
				this.tags = new TagFetchOptions();
			}
			return this.tags;
		};
		prototype.withTagsUsing = function(fetchOptions) {
			return this.tags = fetchOptions;
		};
		prototype.hasTags = function() {
			return this.tags != null;
		};
		prototype.withRegistrator = function() {
			if (this.registrator == null) {
				var PersonFetchOptions = require("as/dto/person/fetchoptions/PersonFetchOptions");
				this.registrator = new PersonFetchOptions();
			}
			return this.registrator;
		};
		prototype.withRegistratorUsing = function(fetchOptions) {
			return this.registrator = fetchOptions;
		};
		prototype.hasRegistrator = function() {
			return this.registrator != null;
		};
		prototype.withModifier = function() {
			if (this.modifier == null) {
				var PersonFetchOptions = require("as/dto/person/fetchoptions/PersonFetchOptions");
				this.modifier = new PersonFetchOptions();
			}
			return this.modifier;
		};
		prototype.withModifierUsing = function(fetchOptions) {
			return this.modifier = fetchOptions;
		};
		prototype.hasModifier = function() {
			return this.modifier != null;
		};
		prototype.withAttachments = function() {
			if (this.attachments == null) {
				var AttachmentFetchOptions = require("as/dto/attachment/fetchoptions/AttachmentFetchOptions");
				this.attachments = new AttachmentFetchOptions();
			}
			return this.attachments;
		};
		prototype.withAttachmentsUsing = function(fetchOptions) {
			return this.attachments = fetchOptions;
		};
		prototype.hasAttachments = function() {
			return this.attachments != null;
		};
		prototype.sortBy = function() {
			if (this.sort == null) {
				var ExperimentSortOptions = require("as/dto/experiment/fetchoptions/ExperimentSortOptions");
				this.sort = new ExperimentSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		type : "ExperimentTypeFetchOptions",
		project : "ProjectFetchOptions",
		dataSets : "DataSetFetchOptions",
		samples : "SampleFetchOptions",
		history : "HistoryEntryFetchOptions",
		propertiesHistory : "HistoryEntryFetchOptions",
		projectHistory : "HistoryEntryFetchOptions",
		samplesHistory : "HistoryEntryFetchOptions",
		dataSetsHistory : "HistoryEntryFetchOptions",
		unknownHistory : "HistoryEntryFetchOptions",
		materialProperties : "MaterialFetchOptions",
		sampleProperties : "SampleFetchOptions",
		tags : "TagFetchOptions",
		registrator : "PersonFetchOptions",
		modifier : "PersonFetchOptions",
		attachments : "AttachmentFetchOptions",
		sort : "ExperimentSortOptions"
	});
	return ExperimentFetchOptions;
})