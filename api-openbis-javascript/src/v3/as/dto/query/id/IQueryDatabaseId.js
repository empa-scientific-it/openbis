/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/IObjectId" ], function(stjs, IObjectId) {
	var IQueryDatabaseId = function() {
	};
	stjs.extend(IQueryDatabaseId, null, [ IObjectId ], null, {});
	return IQueryDatabaseId;
})