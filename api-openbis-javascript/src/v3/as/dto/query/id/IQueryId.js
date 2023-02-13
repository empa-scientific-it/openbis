/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/IObjectId" ], function(stjs, IObjectId) {
	var IQueryId = function() {
	};
	stjs.extend(IQueryId, null, [ IObjectId ], null, {});
	return IQueryId;
})