define([ "stjs", "as/dto/common/id/ObjectPermId", "as/dto/person/id/IPersonId" ], function(stjs, ObjectPermId, IPersonId) {
	var Me = function() {
		ObjectPermId.call(this, null);
	};
	stjs.extend(Me, ObjectPermId, [ ObjectPermId, IPersonId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.person.id.Me';
		constructor.serialVersionUID = 1;

		prototype.toString = function() {
			return "me";
		};
	}, {});
	return Me;
})
