/**
 * Holds information that uniquely identifies an external data management system
 * in openBIS.
 * 
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/IObjectId" ], function(stjs, IObjectId) {
	var IExternalDmsId = function() {
	};
	stjs.extend(IExternalDmsId, null, [ IObjectId ], null, {});
	return IExternalDmsId;
})