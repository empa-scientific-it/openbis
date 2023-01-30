/**
 * Holds information that uniquely identifies a person in openBIS.
 * 
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/IObjectId" ], function(stjs, IObjectId) {
	var IPersonId = function() {
	};
	stjs.extend(IPersonId, null, [ IObjectId ], null, {});
	return IPersonId;
})