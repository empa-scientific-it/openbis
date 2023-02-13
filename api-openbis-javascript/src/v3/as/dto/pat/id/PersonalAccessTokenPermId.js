define([ "stjs", "as/dto/common/id/ObjectPermId", "as/dto/pat/id/IPersonalAccessTokenId" ], function(stjs, ObjectPermId, IPersonalAccessTokenId) {
	var PersonalAccessTokenPermId = function(permId) {
		ObjectPermId.call(this, permId);
	};
	stjs.extend(PersonalAccessTokenPermId, ObjectPermId, [ ObjectPermId, IPersonalAccessTokenId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.pat.id.PersonalAccessTokenPermId';
		constructor.serialVersionUID = 1;
	}, {});
	return PersonalAccessTokenPermId;
})