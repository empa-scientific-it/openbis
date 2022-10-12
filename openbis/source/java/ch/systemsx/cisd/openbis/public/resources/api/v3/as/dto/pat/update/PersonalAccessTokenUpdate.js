/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/FieldUpdateValue" ], function(stjs, FieldUpdateValue) {
	var PersonalAccessTokenUpdate = function() {
		this.accessDate = new FieldUpdateValue();
	};
	stjs.extend(PersonalAccessTokenUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.pat.update.PersonalAccessTokenUpdate';
		constructor.serialVersionUID = 1;
		prototype.personalAccessTokenId = null;
		prototype.accessDate = null;

		prototype.getObjectId = function() {
			return this.getPersonalAccessTokenId();
		};
		prototype.getPersonalAccessTokenId = function() {
			return this.personalAccessTokenId;
		};
		prototype.setPersonalAccessTokenId = function(personalAccessTokenId) {
			this.personalAccessTokenId = personalAccessTokenId;
		};
		prototype.getAccessDate = function() {
			return this.accessDate;
		};
		prototype.setAccessDate = function(accessDate) {
			this.accessDate.setValue(accessDate);
		};
	}, {
		personalAccessTokenId : "IPersonalAccessTokenId",
		accessDate : {
			name : "FieldUpdateValue",
			arguments : [ "Date" ]
		}
	});
	return PersonalAccessTokenUpdate;
})