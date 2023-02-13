/**
 * Holds information that uniquely identifies a plugin (i.e. dynamic property evaluator, managed property handler, entity validator) in openBIS.
 * 
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/IObjectId" ], function(stjs, IObjectId) {
	var IPluginId = function() {
	};
	stjs.extend(IPluginId, null, [ IObjectId ], null, {});
	return IPluginId;
})