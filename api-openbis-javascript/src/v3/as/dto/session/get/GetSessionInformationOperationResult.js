/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var GetSessionInformationOperationResult = function(sessionInformation) {
		this.sessionInformation = sessionInformation;
	};
	stjs.extend(GetSessionInformationOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.session.get.GetSessionInformationOperationResult';
		prototype.sessionInformation = null;
		prototype.getSessionInformation = function() {
			return this.sessionInformation;
		};
		prototype.getMessage = function() {
			return "GetSessionInformationOperationResult";
		};
	}, {
		sessionInformation : "SessionInformation"
	});
	return GetSessionInformationOperationResult;
})