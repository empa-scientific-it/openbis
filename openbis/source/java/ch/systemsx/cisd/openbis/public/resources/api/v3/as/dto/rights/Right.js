define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
	var Right = function() {
		Enum.call(this, [ "CREATE", "UPDATE" ]);
	};
	stjs.extend(Right, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new Right();
})