define([ "stjs", "as/dto/common/operation/IOperation" ], function(stjs, IOperation) {
	var ExecuteSearchDomainServiceOperation = function(options) {
		this.options = options;
	};
	stjs.extend(ExecuteSearchDomainServiceOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.execute.ExecuteSearchDomainServiceOperation';
		prototype.options = null;
		prototype.getOptions = function() {
			return this.options;
		};
		prototype.getMessage = function() {
			return "ExecuteSearchDomainServiceOperation";
		};
	}, {
		options : "SearchDomainServiceExecutionOptions"
	});
	return ExecuteSearchDomainServiceOperation;
})
