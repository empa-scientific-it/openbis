define([ "stjs", "as/dto/common/operation/IOperation" ], function(stjs, IOperation) {
	var ExecuteAggregationServiceOperation = function(serviceId, options) {
		this.serviceId = serviceId;
		this.options = options;
	};
	stjs.extend(ExecuteAggregationServiceOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.execute.ExecuteAggregationServiceOperation';
		prototype.serviceId = null;
		prototype.options = null;
		prototype.getServiceId = function() {
			return this.serviceId;
		};
		prototype.getOptions = function() {
			return this.options;
		};
		prototype.getMessage = function() {
			return "ExecuteAggregationServiceOperation";
		};
	}, {
		serviceId : "IDssServiceId",
		options : "AggregationServiceExecutionOptions"
	});
	return ExecuteAggregationServiceOperation;
})
