/**
 * Holds information that uniquely identifies an event in openBIS.
 *
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/IObjectId" ], function(stjs, IObjectId) {
	var IEventId = function() {
	};
	stjs.extend(IEventId, null, [ IObjectId ], null, {});
	return IEventId;
})