/**
 * Holds information that uniquely identifies a property assignment in openBIS.
 * 
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/IObjectId" ], function(stjs, IObjectId) {
	var IPropertyAssignmentId = function() {
	};
	stjs.extend(IPropertyAssignmentId, null, [ IObjectId ], null, {});
	return IPropertyAssignmentId;
})