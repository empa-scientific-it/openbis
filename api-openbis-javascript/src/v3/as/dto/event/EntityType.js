/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
	var EntityType = function() {
		Enum.call(this, [  "ATTACHMENT", "DATA_SET", "EXPERIMENT", "SPACE", "MATERIAL", "PROJECT", "PROPERTY_TYPE", "SAMPLE",
                              "VOCABULARY", "AUTHORIZATION_GROUP", "TAG" ]);
	};
	stjs.extend(EntityType, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new EntityType();
})