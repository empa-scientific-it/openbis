/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
	var QueryType = function() {
		Enum.call(this, [ "GENERIC", "EXPERIMENT", "SAMPLE", "DATA_SET", "MATERIAL" ]);
	};
	stjs.extend(QueryType, Enum, [ Enum ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.query.QueryType';
	}, {});
	return new QueryType();
})