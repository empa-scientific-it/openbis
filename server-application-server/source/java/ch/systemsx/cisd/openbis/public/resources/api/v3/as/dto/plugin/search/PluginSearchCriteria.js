define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/common/search/IdsSearchCriteria", 
         "as/dto/common/search/NameSearchCriteria", "as/dto/plugin/search/PluginKindSearchCriteria", "as/dto/plugin/search/PluginTypeSearchCriteria" ],
		function(require, stjs, AbstractObjectSearchCriteria, CodeSearchCriteria, SemanticAnnotationSearchCriteria) {
			var PluginSearchCriteria = function() {
				AbstractObjectSearchCriteria.call(this);
			};
			stjs.extend(PluginSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
				prototype['@type'] = 'as.dto.plugin.search.PluginSearchCriteria';
				constructor.serialVersionUID = 1;
				prototype.withIds = function() {
					var IdsSearchCriteria = require("as/dto/common/search/IdsSearchCriteria");
					return this.addCriteria(new IdsSearchCriteria());
				};
				prototype.withName = function() {
					var NameSearchCriteria = require("as/dto/common/search/NameSearchCriteria");
					return this.addCriteria(new NameSearchCriteria());
				};
				prototype.withPluginType = function() {
					var PluginTypeSearchCriteria = require("as/dto/plugin/search/PluginTypeSearchCriteria");
					return this.addCriteria(new PluginTypeSearchCriteria());
				};
				prototype.withPluginKind = function() {
					var PluginKindSearchCriteria = require("as/dto/plugin/search/PluginKindSearchCriteria");
					return this.addCriteria(new PluginKindSearchCriteria());
				};
			}, {});

			return PluginSearchCriteria;
		})
		