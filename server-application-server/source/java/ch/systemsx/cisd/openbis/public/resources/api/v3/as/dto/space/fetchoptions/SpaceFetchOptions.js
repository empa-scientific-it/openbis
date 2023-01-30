/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/person/fetchoptions/PersonFetchOptions", "as/dto/sample/fetchoptions/SampleFetchOptions",
		"as/dto/project/fetchoptions/ProjectFetchOptions", "as/dto/space/fetchoptions/SpaceSortOptions" ], function(require, stjs, FetchOptions) {
	var SpaceFetchOptions = function() {
	};
	stjs.extend(SpaceFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.space.fetchoptions.SpaceFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.registrator = null;
		prototype.samples = null;
		prototype.projects = null;
		prototype.sort = null;
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
		prototype.withProjects = function() {
			if (this.projects == null) {
				var ProjectFetchOptions = require("as/dto/project/fetchoptions/ProjectFetchOptions");
				this.projects = new ProjectFetchOptions();
			}
			return this.projects;
		};
		prototype.withProjectsUsing = function(fetchOptions) {
			return this.projects = fetchOptions;
		};
		prototype.hasProjects = function() {
			return this.projects != null;
		};
		prototype.sortBy = function() {
			if (this.sort == null) {
				var SpaceSortOptions = require("as/dto/space/fetchoptions/SpaceSortOptions");
				this.sort = new SpaceSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		registrator : "PersonFetchOptions",
		samples : "SampleFetchOptions",
		projects : "ProjectFetchOptions",
		sort : "SpaceSortOptions"
	});
	return SpaceFetchOptions;
})