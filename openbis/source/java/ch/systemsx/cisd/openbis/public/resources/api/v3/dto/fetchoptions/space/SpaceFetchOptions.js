/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "dto/fetchoptions/person/PersonFetchOptions", "dto/fetchoptions/sample/SampleFetchOptions", "dto/fetchoptions/project/ProjectFetchOptions" ], function(require, stjs) {
	var SpaceFetchOptions = function() {
	};
	stjs.extend(SpaceFetchOptions, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.space.SpaceFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.registrator = null;
		prototype.samples = null;
		prototype.projects = null;
		prototype.withRegistrator = function() {
			if (this.registrator == null) {
				var PersonFetchOptions = require("dto/fetchoptions/person/PersonFetchOptions");
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
				var SampleFetchOptions = require("dto/fetchoptions/sample/SampleFetchOptions");
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
				var ProjectFetchOptions = require("dto/fetchoptions/project/ProjectFetchOptions");
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
	}, {
		registrator : "PersonFetchOptions",
		samples : "SampleFetchOptions",
		projects : "ProjectFetchOptions"
	});
	return SpaceFetchOptions;
})