define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
	var Role = function() {
		Enum.call(this, [ "DISABLED", "ADMIN", "USER", "POWER_USER", "OBSERVER", "ETL_SERVER" ]);
	};
	stjs.extend(Role, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new Role();
})
