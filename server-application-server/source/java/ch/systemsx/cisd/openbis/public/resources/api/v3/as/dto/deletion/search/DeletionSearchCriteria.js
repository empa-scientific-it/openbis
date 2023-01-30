define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria" ], function(require, stjs, AbstractObjectSearchCriteria) {
	var DeletionSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(DeletionSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.deletion.search.DeletionSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return DeletionSearchCriteria;
})