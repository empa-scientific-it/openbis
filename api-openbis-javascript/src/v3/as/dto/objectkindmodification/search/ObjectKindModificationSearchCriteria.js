/**
 * @author Franz-Josef Elmer
 */
define([ "stjs", "as/dto/common/search/AbstractCompositeSearchCriteria", "as/dto/objectkindmodification/search/ObjectKindCriteria", "as/dto/objectkindmodification/search/OperationKindCriteria" ],
		function(stjs, AbstractCompositeSearchCriteria, ObjectKindCriteria, OperationKindCriteria) {
			var ObjectKindModificationSearchCriteria = function() {
				AbstractCompositeSearchCriteria.call(this);
			};
			stjs.extend(ObjectKindModificationSearchCriteria, AbstractCompositeSearchCriteria, [ AbstractCompositeSearchCriteria ], function(constructor, prototype) {
				prototype['@type'] = 'as.dto.objectkindmodification.search.ObjectKindModificationSearchCriteria';
				constructor.serialVersionUID = 1;

				prototype.withObjectKind = function() {
					var ObjectKindCriteria = require("as/dto/objectkindmodification/search/ObjectKindCriteria");
					return this.addCriteria(new ObjectKindCriteria());
				};

				prototype.withOperationKind = function() {
					var OperationKindCriteria = require("as/dto/objectkindmodification/search/OperationKindCriteria");
					return this.addCriteria(new OperationKindCriteria());
				};

			}, {
				criteria : {
					name : "Collection",
					arguments : [ "ISearchCriteria" ]
				}
			});
			return ObjectKindModificationSearchCriteria;
		})
