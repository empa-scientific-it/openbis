/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
	var ExperimentRelationType = function() {
		Enum.call(this, [ "PROJECT", "SAMPLE", "DATA_SET" ]);
	};
	stjs.extend(ExperimentRelationType, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new ExperimentRelationType();
})
