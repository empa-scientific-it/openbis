define([ "stjs", "as/dto/common/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var ExecuteSearchDomainServiceOperationResult = function(result) {
		this.result = result;
	};
	stjs.extend(ExecuteSearchDomainServiceOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.execute.ExecuteSearchDomainServiceOperationResult';
		prototype.result = null;
		prototype.getResult = function() {
			return this.result;
		};
		prototype.getMessage = function() {
			return "ExecuteSearchDomainServiceOperationResult";
		};
	}, {});
	return ExecuteSearchDomainServiceOperationResult;
})
