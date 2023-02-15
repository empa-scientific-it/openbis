/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/person/search/PersonSearchCriteria" ], function(require, stjs, PersonSearchCriteria) {
	var PersonalAccessTokenOwnerSearchCriteria = function() {
		PersonSearchCriteria.call(this);
	};
	stjs.extend(PersonalAccessTokenOwnerSearchCriteria, PersonSearchCriteria, [ PersonSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.pat.search.PersonalAccessTokenOwnerSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return PersonalAccessTokenOwnerSearchCriteria;
})