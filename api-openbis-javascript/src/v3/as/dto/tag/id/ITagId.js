/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/IObjectId" ], function(stjs, IObjectId) {
	var ITagId = function() {
	};
	stjs.extend(ITagId, null, [ IObjectId ], null, {});
	return ITagId;
})