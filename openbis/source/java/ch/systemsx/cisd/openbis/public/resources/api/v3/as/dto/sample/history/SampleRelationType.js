/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
	var SampleRelationType = function() {
		Enum.call(this, [ "SPACE", "PROJECT", "EXPERIMENT", "PARENT", "CHILD", "CONTAINER", "COMPONENT", "DATA_SET" ]);
	};
	stjs.extend(SampleRelationType, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new SampleRelationType();
})
