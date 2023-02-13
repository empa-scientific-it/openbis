define([ "stjs", "as/dto/common/operation/IOperation" ], function(stjs, IOperation) {
	var ExecuteProcessingServiceOperation = function(serviceId, options) {
		this.serviceId = serviceId;
		this.options = options;
	};
	stjs.extend(ExecuteProcessingServiceOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.execute.ExecuteProcessingServiceOperation';
		prototype.serviceId = null;
		prototype.options = null;
		prototype.getServiceId = function() {
			return this.serviceId;
		};
		prototype.getOptions = function() {
			return this.options;
		};
		prototype.getMessage = function() {
			return "ExecuteProcessingServiceOperation";
		};
	}, {
		serviceId : "IDssServiceId",
		options : "ProcessingServiceExecutionOptions"
	});
	return ExecuteProcessingServiceOperation;
})
