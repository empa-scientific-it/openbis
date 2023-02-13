define([ "stjs", "as/dto/deletion/AbstractObjectDeletionOptions" ], function(stjs, AbstractObjectDeletionOptions) {
	var PluginDeletionOptions = function() {
		AbstractObjectDeletionOptions.call(this);
	};
	stjs.extend(PluginDeletionOptions, AbstractObjectDeletionOptions, [ AbstractObjectDeletionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.plugin.delete.PluginDeletionOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return PluginDeletionOptions;
})
