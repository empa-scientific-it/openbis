/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
	var ProjectRelationType = function() {
		Enum.call(this, [ "SPACE", "EXPERIMENT", "SAMPLE" ]);
	};
	stjs.extend(ProjectRelationType, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new ProjectRelationType();
})
