define([ "require", "stjs", "as/dto/entitytype/search/AbstractEntityTypeSearchCriteria" ], 
	function(require, stjs, AbstractEntityTypeSearchCriteria) {
		var DataSetTypeSearchCriteria = function() {
			AbstractEntityTypeSearchCriteria.call(this);
		};
		stjs.extend(DataSetTypeSearchCriteria, AbstractEntityTypeSearchCriteria, [ AbstractEntityTypeSearchCriteria ], function(constructor, prototype) {
			prototype['@type'] = 'as.dto.dataset.search.DataSetTypeSearchCriteria';
			constructor.serialVersionUID = 1;
		}, {});
		
	return DataSetTypeSearchCriteria;
})