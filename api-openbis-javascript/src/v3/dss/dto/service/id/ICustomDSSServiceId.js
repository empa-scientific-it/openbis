/**
 * Holds information that uniquely identifies a custom dss service in openBIS.
 *
 */
define([ "stjs", "as/dto/common/id/IObjectId" ], function(stjs, IObjectId) {
	var ICustomDSSServiceId = function() {
	};
	stjs.extend(ICustomDSSServiceId, null, [ IObjectId ], null, {});
	return ICustomDSSServiceId;
})
