/**
 * Plugin (i.e. dynamic property evaluator, managed property handler, entity validator) perm id.
 * 
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/ObjectPermId", "as/dto/plugin/id/IPluginId" ], function(stjs, ObjectPermId, IPluginId) {

	/**
	 * @param permId
	 *            Plugin perm id, e.g. "MY_PLUGIN". Plugin perm id is case sensitive.
	 */
	var PluginPermId = function(permId) {
		ObjectPermId.call(this, permId);
	};
	stjs.extend(PluginPermId, ObjectPermId, [ ObjectPermId, IPluginId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.plugin.id.PluginPermId';
		constructor.serialVersionUID = 1;
	}, {});
	return PluginPermId;
})