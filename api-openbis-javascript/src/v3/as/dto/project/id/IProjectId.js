/**
 * Holds information that uniquely identifies a project in openBIS.
 * 
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/IObjectId" ], function(stjs, IObjectId) {
	var IProjectId = function() {
	};
	stjs.extend(IProjectId, null, [ IObjectId ], null, {});
	return IProjectId;
})