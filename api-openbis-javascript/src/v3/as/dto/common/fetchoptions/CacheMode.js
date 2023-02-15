/**
 * @author pkupczyk
 */

define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
	var CacheMode = function() {
		Enum.call(this, [ "NO_CACHE", "CACHE", "RELOAD_AND_CACHE" ]);
	};
	stjs.extend(CacheMode, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new CacheMode();
})
