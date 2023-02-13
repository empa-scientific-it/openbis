/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/IdSearchCriteria" ], function(stjs, IdSearchCriteria) {
	var DatabaseIdSearchCriteria = function() {
		IdSearchCriteria.call(this);
	};
	stjs.extend(DatabaseIdSearchCriteria, IdSearchCriteria, [ IdSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.query.search.DatabaseIdSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {});
	return DatabaseIdSearchCriteria;
})