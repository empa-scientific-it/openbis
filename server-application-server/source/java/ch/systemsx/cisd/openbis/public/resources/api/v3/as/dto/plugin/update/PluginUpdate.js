define([ "stjs", "as/dto/common/update/FieldUpdateValue" ], function(stjs, FieldUpdateValue) {
	var PluginUpdate = function() {
		this.description = new FieldUpdateValue();
		this.script = new FieldUpdateValue();
		this.available = new FieldUpdateValue();
	};
	stjs.extend(PluginUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.plugin.update.PluginUpdate';
		constructor.serialVersionUID = 1;
		prototype.pluginId = null;
		prototype.description = null;
		prototype.script = null;
		prototype.available = null;

		prototype.getObjectId = function() {
			return this.getPluginId();
		};
		prototype.getPluginId = function() {
			return this.pluginId;
		};
		prototype.setPluginId = function(pluginId) {
			this.pluginId = pluginId;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description.setValue(description);
		};
		prototype.getScript = function() {
			return this.script;
		};
		prototype.setScript = function(script) {
			this.script.setValue(script);
		};
		prototype.getAvailable = function() {
			return this.available;
		};
		prototype.setAvailable = function(available) {
			this.available.setValue(available);
		};
	}, {
		pluginId : "IPluginId",
		description: {
			name: "FieldUpdateValue",
			arguments: ["String"]
		},
		script: {
			name: "FieldUpdateValue",
			arguments: ["String"]
		},
		available: {
			name: "FieldUpdateValue",
			arguments: ["Boolean"]
		}
	});
	return PluginUpdate;
})
