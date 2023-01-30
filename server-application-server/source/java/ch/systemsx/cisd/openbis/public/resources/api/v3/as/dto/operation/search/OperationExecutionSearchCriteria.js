/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/search/AbstractCompositeSearchCriteria" ], function(require, stjs, AbstractCompositeSearchCriteria) {
	var OperationExecutionSearchCriteria = function() {
		AbstractCompositeSearchCriteria.call(this);
	};
	stjs.extend(OperationExecutionSearchCriteria, AbstractCompositeSearchCriteria, [ AbstractCompositeSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.operation.search.OperationExecutionSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return OperationExecutionSearchCriteria;
})