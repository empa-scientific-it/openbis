/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
	var Complete = function() {
		Enum.call(this, [ "YES", "NO", "UNKNOWN" ]);
	};
	stjs.extend(Complete, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new Complete();
})