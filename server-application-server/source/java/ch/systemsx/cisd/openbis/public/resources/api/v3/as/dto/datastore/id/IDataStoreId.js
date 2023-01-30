/**
 * Holds information that uniquely identifies a data store in openBIS.
 * 
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/IObjectId" ], function(stjs, IObjectId) {
	var IDataStoreId = function() {
	};
	stjs.extend(IDataStoreId, null, [ IObjectId ], null, {});
	return IDataStoreId;
})