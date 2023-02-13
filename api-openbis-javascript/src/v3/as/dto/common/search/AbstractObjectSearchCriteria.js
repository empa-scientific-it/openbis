/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/AbstractCompositeSearchCriteria", "as/dto/common/search/IdSearchCriteria" ], function(stjs, AbstractCompositeSearchCriteria, IdSearchCriteria) {
	var AbstractObjectSearchCriteria = function() {
		AbstractCompositeSearchCriteria.call(this);
	};
	stjs.extend(AbstractObjectSearchCriteria, AbstractCompositeSearchCriteria, [ AbstractCompositeSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.AbstractObjectSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withId = function() {
			return this.addCriteria(new IdSearchCriteria());
		};
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return AbstractObjectSearchCriteria;
})