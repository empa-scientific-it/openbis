/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/person/fetchoptions/PersonFetchOptions", "as/dto/tag/fetchoptions/TagSortOptions",
		"as/dto/experiment/fetchoptions/ExperimentFetchOptions", "as/dto/sample/fetchoptions/SampleFetchOptions", "as/dto/dataset/fetchoptions/DataSetFetchOptions",
		"as/dto/material/fetchoptions/MaterialFetchOptions" ], function(require, stjs, FetchOptions) {
	var TagFetchOptions = function() {
	};
	stjs.extend(TagFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.tag.fetchoptions.TagFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.experiments = null;
		prototype.samples = null;
		prototype.dataSets = null;
		prototype.materials = null;
		prototype.owner = null;
		prototype.sort = null;

		prototype.withExperiments = function() {
			if (this.experiments == null) {
				var ExperimentFetchOptions = require("as/dto/experiment/fetchoptions/ExperimentFetchOptions");
				this.experiments = new ExperimentFetchOptions();
			}
			return this.experiments;
		};
		prototype.withExperimentsUsing = function(fetchOptions) {
			return this.experiments = fetchOptions;
		};
		prototype.hasExperiments = function() {
			return this.experiments != null;
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

		prototype.withMaterials = function() {
			if (this.materials == null) {
				var MaterialFetchOptions = require("as/dto/material/fetchoptions/MaterialFetchOptions");
				this.materials = new MaterialFetchOptions();
			}
			return this.materials;
		};
		prototype.withMaterialsUsing = function(fetchOptions) {
			return this.materials = fetchOptions;
		};
		prototype.hasMaterials = function() {
			return this.materials != null;
		};

		prototype.withOwner = function() {
			if (this.owner == null) {
				var PersonFetchOptions = require("as/dto/person/fetchoptions/PersonFetchOptions");
				this.owner = new PersonFetchOptions();
			}
			return this.owner;
		};
		prototype.withOwnerUsing = function(fetchOptions) {
			return this.owner = fetchOptions;
		};
		prototype.hasOwner = function() {
			return this.owner != null;
		};

		prototype.sortBy = function() {
			if (this.sort == null) {
				var TagSortOptions = require("as/dto/tag/fetchoptions/TagSortOptions");
				this.sort = new TagSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		experiments : "ExperimentFetchOptions",
		samples : "SampleFetchOptions",
		dataSets : "DataSetFetchOptions",
		materials : "MaterialFetchOptions",
		owner : "PersonFetchOptions",
		sort : "TagSortOptions"
	});
	return TagFetchOptions;
})