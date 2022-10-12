define([ "stjs", "as/dto/common/id/ObjectPermId", "as/dto/session/id/ISessionInformationId" ], function(stjs, ObjectPermId, ISessionInformationId) {
	var SessionInformationPermId = function(permId) {
		ObjectPermId.call(this, permId);
	};
	stjs.extend(SessionInformationPermId, ObjectPermId, [ ObjectPermId, ISessionInformationId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.session.id.SessionInformationPermId';
		constructor.serialVersionUID = 1;
	}, {});
	return SessionInformationPermId;
})