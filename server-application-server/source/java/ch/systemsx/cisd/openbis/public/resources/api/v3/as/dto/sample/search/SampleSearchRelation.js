/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
	var SampleSearchRelation = function() {
		Enum.call(this, [ "SAMPLE", "PARENTS", "CHILDREN", "CONTAINER" ]);
	};
	stjs.extend(SampleSearchRelation, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new SampleSearchRelation();
})
