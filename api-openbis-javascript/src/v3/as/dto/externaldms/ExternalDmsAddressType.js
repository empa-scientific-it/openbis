/**
 * @author anttil
 */
 
define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
	var ExternalDmsAddressType = function() {
		Enum.call(this, [ "OPENBIS", "URL", "FILE_SYSTEM" ]);
	};
	stjs.extend(ExternalDmsAddressType, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new ExternalDmsAddressType();
})