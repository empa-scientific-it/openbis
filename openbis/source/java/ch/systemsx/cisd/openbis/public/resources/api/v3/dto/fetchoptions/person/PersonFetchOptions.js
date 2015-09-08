/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "dto/fetchoptions/FetchOptions", "dto/fetchoptions/space/SpaceFetchOptions", "dto/fetchoptions/person/PersonSortOptions" ], function(require, stjs, FetchOptions) {
	var PersonFetchOptions = function() {
	};
	stjs.extend(PersonFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.person.PersonFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.space = null;
		prototype.registrator = null;
		prototype.sort = null;
		prototype.withSpace = function() {
			if (this.space == null) {
				var SpaceFetchOptions = require("dto/fetchoptions/space/SpaceFetchOptions");
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
		prototype.withRegistrator = function() {
			if (this.registrator == null) {
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
		prototype.sortBy = function() {
			if (this.sort == null) {
				var PersonSortOptions = require("dto/fetchoptions/person/PersonSortOptions");
				this.sort = new PersonSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		space : "SpaceFetchOptions",
		registrator : "PersonFetchOptions",
		sort : "PersonSortOptions"
	});
	return PersonFetchOptions;
})