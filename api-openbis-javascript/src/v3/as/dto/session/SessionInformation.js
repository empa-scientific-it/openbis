/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var SessionInformation = function() {
	};
	stjs.extend(SessionInformation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.session.SessionInformation';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.sessionToken = null;
		prototype.homeGroupCode = null;
		prototype.userName = null;
		prototype.personalAccessTokenSession = null;
		prototype.personalAccessTokenSessionName = null;
		prototype.person = null;
		prototype.creatorPerson = null;

		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};

		prototype.getSessionToken = function() {
			return this.sessionToken;
		};
		prototype.setSessionToken = function(sessionToken) {
			this.sessionToken = sessionToken;
		};

		prototype.getHomeGroupCode = function() {
			return this.homeGroupCode;
		};
		prototype.setHomeGroupCode = function(homeGroupCode) {
			this.homeGroupCode = homeGroupCode;
		};
		
		prototype.getUserName = function() {
			return this.userName;
		};
		prototype.setUserName = function(userName) {
			this.userName = userName;
		};

		prototype.isPersonalAccessTokenSession = function() {
			return this.personalAccessTokenSession;
		};
		prototype.setPersonalAccessTokenSession = function(personalAccessTokenSession) {
			this.personalAccessTokenSession = personalAccessTokenSession;
		};

		prototype.getPersonalAccessTokenSessionName = function() {
			return this.personalAccessTokenSessionName;
		};
		prototype.setPersonalAccessTokenSessionName = function(personalAccessTokenSessionName) {
			this.personalAccessTokenSessionName = personalAccessTokenSessionName;
		};

		prototype.getPerson = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasPerson()) {
				return this.person;
			} else {
				throw new exceptions.NotFetchedException("Person has not been fetched.");
			}
		};
		prototype.setPerson = function(person) {
			this.person = person;
		};

		prototype.getCreatorPerson = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasCreatorPerson()) {
				return this.creatorPerson;
			} else {
				throw new exceptions.NotFetchedException("Creator person has not been fetched.");
			}
		};
		prototype.setCreatorPerson = function(creatorPerson) {
			this.creatorPerson = creatorPerson;
		};
	}, {
		fetchOptions : "SessionInformationFetchOptions",
		person : "Person",
		creatorPerson : "Person"
	});
	return SessionInformation;
})