/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/search/AbstractCompositeSearchCriteria", "as/dto/global/search/GlobalSearchTextCriteria", "as/dto/global/search/GlobalSearchObjectKindCriteria",
		"as/dto/global/search/GlobalSearchWildCardsCriteria" ], function(require, stjs, AbstractCompositeSearchCriteria) {
	var GlobalSearchCriteria = function() {
		AbstractCompositeSearchCriteria.call(this);
	};
	stjs.extend(GlobalSearchCriteria, AbstractCompositeSearchCriteria, [ AbstractCompositeSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.global.search.GlobalSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withText = function() {
			var GlobalSearchTextCriteria = require("as/dto/global/search/GlobalSearchTextCriteria");
			return this.addCriteria(new GlobalSearchTextCriteria());
		};
		prototype.withObjectKind = function() {
			var GlobalSearchObjectKindCriteria = require("as/dto/global/search/GlobalSearchObjectKindCriteria");
			return this.addCriteria(new GlobalSearchObjectKindCriteria());
		};
		prototype.withWildCards = function() {
			var GlobalSearchWildCardsCriteria = require("as/dto/global/search/GlobalSearchWildCardsCriteria");
			return this.addCriteria(new GlobalSearchWildCardsCriteria());
		};
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return GlobalSearchCriteria;
})