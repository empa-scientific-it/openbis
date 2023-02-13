/**
 * Holds information that uniquely identifies a deletion in openBIS.
 * 
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/IObjectId" ], function(stjs, IObjectId) {
	var IDeletionId = function() {
	};
	stjs.extend(IDeletionId, null, [ IObjectId ], null, {});
	return IDeletionId;
})