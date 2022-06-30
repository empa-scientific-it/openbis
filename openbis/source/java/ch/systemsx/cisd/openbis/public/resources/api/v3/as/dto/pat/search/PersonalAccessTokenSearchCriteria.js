/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/common/search/AbstractCompositeSearchCriteria" ], function(require, stjs, AbstractObjectSearchCriteria) {
	var PersonalAccessTokenSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(PersonalAccessTokenSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.pat.search.PersonalAccessTokenSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return PersonalAccessTokenSearchCriteria;
})