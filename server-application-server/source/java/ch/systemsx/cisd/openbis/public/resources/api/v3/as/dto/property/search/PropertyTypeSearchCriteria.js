define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/common/search/IdsSearchCriteria", "as/dto/common/search/CodeSearchCriteria", "as/dto/common/search/CodesSearchCriteria", "as/dto/semanticannotation/search/SemanticAnnotationSearchCriteria" ],
		function(require, stjs, AbstractObjectSearchCriteria, CodeSearchCriteria, SemanticAnnotationSearchCriteria) {
			var PropertyTypeSearchCriteria = function() {
				AbstractObjectSearchCriteria.call(this);
			};
			stjs.extend(PropertyTypeSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
				prototype['@type'] = 'as.dto.property.search.PropertyTypeSearchCriteria';
				constructor.serialVersionUID = 1;
				prototype.withIds = function() {
					var IdsSearchCriteria = require("as/dto/common/search/IdsSearchCriteria");
					return this.addCriteria(new IdsSearchCriteria());
				};
				prototype.withCode = function() {
					var CodeSearchCriteria = require("as/dto/common/search/CodeSearchCriteria");
					return this.addCriteria(new CodeSearchCriteria());
				};
				prototype.withCodes = function() {
					var CodesSearchCriteria = require("as/dto/common/search/CodesSearchCriteria");
					return this.addCriteria(new CodesSearchCriteria());
				};
				prototype.withSemanticAnnotations = function() {
					var SemanticAnnotationSearchCriteria = require("as/dto/semanticannotation/search/SemanticAnnotationSearchCriteria");
					return this.addCriteria(new SemanticAnnotationSearchCriteria());
				};
			}, {});

			return PropertyTypeSearchCriteria;
		})