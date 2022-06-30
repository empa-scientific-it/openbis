/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/common/search/AbstractCompositeSearchCriteria" ], function(require, stjs, AbstractObjectSearchCriteria) {
	var SessionInformationSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(SessionInformationSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.session.search.SessionInformationSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return SessionInformationSearchCriteria;
})