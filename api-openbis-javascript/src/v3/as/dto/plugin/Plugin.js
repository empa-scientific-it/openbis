define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var Plugin = function() {
	};
	stjs.extend(Plugin, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.plugin.Plugin';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.name = null;
		prototype.permId = null;
		prototype.description = null;
		prototype.pluginKind = null;
		prototype.pluginType = null;
		prototype.entityKinds = null;
		prototype.script = null;
		prototype.available = null;
		prototype.registrator = null;
		prototype.registrationDate = null;

		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};
		prototype.getName = function() {
			return this.name;
		};
		prototype.setName = function(name) {
			this.name = name;
		};
		prototype.getPermId = function() {
			return this.permId;
		};
		prototype.setPermId = function(permId) {
			this.permId = permId;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description = description;
		};
		prototype.getPluginKind = function() {
			return this.pluginKind;
		};
		prototype.setPluginKind = function(pluginKind) {
			this.pluginKind = pluginKind;
		};
		prototype.getPluginType = function() {
			return this.pluginType;
		};
		prototype.setPluginType = function(pluginType) {
			this.pluginType = pluginType;
		};
		prototype.getEntityKinds = function() {
			return this.entityKinds;
		};
		prototype.setEntityKinds = function(entityKinds) {
			this.entityKinds = entityKinds;
		};
		prototype.getScript = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasScript()) {
				return this.script;
			} else {
				throw new exceptions.NotFetchedException("Script has not been fetched.");
			}
		};
		prototype.setScript = function(script) {
			this.script = script;
		};
		prototype.isAvailable = function() {
			return this.available;
		};
		prototype.setAvailable = function(available) {
			this.available = available;
		};
		prototype.getRegistrator = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasRegistrator()) {
				return this.registrator;
			} else {
				throw new exceptions.NotFetchedException("Registrator has not been fetched.");
			}
		};
		prototype.setRegistrator = function(registrator) {
			this.registrator = registrator;
		};
		prototype.getRegistrationDate = function() {
			return this.registrationDate;
		};
		prototype.setRegistrationDate = function(registrationDate) {
			this.registrationDate = registrationDate;
		};
	}, {
		fetchOptions : "PluginFetchOptions",
		permId : "PluginPermId",
		pluginKind : "PluginKind",
		pluginType : "PluginType",
		entityKinds : {
			name : "Set",
			arguments : [ "EntityKind" ]
		},
		registrator : "Person",
		registrationDate : "Date"
	});
	return Plugin;
})