/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
	var DataSetKind = function() {
		Enum.call(this, [ "PHYSICAL", "CONTAINER", "LINK" ]);
	};
	stjs.extend(DataSetKind, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new DataSetKind();
})