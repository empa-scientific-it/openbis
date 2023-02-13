define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
	var RoleLevel = function() {
		Enum.call(this, [ "INSTANCE", "SPACE", "PROJECT" ]);
	};
	stjs.extend(RoleLevel, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new RoleLevel();
})
