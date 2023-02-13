/**
 * Holds information that uniquely identifies a space in openBIS.
 * 
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/IObjectId" ], function(stjs, IObjectId) {
	var ISpaceId = function() {
	};
	stjs.extend(ISpaceId, null, [ IObjectId ], null, {});
	return ISpaceId;
})