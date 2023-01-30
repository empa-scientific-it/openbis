/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/person/search/PersonSearchCriteria" ], function(require, stjs, PersonSearchCriteria) {
	var RegistratorSearchCriteria = function() {
		PersonSearchCriteria.call(this);
	};
	stjs.extend(RegistratorSearchCriteria, PersonSearchCriteria, [ PersonSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.person.search.RegistratorSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return RegistratorSearchCriteria;
})