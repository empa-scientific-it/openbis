define([ "stjs" ], function(stjs) {
	var PluginCreation = function() {
	};
	stjs.extend(PluginCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.plugin.create.PluginCreation';
		constructor.serialVersionUID = 1;
		prototype.name = null;
		prototype.description = null;
		prototype.script = null;
		prototype.pluginType = null;
		prototype.available = true;
		prototype.entityKind = null;

		prototype.getName = function() {
			return this.name;
		};
		prototype.setName = function(name) {
			this.name = name;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description = description;
		};
		prototype.getScript = function() {
			return this.script;
		};
		prototype.setScript = function(script) {
			this.script = script;
		};
		prototype.getPluginType = function() {
			return this.pluginType;
		};
		prototype.setPluginType = function(pluginType) {
			this.pluginType = pluginType;
		};
		prototype.isAvailable = function() {
			return this.available;
		};
		prototype.setAvailable = function(available) {
			this.available = available;
		};
		prototype.getEntityKind = function() {
			return this.entityKind;
		};
		prototype.setEntityKind = function(entityKind) {
			this.entityKind = entityKind;
		};
	}, {
		pluginType : "PluginType",
		entityKind : "EntityKind"
	});
	return PluginCreation;
})
