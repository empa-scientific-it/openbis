/**
 * Holds information that uniquely identifies a data set file in openBIS.
 * 
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/IObjectId" ], function(stjs, IObjectId) {
	var IDataSetFileId = function() {
	};
	stjs.extend(IDataSetFileId, null, [ IObjectId ], null, {});
	return IDataSetFileId;
})