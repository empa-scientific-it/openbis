/**
 * Holds information that uniquely identifies a storage format in openBIS.
 * 
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/IObjectId" ], function(stjs, IObjectId) {
	var IStorageFormatId = function() {
	};
	stjs.extend(IStorageFormatId, null, [ IObjectId ], null, {});
	return IStorageFormatId;
})