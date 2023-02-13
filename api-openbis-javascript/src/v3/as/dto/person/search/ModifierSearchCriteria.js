/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/person/search/PersonSearchCriteria" ], function(require, stjs, PersonSearchCriteria) {
	var ModifierSearchCriteria = function() {
		PersonSearchCriteria.call(this);
	};
	stjs.extend(ModifierSearchCriteria, PersonSearchCriteria, [ PersonSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.person.search.ModifierSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return ModifierSearchCriteria;
})