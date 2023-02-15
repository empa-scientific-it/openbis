define([ "stjs", "as/dto/common/operation/IOperation" ], function(stjs, IOperation) {
	var ExecuteReportingServiceOperation = function(serviceId, options) {
		this.serviceId = serviceId;
		this.options = options;
	};
	stjs.extend(ExecuteReportingServiceOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.execute.ExecuteReportingServiceOperation';
		prototype.serviceId = null;
		prototype.options = null;
		prototype.getServiceId = function() {
			return this.serviceId;
		};
		prototype.getOptions = function() {
			return this.options;
		};
		prototype.getMessage = function() {
			return "ExecuteReportingServiceOperation";
		};
	}, {
		serviceId : "IDssServiceId",
		options : "ReportingServiceExecutionOptions"
	});
	return ExecuteReportingServiceOperation;
})
