define([ "require", "stjs", "as/dto/entitytype/search/AbstractEntityTypeSearchCriteria" ], 
	function(require, stjs, AbstractEntityTypeSearchCriteria) {
		var ExperimentTypeSearchCriteria = function() {
			AbstractEntityTypeSearchCriteria.call(this);
		};
		stjs.extend(ExperimentTypeSearchCriteria, AbstractEntityTypeSearchCriteria, [ AbstractEntityTypeSearchCriteria ], function(constructor, prototype) {
			prototype['@type'] = 'as.dto.experiment.search.ExperimentTypeSearchCriteria';
			constructor.serialVersionUID = 1;
		}, {});
		
	return ExperimentTypeSearchCriteria;
})