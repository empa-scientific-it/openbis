/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/person/fetchoptions/PersonFetchOptions", "as/dto/session/fetchoptions/SessionInformationSortOptions" ], function(require, stjs, FetchOptions) {
	var SessionInformationFetchOptions = function() {
	};
	stjs.extend(SessionInformationFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.session.fetchoptions.SessionInformationFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.person = null;
		prototype.creatorPerson = null;
		prototype.sort = null;

		prototype.withPerson = function() {
			if (this.person == null) {
				var PersonFetchOptions = require("as/dto/person/fetchoptions/PersonFetchOptions");
				this.person = new PersonFetchOptions();
			}
			return this.person;
		};
		prototype.withPersonUsing = function(fetchOptions) {
			return this.person = fetchOptions;
		};
		prototype.hasPerson = function() {
			return this.person != null;
		};
		prototype.withCreatorPerson = function() {
			if (this.creatorPerson == null) {
				var PersonFetchOptions = require("as/dto/person/fetchoptions/PersonFetchOptions");
				this.creatorPerson = new PersonFetchOptions();
			}
			return this.creatorPerson;
		};
		prototype.withCreatorPersonUsing = function(fetchOptions) {
			return this.creatorPerson = fetchOptions;
		};
		prototype.hasCreatorPerson = function() {
			return this.creatorPerson != null;
		};
		prototype.sortBy = function() {
			if (this.sort == null) {
				var SessionInformationSortOptions = require("as/dto/session/fetchoptions/SessionInformationSortOptions");
				this.sort = new SessionInformationSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		person : "PersonFetchOptions",
		creatorPerson : "PersonFetchOptions",
		sort : "SessionInformationSortOptions"
	});
	return SessionInformationFetchOptions;
})