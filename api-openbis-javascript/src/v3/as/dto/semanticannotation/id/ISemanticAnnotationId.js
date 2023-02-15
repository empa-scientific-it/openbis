/**
 * Holds information that uniquely identifies a semantic annotation in openBIS.
 * 
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/IObjectId" ], function(stjs, IObjectId) {
	var ISemanticAnnotationId = function() {
	};
	stjs.extend(ISemanticAnnotationId, null, [ IObjectId ], null, {});
	return ISemanticAnnotationId;
})