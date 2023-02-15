/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/operation/IOperationExecutionNotification" ], function(stjs, IOperationExecutionNotification) {
	var OperationExecutionEmailNotification = function(emails) {
		this.emails = emails;
	};
	stjs.extend(OperationExecutionEmailNotification, null, [ IOperationExecutionNotification ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.operation.OperationExecutionEmailNotification';
		prototype.emails = null;
		prototype.getEmails = function() {
			return this.emails;
		};
	}, {
		emails : {
			name : "List",
			arguments : [ null ]
		}
	});
	return OperationExecutionEmailNotification;
})