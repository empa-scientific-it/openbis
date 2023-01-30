/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var PersonalAccessToken = function() {
	};
	stjs.extend(PersonalAccessToken, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.pat.PersonalAccessToken';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.permId = null;
		prototype.hash = null;
		prototype.sessionName = null;
		prototype.validFromDate = null;
		prototype.validToDate = null;
		prototype.owner = null;
		prototype.registrator = null;
		prototype.modifier = null;
		prototype.registrationDate = null;
		prototype.modificationDate = null;
		prototype.accessDate = null;

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
		prototype.getHash = function() {
			return this.hash;
		};
		prototype.setHash = function(hash) {
			this.hash = hash;
		};
		prototype.getSessionName = function() {
			return this.sessionName;
		};
		prototype.setSessionName = function(sessionName) {
			this.sessionName = sessionName;
		};
		prototype.getValidFromDate = function() {
			return this.validFromDate;
		};
		prototype.setValidFromDate = function(validFromDate) {
			this.validFromDate = validFromDate;
		};
		prototype.getValidToDate = function() {
			return this.validToDate;
		};
		prototype.setValidToDate = function(validToDate) {
			this.validToDate = validToDate;
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
		prototype.getAccessDate = function() {
			return this.accessDate;
		};
		prototype.setAccessDate = function(accessDate) {
			this.accessDate = accessDate;
		};
	}, {
		fetchOptions : "PersonalAccessTokenFetchOptions",
		permId : "PersonalAccessTokenPermId",
		validFromDate : "Date",
		validToDate : "Date",
		owner : "Person",
		registrator : "Person",
		modifier : "Person",
		registrationDate : "Date",
		modificationDate : "Date",
		accessDate : "Date"
	});
	return PersonalAccessToken;
})