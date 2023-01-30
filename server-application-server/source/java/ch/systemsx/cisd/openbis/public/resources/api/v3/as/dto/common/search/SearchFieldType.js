/**
 * @author pkupczyk
 */

define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
	var SearchFieldType = function() {
		Enum.call(this, [ "PROPERTY", "ATTRIBUTE", "ANY_PROPERTY", "ANY_FIELD" ]);
	};
	stjs.extend(SearchFieldType, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new SearchFieldType();
})
