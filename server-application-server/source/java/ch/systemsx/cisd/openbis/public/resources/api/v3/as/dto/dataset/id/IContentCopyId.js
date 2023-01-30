/**
 * Holds information that uniquely identifies a content copy in openBIS.
 * 
 * @author anttil
 */
define([ "stjs", "as/dto/common/id/IObjectId" ], function(stjs, IObjectId) {
	var IContentCopyId = function() {
	};
	stjs.extend(IContentCopyId, null, [ IObjectId ], null, {});
	return IContentCopyId;
})