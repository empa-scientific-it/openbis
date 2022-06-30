/**
 * @author pkupczyk
 */
define([ "stjs" ], function(stjs) {
	var PersonalAccessTokenCreation = function() {
	};
	stjs.extend(PersonalAccessTokenCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.pat.create.PersonalAccessTokenCreation';
		constructor.serialVersionUID = 1;
		prototype.ownerId = null;
		prototype.sessionName = null;
		prototype.validFromDate = null;
		prototype.validToDate = null;

		prototype.getOwnerId = function() {
			return this.ownerId;
		};
		prototype.setOwnerId = function(ownerId) {
			this.ownerId = ownerId;
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
	}, {
		ownerId : "IPersonId",
		validFromDate : "Date",
		validToDate : "Date"
	});
	return PersonalAccessTokenCreation;
})