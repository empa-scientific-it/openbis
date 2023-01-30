/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
	var EntityKind = function() {
		Enum.call(this, [ "MATERIAL", "EXPERIMENT", "SAMPLE", "DATA_SET" ]);
	};
	stjs.extend(EntityKind, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new EntityKind();
})