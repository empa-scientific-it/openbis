/**
 * @author pkupczyk
 */

define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
	var SearchOperator = function() {
		Enum.call(this, [ "AND", "OR" ]);
	};
	stjs.extend(SearchOperator, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new SearchOperator();
})