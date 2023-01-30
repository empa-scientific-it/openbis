define([ "stjs", "as/dto/common/id/ObjectTechId", "as/dto/query/id/IQueryId" ], function(stjs, ObjectTechId, IQueryId) {
	var QueryTechId = function(techId) {
		ObjectTechId.call(this, techId);
	};
	stjs.extend(QueryTechId, ObjectTechId, [ ObjectTechId, IQueryId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.query.id.QueryTechId';
		constructor.serialVersionUID = 1;
	}, {});
	return QueryTechId;
})
