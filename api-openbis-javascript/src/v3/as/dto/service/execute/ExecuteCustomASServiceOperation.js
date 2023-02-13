/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperation" ], function(stjs, IOperation) {
	var ExecuteCustomASServiceOperation = function(serviceId, options) {
		this.serviceId = serviceId;
		this.options = options;
	};
	stjs.extend(ExecuteCustomASServiceOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.execute.ExecuteCustomASServiceOperation';
		prototype.serviceId = null;
		prototype.options = null;
		prototype.getServiceId = function() {
			return this.serviceId;
		};
		prototype.getOptions = function() {
			return this.options;
		};
		prototype.getMessage = function() {
			return "ExecuteCustomASServiceOperation";
		};
	}, {
		serviceId : "ICustomASServiceId",
		options : "CustomASServiceExecutionOptions"
	});
	return ExecuteCustomASServiceOperation;
})