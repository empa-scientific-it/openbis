define([ "stjs" ], function(stjs) {
	var PersonCreation = function() {
	};
	stjs.extend(PersonCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.person.create.PersonCreation';
		constructor.serialVersionUID = 1;
		prototype.userId = null;
		prototype.spaceId = null;

		prototype.getUserId = function() {
			return this.userId;
		};
		prototype.setUserId = function(userId) {
			this.userId = userId;
		};
		prototype.getSpaceId = function() {
			return this.spaceId;
		};
		prototype.setSpaceId = function(spaceId) {
			this.spaceId = spaceId;
		};
	}, {
		spaceId: "ISpaceId"
	});
	return PersonCreation;
})