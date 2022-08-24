/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", 'as/dto/person/fetchoptions/PersonFetchOptions', 'as/dto/pat/fetchoptions/PersonalAccessTokenSortOptions' ], function(require, stjs, FetchOptions) {
	var PersonalAccessTokenFetchOptions = function() {
	};
	stjs.extend(PersonalAccessTokenFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.pat.fetchoptions.PersonalAccessTokenFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.owner = null;
		prototype.registrator = null;
		prototype.modifier = null;
		prototype.sort = null;
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
		prototype.sortBy = function() {
			if (this.sort == null) {
				var PersonalAccessTokenSortOptions = require("as/dto/pat/fetchoptions/PersonalAccessTokenSortOptions");
				this.sort = new PersonalAccessTokenSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		owner : "PersonFetchOptions",
		registrator : "PersonFetchOptions",
		modifier : "PersonFetchOptions",
		sort : "PersonalAccessTokenSortOptions"
	});
	return PersonalAccessTokenFetchOptions;
})