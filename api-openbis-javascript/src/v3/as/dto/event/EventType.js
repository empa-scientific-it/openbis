/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
	var EventType = function() {
		Enum.call(this, [ "DELETION", "MOVEMENT", "FREEZING" ]);
	};
	stjs.extend(EventType, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new EventType();
})