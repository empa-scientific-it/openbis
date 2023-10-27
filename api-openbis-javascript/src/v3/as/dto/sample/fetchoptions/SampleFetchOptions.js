/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "as/dto/common/fetchoptions/AbstractEntityFetchOptions", 'as/dto/sample/fetchoptions/SampleTypeFetchOptions', 'as/dto/space/fetchoptions/SpaceFetchOptions',
		'as/dto/project/fetchoptions/ProjectFetchOptions', 'as/dto/experiment/fetchoptions/ExperimentFetchOptions', 'as/dto/tag/fetchoptions/TagFetchOptions',
		'as/dto/person/fetchoptions/PersonFetchOptions', 'as/dto/attachment/fetchoptions/AttachmentFetchOptions', 'as/dto/material/fetchoptions/MaterialFetchOptions', 'as/dto/sample/fetchoptions/SampleFetchOptions',
		'as/dto/dataset/fetchoptions/DataSetFetchOptions', 'as/dto/history/fetchoptions/HistoryEntryFetchOptions', 'as/dto/sample/fetchoptions/SampleSortOptions' ], function(require, stjs, AbstractEntityFetchOptions) {
	var SampleFetchOptions = function() {
	    AbstractEntityFetchOptions.call(this);
	};
	stjs.extend(SampleFetchOptions, AbstractEntityFetchOptions, [ AbstractEntityFetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.fetchoptions.SampleFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.type = null;
		prototype.project = null;
		prototype.space = null;
		prototype.experiment = null;
		prototype.materialProperties = null;
		prototype.sampleProperties = null;
		prototype.parents = null;
		prototype.children = null;
		prototype.container = null;
		prototype.components = null;
		prototype.dataSets = null;
		prototype.history = null;
		prototype.propertiesHistory = null;
		prototype.spaceHistory = null;
		prototype.projectHistory = null;
		prototype.experimentHistory = null;
		prototype.parentsHistory = null;
		prototype.childrenHistory = null;
		prototype.containerHistory = null;
		prototype.componentsHistory = null;
		prototype.dataSetsHistory = null;
		prototype.unknownHistory = null;
		prototype.tags = null;
		prototype.registrator = null;
		prototype.modifier = null;
		prototype.attachments = null;
		prototype.sort = null;
		prototype.withType = function() {
			if (this.type == null) {
				var SampleTypeFetchOptions = require("as/dto/sample/fetchoptions/SampleTypeFetchOptions");
				this.type = new SampleTypeFetchOptions();
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
		prototype.withSpace = function() {
			if (this.space == null) {
				var SpaceFetchOptions = require("as/dto/space/fetchoptions/SpaceFetchOptions");
				this.space = new SpaceFetchOptions();
			}
			return this.space;
		};
		prototype.withSpaceUsing = function(fetchOptions) {
			return this.space = fetchOptions;
		};
		prototype.hasSpace = function() {
			return this.space != null;
		};
		prototype.withExperiment = function() {
			if (this.experiment == null) {
				var ExperimentFetchOptions = require("as/dto/experiment/fetchoptions/ExperimentFetchOptions");
				this.experiment = new ExperimentFetchOptions();
			}
			return this.experiment;
		};
		prototype.withExperimentUsing = function(fetchOptions) {
			return this.experiment = fetchOptions;
		};
		prototype.hasExperiment = function() {
			return this.experiment != null;
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
		prototype.withParents = function() {
			if (this.parents == null) {
				this.parents = new SampleFetchOptions();
			}
			return this.parents;
		};
		prototype.withParentsUsing = function(fetchOptions) {
			return this.parents = fetchOptions;
		};
		prototype.hasParents = function() {
			return this.parents != null;
		};
		prototype.withChildren = function() {
			if (this.children == null) {
				this.children = new SampleFetchOptions();
			}
			return this.children;
		};
		prototype.withChildrenUsing = function(fetchOptions) {
			return this.children = fetchOptions;
		};
		prototype.hasChildren = function() {
			return this.children != null;
		};
		prototype.withContainer = function() {
			if (this.container == null) {
				this.container = new SampleFetchOptions();
			}
			return this.container;
		};
		prototype.withContainerUsing = function(fetchOptions) {
			return this.container = fetchOptions;
		};
		prototype.hasContainer = function() {
			return this.container != null;
		};
		prototype.withComponents = function() {
			if (this.components == null) {
				this.components = new SampleFetchOptions();
			}
			return this.components;
		};
		prototype.withComponentsUsing = function(fetchOptions) {
			return this.components = fetchOptions;
		};
		prototype.hasComponents = function() {
			return this.components != null;
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

		prototype.withSpaceHistory = function() {
			if (this.spaceHistory == null) {
				var HistoryEntryFetchOptions = require("as/dto/history/fetchoptions/HistoryEntryFetchOptions");
				this.spaceHistory = new HistoryEntryFetchOptions();
			}
			return this.spaceHistory;
		};
		prototype.withSpaceHistoryUsing = function(fetchOptions) {
			return this.spaceHistory = fetchOptions;
		};
		prototype.hasSpaceHistory = function() {
			return this.spaceHistory != null;
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

		prototype.withExperimentHistory = function() {
			if (this.experimentHistory == null) {
				var HistoryEntryFetchOptions = require("as/dto/history/fetchoptions/HistoryEntryFetchOptions");
				this.experimentHistory = new HistoryEntryFetchOptions();
			}
			return this.experimentHistory;
		};
		prototype.withExperimentHistoryUsing = function(fetchOptions) {
			return this.experimentHistory = fetchOptions;
		};
		prototype.hasExperimentHistory = function() {
			return this.experimentHistory != null;
		};

		prototype.withParentsHistory = function() {
			if (this.parentsHistory == null) {
				var HistoryEntryFetchOptions = require("as/dto/history/fetchoptions/HistoryEntryFetchOptions");
				this.parentsHistory = new HistoryEntryFetchOptions();
			}
			return this.parentsHistory;
		};
		prototype.withParentsHistoryUsing = function(fetchOptions) {
			return this.parentsHistory = fetchOptions;
		};
		prototype.hasParentsHistory = function() {
			return this.parentsHistory != null;
		};

		prototype.withChildrenHistory = function() {
			if (this.childrenHistory == null) {
				var HistoryEntryFetchOptions = require("as/dto/history/fetchoptions/HistoryEntryFetchOptions");
				this.childrenHistory = new HistoryEntryFetchOptions();
			}
			return this.childrenHistory;
		};
		prototype.withChildrenHistoryUsing = function(fetchOptions) {
			return this.childrenHistory = fetchOptions;
		};
		prototype.hasChildrenHistory = function() {
			return this.childrenHistory != null;
		};

		prototype.withContainerHistory = function() {
			if (this.containerHistory == null) {
				var HistoryEntryFetchOptions = require("as/dto/history/fetchoptions/HistoryEntryFetchOptions");
				this.containerHistory = new HistoryEntryFetchOptions();
			}
			return this.containerHistory;
		};
		prototype.withContainerHistoryUsing = function(fetchOptions) {
			return this.containerHistory = fetchOptions;
		};
		prototype.hasContainerHistory = function() {
			return this.containerHistory != null;
		};

		prototype.withComponentsHistory = function() {
			if (this.componentsHistory == null) {
				var HistoryEntryFetchOptions = require("as/dto/history/fetchoptions/HistoryEntryFetchOptions");
				this.componentsHistory = new HistoryEntryFetchOptions();
			}
			return this.componentsHistory;
		};
		prototype.withComponentsHistoryUsing = function(fetchOptions) {
			return this.componentsHistory = fetchOptions;
		};
		prototype.hasComponentsHistory = function() {
			return this.componentsHistory != null;
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
				var SampleSortOptions = require("as/dto/sample/fetchoptions/SampleSortOptions");
				this.sort = new SampleSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		type : "SampleTypeFetchOptions",
		project : "ProjectFetchOptions",
		space : "SpaceFetchOptions",
		experiment : "ExperimentFetchOptions",
		materialProperties : "MaterialFetchOptions",
		sampleProperties : "SampleFetchOptions",
		parents : "SampleFetchOptions",
		children : "SampleFetchOptions",
		container : "SampleFetchOptions",
		components : "SampleFetchOptions",
		dataSets : "DataSetFetchOptions",
		history : "HistoryEntryFetchOptions",
		propertiesHistory : "HistoryEntryFetchOptions",
		spaceHistory : "HistoryEntryFetchOptions",
		projectHistory : "HistoryEntryFetchOptions",
		experimentHistory : "HistoryEntryFetchOptions",
		parentsHistory : "HistoryEntryFetchOptions",
		childrenHistory : "HistoryEntryFetchOptions",
		containerHistory : "HistoryEntryFetchOptions",
		componentsHistory : "HistoryEntryFetchOptions",
		dataSetsHistory : "HistoryEntryFetchOptions",
		unknownHistory : "HistoryEntryFetchOptions",
		tags : "TagFetchOptions",
		registrator : "PersonFetchOptions",
		modifier : "PersonFetchOptions",
		attachments : "AttachmentFetchOptions",
		sort : "SampleSortOptions"
	});
	return SampleFetchOptions;
})