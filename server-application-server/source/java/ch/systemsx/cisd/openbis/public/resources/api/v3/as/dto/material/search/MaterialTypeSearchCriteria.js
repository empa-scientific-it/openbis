define([ "require", "stjs", "as/dto/entitytype/search/AbstractEntityTypeSearchCriteria" ], 
	function(require, stjs, AbstractEntityTypeSearchCriteria) {
		var MaterialTypeSearchCriteria = function() {
			AbstractEntityTypeSearchCriteria.call(this);
		};
		stjs.extend(MaterialTypeSearchCriteria, AbstractEntityTypeSearchCriteria, [ AbstractEntityTypeSearchCriteria ], function(constructor, prototype) {
			prototype['@type'] = 'as.dto.material.search.MaterialTypeSearchCriteria';
			constructor.serialVersionUID = 1;
		}, {});
		
	return MaterialTypeSearchCriteria;
})