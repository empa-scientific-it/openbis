/**
 * @author pkupczyk
 */

define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
	var DataSetRelationType = function() {
		Enum.call(this, [ "EXPERIMENT", "SAMPLE", "PARENT", "CHILD", "CONTAINER", "COMPONENT" ]);
	};
	stjs.extend(DataSetRelationType, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new DataSetRelationType();
})
