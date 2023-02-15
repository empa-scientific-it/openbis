/**
 * Enumeration of operations which can be applied to an object.
 *
 * @author Franz-Josef Elmer
 */
define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
	var OperationKind = function() {
		Enum.call(this, [ "CREATE_OR_DELETE", "UPDATE" ]);
	};
	stjs.extend(OperationKind, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new OperationKind();
})
